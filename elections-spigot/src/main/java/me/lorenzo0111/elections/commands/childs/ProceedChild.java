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
import me.lorenzo0111.elections.api.objects.Vote;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.ICommand;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProceedChild extends SubCommand {

    public ProceedChild(ICommand<?> command) {
        super(command);
    }

    @Override
    public String getName() {
        return "proceed";
    }

    @Permission("elections.proceed")
    @Override
    public void handleSubcommand(User<?> user, String[] args) {
        if (args.length != 2) {
            user.audience().sendMessage(Messages.component(true,"proceed", "missing-election-name"));
            return;
        }

        ElectionsPlus plugin = ElectionsPlus.getInstance();
        plugin.getManager()
                .getVotes()
                .thenAccept((tmpVotes) -> {
                    List<Vote> votes = tmpVotes
                            .stream()
                            .filter((vote) -> vote.getElection().equalsIgnoreCase(args[1]))
                            .collect(Collectors.toList());

                    Map<String,Integer> counts = new HashMap<>();
                    List<String> winners = new ArrayList<>();
                    int winnerVotes = -1;

                    for (Vote vote : votes) {
                        if (!counts.containsKey(vote.getParty())) {
                            counts.put(vote.getParty(),1);
                            continue;
                        }

                        Integer count = counts.get(vote.getParty());
                        counts.replace(vote.getParty(),count,count+1);
                    }

                    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                        if (winnerVotes < entry.getValue()) {
                            winners.clear();
                            winners.add(entry.getKey());
                            winnerVotes = entry.getValue();
                        }

                        else if (winnerVotes == entry.getValue()) {
                            winners.add(entry.getKey());
                        }
                    }

                    if (winners.isEmpty()) {
                        user.audience().sendMessage(Messages.component(true, "proceed", "no-winner"));
                        return;
                    }

                    if (winners.size() == 1) {
                        plugin.getApi()
                                .getParty(winners.get(0))
                                .thenAccept((winner) -> plugin.win(winner.getOwner()));

                        user.audience().sendMessage(Messages.component(true, Messages.multiple("party", winners.get(0), "election", args[1]), "proceed", "winner"));
                        return;
                    }

                    String parties = "";
                    for (String name : winners) {
                        if (parties == "") {
                            parties = name;
                        } else {
                            parties = parties + ", " + name;
                        }
                    }

                    if ((plugin.config("rank", "strategy").equalsIgnoreCase("both"))) {
                        for (String name : winners) {
                            plugin.getApi()
                                    .getParty(name)
                                    .thenAccept((winner) -> plugin.win(winner.getOwner()));
                        }

                        user.audience().sendMessage(Messages.component(true, Messages.multiple("parties", parties, "election", args[1]), "proceed", "multiple-winners"));
                        return;
                    }

                    user.audience().sendMessage(Messages.component(true, Messages.multiple("parties", parties, "election", args[1]), "proceed", "tie"));
                });
    }
}
