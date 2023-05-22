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
import me.lorenzo0111.elections.handlers.ChatColor;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.ICommand;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import net.kyori.adventure.text.Component;

public class CloseChild extends SubCommand {

    public CloseChild(ICommand<?> command) {
        super(command);
    }

    @Override
    public String getName() {
        return "close";
    }

    @Permission("elections.close")
    @Override
    public void handleSubcommand(User<?> user, String[] args) {
        ElectionsPlus plugin = (ElectionsPlus) getCommand().getPlugin();

        if (args.length != 2) {
            Messages.send(user.audience(), true, "errors", "election-name-missing");
            return;
        }

        plugin.getApi()
                .getElection(args[1])
                .thenAccept((election) -> {
                    if (election != null) {
                        plugin.getManager()
                                .deleteElection(election);

                        Messages.send(user.audience(), true, Messages.single("name", election.getName()), "election", "deleted");
                        return;
                    }

                    Messages.send(user.audience(), true, Messages.single("name", args[1]), "errors", "election-not-found");
                });
    }
}
