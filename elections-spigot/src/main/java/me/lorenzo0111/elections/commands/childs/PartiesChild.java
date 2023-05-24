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
import me.lorenzo0111.elections.conversation.ConversationUtil;
import me.lorenzo0111.elections.conversation.conversations.CreatePartyConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.menus.PartiesMenu;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import org.bukkit.entity.Player;

public class PartiesChild extends SubCommand {
    private final ElectionsPlus plugin;

    public PartiesChild(Command command, ElectionsPlus plugin) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "parties";
    }

    private static String array2string(String[] args, int start) {
        String result = "";
        for (int i = start; i < args.length; i++) {
            if (i > 2) {
                result = result + " ";
            }
            result = result + args[i];
        }

        return result;
    }

    @Permission(value = "elections.parties")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (!(sender.player() instanceof Player)) {
            Messages.send(sender.audience(),true, "errors", "console");
            return;
        }
        Player player = (Player)sender.player();

        if (args.length < 2) {
            plugin.getManager()
                .getParties()
                .thenAccept((parties) -> new PartiesMenu((Player) sender.player(), parties, plugin).setup());
            return;
        }

        if (args[1].equalsIgnoreCase("create")) {
            CreatePartyConversation conversation = new CreatePartyConversation(player, plugin);
            String partyName = array2string(args, 2);

            if(partyName == "") {
                ConversationUtil.createConversation(plugin, conversation);
            } else {
                conversation.handle(partyName);
            }
            return;
        }
        
        if (args[1].equalsIgnoreCase("delete")) {
            String partyName = array2string(args, 2);
            if (partyName == "") {
                player.sendMessage(Messages.componentString(true, "errors", "party-name-required"));
                return;
            }

            this.plugin.getManager().deleteParty(partyName);
            player.sendMessage(Messages.componentString(true, "parties", "deleted"));
            return;
        }

        player.sendMessage(Messages.componentString(true, "errors", "command-not-found"));
    }
}
