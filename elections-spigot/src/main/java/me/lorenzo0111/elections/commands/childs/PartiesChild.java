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
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.ConversationUtil;
import me.lorenzo0111.elections.conversation.conversations.CreatePartyConversation;
import me.lorenzo0111.elections.conversation.conversations.AddMemberConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.menus.PartiesMenu;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;

import java.util.ArrayList;
import java.util.List;

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

    private static ArrayList<String> unquote(String[] args, int start) {
        String s = array2string(args, start);
        ArrayList<String> results = new ArrayList<String>();
        Boolean inQuote = false;

        String element = "";
        Character quote = Character.valueOf('"');
        Character space = Character.valueOf(' ');
        Character last = Character.valueOf('x');
        
        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);

            if (inQuote) {
                if (c.equals(quote)) {
                    inQuote = false;
                    results.add(element);
                    element = "";
                } else {
                    element += c;
                }
            } else {
                if (c.equals(quote)) {
                    inQuote = true;
                } else if (c.equals(space)) {
                    if (last.equals(quote)) {
                        // strip it
                    } else {
                        results.add(element);
                        element = "";
                    }
                } else {
                    element += c;
                }
            }
            last = c;
        }

        if (element.length() > 0) {
            results.add(element);
        }

        return results;
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

        if (args[1].equalsIgnoreCase("add-member")) {
            ArrayList<String> a = unquote(args, 2);
            for (String x : a) {
                player.sendMessage("ARG: " + x);
            }
            if (a.size() != 2) {
                player.sendMessage(Messages.componentString(true, "errors", "bad-args"));
                return;
            }

            String partyName = a.get(0);

            if (partyName == "") {
                player.sendMessage(Messages.componentString(true, "errors", "party-name-required"));
                return;
            }
            String memberName = a.get(1);

            if (memberName == "") {
                player.sendMessage(Messages.componentString(true, "errors", "member-name-required"));
                return;
            }

            plugin.getManager()
                .getParties()
                .thenAccept((parties) -> addMember(player, parties, partyName, memberName));

            return;
        }

        player.sendMessage(Messages.componentString(true, "errors", "command-not-found"));
    }

    private void addMember(Player player, List<Party> parties, String partyName, String memberName) {
        for (Party party : parties) {
            if (party.getName().equals(partyName)) {
                AddMemberConversation conversation = new AddMemberConversation(party, player, this.plugin);
                conversation.handle(memberName);
                return;
            }
        }
        player.sendMessage(Messages.componentString(true, Messages.single("party", partyName), "errors", "party-not-found"));
    }
}
