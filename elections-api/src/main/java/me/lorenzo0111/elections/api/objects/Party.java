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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.lorenzo0111.elections.constants.Getters;
import me.lorenzo0111.pluginslib.database.DatabaseSerializable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

public class Party implements DatabaseSerializable {
    private final String name;
    private String icon;
    private UUID owner;
    private final List<UUID> members;

    public Party(String name, UUID owner, List<UUID> members) {
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    public Party(String name, UUID owner) {
        this(name,owner,new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public void addMember(UUID uuid) {
        if (!this.members.contains(uuid))
            this.members.add(uuid);

        Getters.database()
                .updateParty(this);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);

        Getters.database()
                .updateParty(this);
    }

    public List<UUID> getMembers() {
        return members;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.members.remove(owner);
        Getters.database()
                .updateParty(this);
    }

    public void setIcon(String icon) {
        this.icon = icon;
        Getters.database()
                .updateParty(this);
    }

    public void setIconWithoutUpdate(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public DatabaseSerializable from(Map<String, Object> keys) {
        String name = (String) keys.get("name");
        String icon = (String) keys.get("icon");
        UUID owner = UUID.fromString((String) keys.get("owner"));

        Type type = new TypeToken<ArrayList<UUID>>() {}.getType();
        List<UUID> members = new Gson().fromJson((String) keys.get("members"),type);

        Party party = new Party(name, owner, members);
        party.setIconWithoutUpdate(icon);
        
        return party;
    }

    @Override
    public @NotNull String tableName() {
        return "parties";
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("owner",owner);
        map.put("members", new Gson().toJson(members));
        if (icon != null)
            map.put("icon", icon);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return Objects.equals(name, party.name) && Objects.equals(icon, party.icon) && Objects.equals(owner, party.owner) && Objects.equals(members, party.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, icon, owner, members);
    }

    @Override
    public String toString() {
        return "Party{" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", owner=" + owner +
                ", members=" + members +
                '}';
    }
}
