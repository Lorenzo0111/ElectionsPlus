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

package me.lorenzo0111.elections.handlers;

import dev.triumphteam.gui.components.util.Legacy;
import me.lorenzo0111.pluginslib.audience.BukkitAudienceManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Messages {
    private static ConfigurationNode config;
    private static String prefix;
    private static final String NOT_FOUND = ": <red>String not found in messages.yml";

    public static void init(ConfigurationNode config, String prefix, JavaPlugin plugin) {
        Messages.config = config;
        Messages.prefix = prefix;
    }

    public static void close() {
        if (BukkitAudienceManager.initialized())
            BukkitAudienceManager.shutdown();
    }

    public static String prefix() { return prefix; }
    public static ConfigurationNode config() { return config; }

    public static Map<String,String> single(String key, String value) {
        Map<String,String> map = new HashMap<>();
        map.put(key,ChatColor.translateAlternateColorCodes('&', value));
        return map;
    }

    public static Map<String,String> keys(String... keys) {
        Map<String,String> map = new HashMap<>();
        Arrays.asList(keys).forEach(k -> map.put(k,get(k)));
        return map;
    }

    public static String componentString(boolean prefix, Object... path) {
        return Legacy.SERIALIZER.serialize(component(prefix, path));
    }

    public static String componentString(boolean prefix, Map<String,String> placeholders, Object... path) {
        return Legacy.SERIALIZER.serialize(component(prefix, placeholders, path));
    }

    private static String notfound(Object... path) {
        String paths = "";
        for(Object o : path) {
            String t = o.getClass().getName();
            String v = o.toString();
            paths = paths + "[" + t + ":" + v + "]";
        }

        return paths + NOT_FOUND;
    }

    /*
    private static TagResolver mkPlaceholders(Object o) {
        if (!(o instanceof Map)) {
            return null;
        }

        Map<String, String> m = (Map<String,String>)o;
        Set<String> keys = m.keySet();

        Builder b = TagResolver.builder();
        for (String k : keys) {
            String v = m.get(k);
            b.tag(k, Tag.preProcessParsed(v));
        }

        return b.build();
    }
    */

    public static Component component(boolean prefix, Object... path) {
        String p = prefix ? prefix() : "";

        Builder b = TagResolver.builder();
        Boolean empty = true;
        ArrayList<String> newPath = new ArrayList<String>();

        for(Object o : path) {
            if (o instanceof String) {
                newPath.add((String)o);
            } else if (o instanceof Map) {
                Map<String, String> m = (Map<String,String>)o;
                Set<String> keys = m.keySet();
                for (String k : keys) {
                    String v = m.get(k);
                    b.tag(k, Tag.preProcessParsed(v));
                    empty = false;
                }
            } else if (o instanceof ArrayList) {
                return MiniMessage.miniMessage().deserialize(p + "arraylist");
            } else if (o instanceof Object[]) {
                Object oa[] = (Object[])o;
                String err = null;

                for(Object element : oa) {
                    String t = element.getClass().getName();
                    String v = element.toString();
                    err = "X:" + t + ", " + v;
                    break;
                }

                if (err != null) {
                    return MiniMessage.miniMessage().deserialize(p + err);
                }
            }
            else {
                return MiniMessage.miniMessage().deserialize(p + "U:" + notfound(o));
            }
        }

        ConfigurationNode n = config.node(newPath);

        if (empty) {
            return MiniMessage.miniMessage().deserialize(p + n.getString("x:" + n.toString() + ":" + notfound(path)));
        }

        TagResolver placeholders = b.build();

        return MiniMessage.miniMessage().deserialize(p + n.getString("x:" + n.toString() + ":" + notfound(path)), placeholders);
    }

    public static Component component(boolean prefix, TagResolver placeholders, Object... path) {
        String p = prefix ? prefix() : "";

        return MiniMessage.miniMessage()
                .deserialize(ChatColor.translateAlternateColorCodes(
                        '&', p + config.node(path).getString(notfound(path))), placeholders);
    }

    public static String get(Object... path) {
        return ChatColor.translateAlternateColorCodes('&', config.node(path).getString(notfound(path)));
    }

    public static void send(CommandSender sender, boolean prefix, Object... path) {
        send(BukkitAudienceManager.audience(sender), prefix , new HashMap<>(),path);
    }

    public static void send(CommandSender player, boolean prefix, Map<String,String> placeholders, Object... path) {
        send(BukkitAudienceManager.audience(player),prefix,placeholders,path);
    }

    public static void send(Audience player, boolean prefix, Object... path) {
        send(player, prefix , new HashMap<>(),path);
    }

    public static void send(Audience player, boolean prefix, Map<String,String> placeholders, Object... path) {
        player.sendMessage(component(prefix,placeholders,path));
    }

    public static Audience audience(Player player) {
        return BukkitAudienceManager.audience(player);
    }
}
