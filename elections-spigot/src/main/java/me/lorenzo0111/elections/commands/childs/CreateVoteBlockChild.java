/*
 * This file is part of ElectionsPlus, licensed under the MIT License.
 *
 * Copyright (©) tadhunt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.lorenzo0111.elections.commands.childs;

import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.ElectionBlock;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.menus.ElectionsMenu;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CreateVoteBlockChild extends SubCommand implements Listener {
    private final ElectionsPlus plugin;
    private List<Block> blocks;
    private Boolean already = false;
    
    public CreateVoteBlockChild(Command command, ElectionsPlus plugin) {
        super(command);
        this.plugin = plugin;
        this.blocks = new ArrayList<Block>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setup() {
        if (already) {
            plugin.getLogger().info("already setup");
            return;
        }
        
        already = true;
        plugin.getLogger().info("running setup");

        plugin.getManager()
        .getElectionBlocks()
        .thenAccept(electionBlocks -> {
            if (electionBlocks == null) {
                    plugin.getLogger().info("No election blocks");
                    return;
            }

            for (ElectionBlock electionBlock : electionBlocks) {
                Map<String, Object> rawLocation = electionBlock.getLocation();
                Location location = Location.deserialize(rawLocation);

                World world = Bukkit.getWorld("world");
                Block block = world.getBlockAt(location);
                String blockData = block.getBlockData().getAsString();

                if (electionBlock.getBlockData().equals(blockData)) {
                    blocks.add(block);
                }
            }
        });

    }

    @Override
    public String getName() {
        return "create-vote-block";
    }

    @Permission("elections.create")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (!(sender.player() instanceof Player)) {
            Messages.send(sender.audience(), true, "errors", "console");
            return;
        }

        Player player = (Player)sender.player();

        Block block = player.getTargetBlock(null, 50);

        if (block == null) {
            Messages.send(sender.audience(), true, "errors", "bad-args");
        }
        if (block.isEmpty()) {
            Messages.send(sender.audience(), true, "errors", "no-block");
        }

        this.blocks.add(block);

        UUID world = block.getWorld().getUID();
        Map<String, Object> location = block.getLocation().serialize();
        String blockData = block.getBlockData().getAsString();

        plugin.getManager()
            .createElectionBlock(world, location, blockData)
            .thenAccept(electionBlock -> {
                if (electionBlock == null) {
                        player.sendMessage("block at that location already exists");
                        return;
                }

                player.sendMessage("block added");
                player.sendMessage(electionBlock.toString());
            });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        //Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        if(hand != EquipmentSlot.HAND) {
            return;
        }

        if (!this.blocks.contains(clickedBlock)) {
            return;
        }

        plugin
            .getManager()
            .getElections()
            .thenAccept((elections) -> new ElectionsMenu(player, elections, ElectionsPlus.getInstance()).setup());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block brokenBlock = e.getBlock();

        if (this.blocks.contains(brokenBlock)) {
            e.setCancelled(true);
        }
    }
}
