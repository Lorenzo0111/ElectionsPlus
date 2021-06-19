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

import com.google.gson.Gson;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.pluginslib.database.DatabaseSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Election implements DatabaseSerializable {
    private final String name;
    private final List<Party> parties;
    private boolean open;

    public Election(String name, List<Party> parties, boolean open) {
        this.name = name;
        this.parties = parties;
        this.open = open;
    }

    public String getName() {
        return name;
    }

    public List<Party> getParties() {
        return parties;
    }

    public void close() {
        this.open = false;
        ElectionsPlus.getInstance()
                .getManager()
                .updateElection(this);
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public DatabaseSerializable from(Map<String, Object> keys) throws RuntimeException {
        throw new RuntimeException("You can't deserialize this class. You have to do that manually.");
    }

    @Override
    public @NotNull String tableName() {
        return "elections";
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        List<String> parties = new ArrayList<>();
        this.getParties().forEach(p -> parties.add(p.getName()));
        map.put("parties",new Gson().toJson(parties));
        map.put("open", isOpen() ? 1 : 0);
        return map;
    }
}
