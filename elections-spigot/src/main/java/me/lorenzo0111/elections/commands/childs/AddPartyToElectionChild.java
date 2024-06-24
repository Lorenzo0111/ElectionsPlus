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
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.config.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class AddPartyToElectionChild extends SubCommand {
    private final ElectionsPlus plugin;

    public AddPartyToElectionChild(Command command, ElectionsPlus plugin) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "add-party";
    }

    @Permission("elections.create")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (!(sender.player() instanceof Player)) {
            sender.audience().sendMessage(Messages.component(true, "errors.console"));
            return;
        }

        if (args.length < 3) {
            sender.audience().sendMessage(Messages.component(true, "errors.bad-args"));
            return;
        }

        plugin.getManager()
                .getElections()
                .thenAccept((elections) -> handleElections(sender, args[1], args[2], elections));
    }

    private void handleElections(User<?> sender, String electionName, String party, List<Election> elections) {
        Optional<Election> e = elections.stream()
                .filter(election -> election.getName().equals(electionName))
                .findFirst();

        if (e.isPresent()) {
            addPartyToElection(e.get(), sender, party);
            return;
        }

        sender.audience()
                .sendMessage(Messages.component(
                        true,
                        "errors.election-not-found",
                        Placeholder.unparsed("name", electionName))
                );
    }

    private void addPartyToElection(Election election, User<?> sender, String partyName) {
        plugin.getManager()
                .getParties()
                .thenAccept((parties) -> {
                    List<Party> electionParties = election.getParties();

                    Party party = parties.stream()
                            .filter(p -> p.getName().equalsIgnoreCase(partyName))
                            .findFirst()
                            .orElse(null);

                    if (party == null) {
                        sender.audience().sendMessage(Messages.component(true,
                                "errors.party-not-found",
                                Placeholder.unparsed("party", partyName)));
                        return;
                    }

                    if (electionParties.contains(party)) {
                        sender.audience().sendMessage(Messages.component(true,
                                "errors.party-already-added",
                                Placeholder.unparsed("party", partyName),
                                Placeholder.unparsed("election", election.getName()))
                        );
                        return;
                    }

                    electionParties.add(party);
                    sender.audience().sendMessage(Messages.component(true,
                            "election.party-added",
                            Placeholder.unparsed("party", partyName),
                            Placeholder.unparsed("election", election.getName()))
                    );

                    Election newElection = new Election(election.getName(), electionParties, election.isOpen());
                    plugin.getManager().updateElection(newElection);
                });
    }
}
