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
import me.lorenzo0111.elections.config.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.ICommand;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class CloseChild extends SubCommand {
    private final ElectionsPlus plugin;

    public CloseChild(ICommand<?> command, ElectionsPlus plugin) {
        super(command);

        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "close";
    }

    @Permission("elections.close")
    @Override
    public void handleSubcommand(User<?> user, String[] args) {
        if (args.length < 2) {
            user.audience().sendMessage(Messages.component(true, "errors.election-name-missing"));
            return;
        }

        plugin.getApi()
                .getElection(args[1])
                .thenAccept((election) -> {
                    if (election != null) {
                        election.close();

                        user.audience().sendMessage(Messages.component(true, "errors.election-closed", Placeholder.unparsed("name", election.getName())));
                        return;
                    }

                    user.audience().sendMessage(Messages.component(true, "election-not-found", Placeholder.unparsed("name", args[1])));
                });
    }
}
