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

package me.lorenzo0111.elections.conversation;

import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.ChatColor;
import me.lorenzo0111.pluginslib.conversation.Conversation;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class CreatePartyConversation implements Conversation {
    private final ElectionsPlus plugin;

    public CreatePartyConversation(ElectionsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(Player player, @Nullable Text input) {
        if (input == null)
            return;

        ElectionsPlus.getInstance()
                .getManager()
                .createParty(input.toPlain(), player.getUniqueId())
                .thenAccept((party) -> {
                    if (party == null) {
                        player.sendMessage(Text.of(ChatColor.translateAlternateColorCodes('&', ElectionsPlus.getInstance().config("prefix") + "<red>A party with that name already exist.")));
                        return;
                    }

                    player.sendMessage(Text.of(ChatColor.translateAlternateColorCodes('&', ElectionsPlus.getInstance().config("prefix") + "<gray>Party created.")));
                });
    }

    @Override
    public Component reason() {
        return Messages.component(true,"conversations", "create");
    }

    @Override
    public @Nullable Text escape() {
        return Text.of(plugin.config("escape"));
    }
}
