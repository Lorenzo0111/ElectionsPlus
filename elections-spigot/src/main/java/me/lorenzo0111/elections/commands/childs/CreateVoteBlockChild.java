/*
 * This file is part of ElectionsPlus, licensed under the MIT License.
 *
 * Copyright (c) tadhunt
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
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.menus.ElectionsMenu;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CreateVoteBlockChild extends SubCommand implements Listener {
    private final ElectionsPlus plugin;
    private Block block;
    
    public CreateVoteBlockChild(Command command, ElectionsPlus plugin) {
        super(command);
        this.plugin = plugin;
        this.block = null;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // database lookup to see if any block(s) have been registered
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

        this.block = block;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if(this.block == null || hand != EquipmentSlot.HAND) {
            return;
        }

        if(!this.block.equals(block)) {
            return;
        }

        player.sendMessage("clicked: action: " + action.toString());

        plugin
            .getManager()
            .getElections()
            .thenAccept((elections) -> new ElectionsMenu(player, elections, ElectionsPlus.getInstance()).setup());
    }
}
