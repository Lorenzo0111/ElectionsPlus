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
import me.lorenzo0111.elections.conversation.NameConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.menus.CreateElectionMenu;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import me.lorenzo0111.pluginslib.conversation.ConversationUtil;
import org.spongepowered.api.entity.living.player.Player;

public class CreateChild extends SubCommand {
    private final ElectionsPlus plugin;

    public CreateChild(Command command, ElectionsPlus plugin) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Permission("elections.create")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (!(sender.player() instanceof Player)) {
            Messages.send(sender.audience(),true, "errors", "console");
            return;
        }

        CreateElectionMenu menu = new CreateElectionMenu(plugin,"None",(Player) sender);

        if (args.length != 2) {
            ConversationUtil.startConversation((Player) sender.player(),new NameConversation(plugin,menu));
            return;
        }

        String name = args[1];

        menu.setName(name);
        menu.setup();
    }
}
