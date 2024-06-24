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
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.menus.PartiesMenu;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

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

    @Permission(value = "elections.parties")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (!(sender.player() instanceof Player)) {
            sender.audience().sendMessage(Messages.component(true, "errors.console"));
            return;
        }

        Player player = (Player) sender.player();

        if (args.length < 2) {
            plugin.getManager()
                    .getParties()
                    .thenAccept((parties) -> new PartiesMenu(player, parties, plugin).setup());
            return;
        }


        switch (args[1].toLowerCase()) {
            case "create":
                CreatePartyConversation conversation = new CreatePartyConversation(player, plugin);
                switch (args.length) {
                    case 2:
                        ConversationUtil.createConversation(plugin, conversation);
                        break;
                    case 3:
                        conversation.handle(args[2]);
                        break;
                    default:
                        sender.audience().sendMessage(Messages.component(true, "errors.bad-args"));
                        break;
                }
                return;
            case "delete":
                if (args.length == 3) {
                    String partyName = args[2];
                    if (partyName.isEmpty()) {
                        sender.audience().sendMessage(Messages.component(true, "errors.party-name-required"));
                        return;
                    }

                    this.plugin.getManager().deleteParty(partyName);
                    sender.audience().sendMessage(Messages.component(true, "parties.deleted"));
                    return;
                }

                sender.audience().sendMessage(Messages.component(true, "errors.bad-args"));
                return;
            case "add-member":
                if (args.length == 4) {
                    String partyName = args[2];
                    String memberName = args[3];

                    plugin.getManager()
                            .getParties()
                            .thenAccept((parties) -> addMember(sender.audience(), parties, partyName, memberName));
                    return;
                }

                sender.audience().sendMessage(Messages.component(true, "errors.bad-args"));
                return;
            default:
                sender.audience().sendMessage(Messages.component(true, "errors.bad-args"));
                break;
        }
    }

    private void addMember(Audience audience, List<Party> parties, String partyName, String memberName) {
        Player member = Bukkit.getPlayer(memberName);
        if (member == null) {
            audience.sendMessage(Messages.component(true, "errors.user-not-online", Placeholder.unparsed("name", memberName)));
            return;
        }

        for (Party party : parties) {
            if (party.getName().equals(partyName)) {
                party.addMember(member.getUniqueId());

                audience.sendMessage(Messages.component(true, "parties.user-added",
                        Placeholder.unparsed("name", member.getName()),
                        Placeholder.unparsed("party", party.getName())));
                return;
            }
        }

        audience.sendMessage(Messages.component(true, "errors.party-not-found",
                Placeholder.unparsed("party", partyName)));
    }
}
