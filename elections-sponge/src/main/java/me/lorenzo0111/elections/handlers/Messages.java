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

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Messages {
    private static ConfigurationNode config;
    private static String prefix;
    private static final String NOT_FOUND = "&cString not found in the messages.yml file.";
    @Inject private static SpongeAudiences adventure;

    public static void init(ConfigurationNode config, String prefix) {
        Messages.config = config;
        Messages.prefix = prefix;
    }

    public static String prefix() { return prefix; }
    public static ConfigurationNode config() { return config; }

    public static Map<String,String> keys(String... keys) {
        Map<String,String> map = new HashMap<>();
        Arrays.asList(keys).forEach(k -> map.put(k,get(k)));
        return map;
    }

    public static Text text(Object... path) {
        return text(component(false,path));
    }

    public static Text text(Component component) {
        return Text.of(LegacyComponentSerializer.builder()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build().serialize(component));
    }


    public static Component component(boolean prefix, Object... path) {
        return component(prefix,new HashMap<>(),path);
    }

    public static Component component(boolean prefix, TagResolver placeholders, Object... path) {
        String p = prefix ? prefix() : "";

        return MiniMessage.miniMessage().deserialize(ChatColor.translateAlternateColorCodes('&', p + config.node(path).getString(NOT_FOUND)), placeholders);
    }

    public static String get(Object... path) {
        return ChatColor.translateAlternateColorCodes('&', config.node(path).getString(NOT_FOUND));
    }

    public static void send(Audience player, boolean prefix, Object... path) {
        send(player, prefix , new HashMap<>(),path);
    }

    public static void send(Audience player, boolean prefix, Map<String,String> placeholders, Object... path) {
        player.sendMessage(component(prefix,placeholders,path));
    }

    public static Audience audience(Player player) {
        return adventure.player(player);
    }
}
