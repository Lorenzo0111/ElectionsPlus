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
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

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
    
    private static Party findParty(List<Party> parties, String name) {
        for (Party party : parties) {
            if (party.getName().equals(name)) {
                return party;
            }
        }
        return null;
    }

    @Permission("elections.create")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        if (!(sender.player() instanceof Player)) {
            Messages.send(sender.audience(), true, "errors", "console");
            return;
        }

        ArrayList<String> a = plugin.unquote(args, 1);

        if (a.size() < 2) {
           Messages.send(sender.audience(), true, "errors", "bad-args");
           return;
        }

        plugin.getManager()
           .getElections()
           .thenAccept((elections) -> handleElections(sender, a, elections));
        }
        
        private void handleElections(User<?> sender, ArrayList<String> args, List<Election> elections) {
            String electionName = args.remove(0);
            
            for(Election election : elections) {
                if (election.getName().equals(electionName)) {
                    electionAddParties(election, sender, args);
                    return;
                }
            }
            
            Messages.send(sender.audience(), true, Messages.single("name", electionName), "errors", "election-not-found");
        }
        
    private void electionAddParties(Election election, User<?> sender, ArrayList<String> args) {
        plugin.getManager()
            .getParties()
            .thenAccept((parties) -> {
                Boolean dirty = false;
                List<Party> electionParties = election.getParties();
                
                for (String partyName : args) {
                    Party party = findParty(parties, partyName);
                    if (party == null) {
                        Messages.send(sender.audience(), true, Messages.single("party", partyName), "errors", "party-not-found");
                        return;
                    }
                    
                    if (electionParties.contains(party)) {
                        continue;
                    }
                    
                    electionParties.add(party);
                    dirty = true;
                    Messages.send(sender.audience(), true, Messages.multiple("party", partyName, "election", election.getName()), "election", "party-added");
                }
                
                if (dirty) {
                    Election newElection = new Election(election.getName(), electionParties, election.isOpen());
                    plugin.getManager().updateElection(newElection);
                } else {
                    Messages.send(sender.audience(), true, Messages.single("election", election.getName()), "election", "nochange");
                }
            });
    }
}
