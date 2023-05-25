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

import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;

public class HelpChild extends SubCommand {

    public HelpChild(Command command) {
        super(command);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        this.formatHelp(sender, null, "help", "header");
        this.formatHelp(sender, "elections.create", "help", "create");
        this.formatHelp(sender, "elections.create", "help", "add-party");
        this.formatHelp(sender, "elections.parties", "help", "parties");
        this.formatHelp(sender, "elections.list", "help", "list");
        this.formatHelp(sender, "elections.disband", "help", "disband");
        // "elections help"
        this.formatHelp(sender, "elections.vote", "help", "vote");
        this.formatHelp(sender, "elections.reload", "help", "reload");
        this.formatHelp(sender, "elections.info", "help", "info");
        this.formatHelp(sender, "elections.close", "help", "close");
        this.formatHelp(sender, "elections.proceed", "help", "proceed");
    }

    private void formatHelp(User<?> sender, String permission, Object... path) {
        if (permission == null || !sender.hasPermission(permission)) {
            return;
        }
        Messages.send(sender.audience(), true, path);
    }
}
