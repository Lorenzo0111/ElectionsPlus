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
import me.lorenzo0111.elections.config.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.ICommand;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoChild extends SubCommand {

    public InfoChild(ICommand<?> command) {
        super(command);
    }

    @Override
    public String getName() {
        return "info";
    }

    @Permission("elections.info")
    @Override
    public void handleSubcommand(User<?> user, String[] args) {
        ElectionsPlus plugin = (ElectionsPlus) getCommand().getPlugin();

        if (args.length < 2) {
            user.audience().sendMessage(Messages.component(true, "errors.election-name-missing"));
            return;
        }

        user.audience().sendMessage(Messages.component(true, "votes.calculating", Placeholder.unparsed("election", args[1])));

        plugin.getApi()
                .getVotes()
                .thenAccept((votes) -> {
                    List<Vote> collect = votes.stream()
                            .filter(vote -> vote.getElection().equalsIgnoreCase(args[1]))
                            .collect(Collectors.toList());

                    int total = 0;
                    Map<String, Integer> voteMap = new HashMap<>();

                    for (Vote vote : collect) {
                        total++;

                        if (!voteMap.containsKey(vote.getParty())) {
                            voteMap.put(vote.getParty(), 1);
                            continue;
                        }

                        Integer voteCount = voteMap.get(vote.getParty());
                        voteMap.replace(vote.getParty(), voteCount, voteCount + 1);
                    }

                    user.audience().sendMessage(Messages.component(true, "votes.title"));
                    for (String party : voteMap.keySet()) {
                        int voteCount = voteMap.get(party);
                        int percent = voteCount * 100 / total;

                        user.audience().sendMessage(Messages.component(true,
                                "votes.status",
                                Placeholder.unparsed("party", party),
                                Formatter.number("votes", voteCount),
                                Formatter.number("percent", percent)
                        ));
                    }
                });
    }
}
