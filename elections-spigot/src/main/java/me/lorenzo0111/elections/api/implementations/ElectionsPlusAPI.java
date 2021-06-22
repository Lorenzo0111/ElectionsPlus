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

package me.lorenzo0111.elections.api.implementations;

import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.IElectionsPlusAPI;
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.api.objects.Vote;
import me.lorenzo0111.elections.cache.CacheManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ElectionsPlusAPI implements IElectionsPlusAPI {
    private final ElectionsPlus plugin;

    public ElectionsPlusAPI(ElectionsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public CacheManager getCache() {
        return plugin.getCache();
    }

    @Override
    public CompletableFuture<List<Vote>> getVotes() {
        return plugin.getManager()
                .getVotes();
    }

    @Override
    public CompletableFuture<Vote> getVote(UUID player, String election, String party) {
        CompletableFuture<Vote> voteFuture = new CompletableFuture<>();

        plugin.getManager()
                .getVotes()
                .thenAccept(v -> {
                    Vote vote = v.stream()
                            .filter(f -> f.getPlayer().equals(player))
                            .filter(f -> f.getElection().equals(election))
                            .filter(f -> f.getParty().equals(party))
                            .findFirst()
                            .orElse(null);

                    voteFuture.complete(vote);
                });

        return voteFuture;
    }

    @Override
    public CompletableFuture<Party> getParty(String name) {
        CompletableFuture<Party> future = new CompletableFuture<>();

        plugin.getManager()
                .getParties()
                .thenAccept(p -> {
                    Party party = p.stream()
                            .filter(f -> f.getName().equals(name))
                            .findFirst()
                            .orElse(null);

                    future.complete(party);
                });

        return future;
    }

    @Override
    public CompletableFuture<Election> getElection(String name) {
        CompletableFuture<Election> future = new CompletableFuture<>();

        this.getElections()
                .thenAccept((l) -> future.complete(l.stream()
                        .filter(election -> election.getName().equals(name))
                        .findFirst()
                        .orElse(null)));

        return future;
    }

    @Override
    public CompletableFuture<List<Election>> getElections() {
        return plugin.getManager()
                .getElections();
    }

    @Override
    public CompletableFuture<Boolean> addVote(UUID player, Election election, Party party) {
        return plugin.getManager()
                .vote(player,party,election);
    }

    @Override
    public CompletableFuture<Boolean> addVote(Vote vote) {
        return plugin.getManager()
                .vote(vote);
    }
}
