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
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.ConfigurationNode;

public class Messages {
    private static final String NOT_FOUND = "<red>String not found in messages.yml";
    private static MiniMessage miniMessage;
    private static ConfigurationNode config;
    private static String prefix;

    public static void init(ConfigurationNode config) {
        Messages.config = config;
        Messages.miniMessage = MiniMessage.miniMessage();
        Messages.prefix = config.node("prefix").getString("");
    }

    public static void close() {
        if (BukkitAudienceManager.initialized())
            BukkitAudienceManager.shutdown();
    }

    public static Component component(boolean prefix, String path, TagResolver... placeholders) {
        ConfigurationNode node = config.node((Object[]) path.split("\\."));
        if (node.virtual()) {
            return miniMessage.deserialize(Messages.prefix + NOT_FOUND);
        }

        String message = node.getString("");
        if (prefix) {
            message = Messages.prefix + message;
        }

        return miniMessage.deserialize(message, placeholders);
    }

    public static String string(boolean prefix, String path, TagResolver... placeholders) {
        return Legacy.SERIALIZER.serialize(component(prefix, path, placeholders));
    }
}
