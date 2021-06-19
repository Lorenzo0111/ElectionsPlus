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

package me.lorenzo0111.elections.api.objects;

import me.lorenzo0111.pluginslib.database.DatabaseSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vote implements DatabaseSerializable {
    private final UUID player;
    private final String party;
    private final String election;

    public Vote(UUID player, String party, String election) {
        this.player = player;
        this.party = party;
        this.election = election;
    }

    public String getParty() {
        return party;
    }

    public String getElection() {
        return election;
    }

    public UUID getPlayer() {
        return player;
    }

    @Override
    public DatabaseSerializable from(Map<String, Object> keys) {
        UUID player = UUID.fromString((String) keys.get("uuid"));
        String party = (String) keys.get("party");
        String election = (String) keys.get("election");
        return new Vote(player,party,election);
    }

    @Override
    public @NotNull String tableName() {
        return "votes";
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("uuid",player);
        map.put("party",party);
        map.put("election",election);
        return map;
    }
}
