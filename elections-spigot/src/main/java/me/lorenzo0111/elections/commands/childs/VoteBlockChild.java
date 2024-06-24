/*
 * This file is part of ElectionsPlus, licensed under the MIT License.
 *
 * Copyright (c) Lorenzo0111
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
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class VoteBlockChild extends SubCommand {
    private final ElectionsPlus plugin;

    public VoteBlockChild(Command command, ElectionsPlus plugin) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "vote-block";
    }

    @Permission("elections.create")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (args.length != 2) {
            Messages.send(sender.audience(), true, "errors", "bad-args");
            return;
        }
        if (!(sender.player() instanceof Player)) {
            Messages.send(sender.audience(), true, "errors", "console");
            return;
        }

        Player player = (Player) sender.player();
        Block block = player.getTargetBlock(null, 5);

        if (block == null || block.isEmpty()) {
            Messages.send(sender.audience(), true, "errors", "no-block");
            return;
        }

        if (args[1].equalsIgnoreCase("create")) {
            this.create(sender, block);
            return;
        }

        if (args[1].equalsIgnoreCase("delete")) {
            this.delete(sender, block);
            return;
        }

        Messages.send(sender.audience(), true, "errors", "bad-args");
    }

    private void create(User<?> sender, Block block) {
        Location location = block.getLocation();

        plugin.getManager()
                .createElectionBlock(
                        new ElectionBlock(
                                location.getWorld().getName(),
                                location.getBlockX(),
                                location.getBlockY(),
                                location.getBlockZ()
                        )
                )
                .thenAccept(electionBlock -> {
                    if (electionBlock == null) {
                        Messages.send(sender.audience(), true, "errors", "block-already-exists");
                        return;
                    }

                    Messages.send(sender.audience(), true, "vote-block", "created");
                });
    }

    private void delete(User<?> sender, Block block) {
        ElectionBlock electionBlock = new ElectionBlock(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
        plugin.getManager().deleteElectionBlock(electionBlock);

        Messages.send(sender.audience(), true, "vote-block", "deleted");
    }
}

