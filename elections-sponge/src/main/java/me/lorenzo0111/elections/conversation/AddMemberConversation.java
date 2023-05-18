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
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.ChatColor;
import me.lorenzo0111.pluginslib.conversation.Conversation;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

public class AddMemberConversation implements Conversation {
    private final ElectionsPlus plugin;
    private final Party party;

    public AddMemberConversation(ElectionsPlus plugin, Party party) {
        this.plugin = plugin;
        this.party = party;
    }


    @Override
    public void handle(Player player, @Nullable Text input) {
        if (input == null || input.isEmpty())
            return;

        String text = TextSerializers.FORMATTING_CODE.serialize(input);

        Optional<Player> target = Sponge.getServer().getPlayer(text);
        if (!target.isPresent()) {
            player.sendMessage(Text.of(ChatColor.translateAlternateColorCodes('&', plugin.config("prefix") + "&7This user is not online.")));
            return;
        }

        party.addMember(target.get().getUniqueId());
        player.sendMessage(Text.of(ChatColor.translateAlternateColorCodes('&', plugin.config("prefix") + "&7Member added to the party.")));
    }

    @Override
    public Component reason() {
        return Messages.component(true,"conversations", "add");
    }

    @Override
    public @Nullable Text escape() {
        return Text.of(plugin.config("escape"));
    }
}
