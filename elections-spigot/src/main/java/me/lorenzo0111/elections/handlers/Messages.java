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
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Messages {
    private static ConfigurationNode config;
    private static String prefix;
    private static final String NOT_FOUND = ": <red>String not found in messages.yml";

    public static void init(ConfigurationNode config, String prefix, JavaPlugin plugin) {
        Messages.config = config;

        Component p = component(false, "prefix");
        Messages.prefix = MiniMessage.miniMessage().serialize(p);
    }

    public static void close() {
        if (BukkitAudienceManager.initialized())
            BukkitAudienceManager.shutdown();
    }

    public static String prefix() { return prefix; }
    public static ConfigurationNode config() { return config; }

    public static Map<String,String> single(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static Map<String,String> multiple(String... path) {
        Map<String,String> map = new HashMap<>();

        String key = null;
        for (String s : path) {
            if (key == null) {
                key = s;
            } else {
                map.put(key, s);
                key = null;
            }
        }

        if (key != null) {
            map.put(key, "");
        }

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

    @SuppressWarnings("unchecked")
    private static Map<String, String> obj2strmap(Object o) {
        return (Map<String, String>)o;
    }

    public static Component component(boolean prefix, Object... path) {
        String p = prefix ? prefix() : "";

        Builder b = TagResolver.builder();
        boolean empty = true;
        String pathDebug = "";
        ArrayList<String> newPath = new ArrayList<>();

        for(Object o : path) {
            if (o instanceof String) {
                newPath.add((String)o);
                pathDebug = pathDebug + o + ".";
            } else if (o instanceof Map) {
                try {
                    Map<String, String> m = obj2strmap(o);
                    Set<String> keys = m.keySet();
                    for (String k : keys) {
                        String v = m.get(k);
                        b.tag(k, Tag.preProcessParsed(v));
                        empty = false;
                    }
                } catch(Exception e) {
                    return MiniMessage.miniMessage().deserialize(p + "A:" + pathDebug + ": " + e.toString());
                }
            } else if (o instanceof Object[]) {
                Object oa[] = (Object[])o;

                for(Object element : oa) {
                    if (element instanceof String) {
                        newPath.add((String)element);
                    } else if (element instanceof Map) {
                        try {
                            Map<String, String> m = obj2strmap(element);
                            Set<String> keys = m.keySet();
                            for (String k : keys) {
                                String v = m.get(k);
                                b.tag(k, Tag.preProcessParsed(v));
                                empty = false;
                            }
                        } catch(Exception e) {
                            return MiniMessage.miniMessage().deserialize(p + "B:" + pathDebug + ": " + e.toString());
                        }        
                    } else {
                        String t = element.getClass().getName();
                        String v = element.toString();
                        return MiniMessage.miniMessage().deserialize(p + "C:" + t + ", " + v);
                    }
                }
            } else {
                return MiniMessage.miniMessage().deserialize(p + "D:" + notfound(o));
            }
        }

        ConfigurationNode n = config.node(newPath);

        if (empty) {
            return MiniMessage.miniMessage().deserialize(p + n.getString("E:" + pathDebug));
        }

        TagResolver placeholders = b.build();

        return MiniMessage.miniMessage().deserialize(p + n.getString("F:" + pathDebug), placeholders);
    }

    public static Component component(boolean prefix, TagResolver placeholders, Object... path) {
        String p = prefix ? prefix() : "";

        return MiniMessage.miniMessage()
                .deserialize(p + config.node(path).getString(notfound(path)), placeholders);
    }

    public static String get(Object... path) {
        Component c = component(false, path);
        return MiniMessage.miniMessage().serialize(c);
    }

    public static void send(CommandSender sender, boolean prefix, Object... path) {
        send(BukkitAudienceManager.audience(sender), prefix, new HashMap<>(),path);
    }

    public static void send(CommandSender player, boolean prefix, Map<String,String> placeholders, Object... path) {
        send(BukkitAudienceManager.audience(player), prefix, placeholders, path);
    }

    public static void send(Audience player, boolean prefix, Object... path) {
        send(player, prefix , new HashMap<>(),path);
    }

    public static void send(Audience player, boolean prefix, Map<String,String> placeholders, Object... path) {
        player.sendMessage(component(prefix, placeholders, path));
    }

    public static Audience audience(Player player) {
        return BukkitAudienceManager.audience(player);
    }
}
