/*
 * This file is part of ElectionsPlus, licensed under the MIT License.
 *
 * Copyright (©) tadhunt
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
 
 public class ElectionBlock implements DatabaseSerializable {
    private final UUID world;
    private final Map<String, Object> location;
    private final String blockData;

    public ElectionBlock(UUID world, Map<String, Object> location, String blockData) {
        this.world = world;
        this.location = location;
        this.blockData = blockData;
    }

    @Override
    public DatabaseSerializable from(Map<String, Object> keys) throws RuntimeException {
        throw new RuntimeException("You can't deserialize this class. You have to do that manually.");
    }

    @Override
    public @NotNull String tableName() {
        return "blocks";
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String,Object> map = new HashMap<>();
        map.put("world", world);
        map.put("location", location);
        map.put("blockdata", blockData);
        return map;
    }

    public UUID getWorld() {
        return this.world;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public String getBlockData() {
        return blockData;
    }
 }
 