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

package me.lorenzo0111.elections.conversation.conversations;

import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.conversation.Conversation;
import me.lorenzo0111.elections.handlers.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CreatePartyConversation extends Conversation {

    public CreatePartyConversation(Player author, ElectionsPlus plugin) {
        super(Messages.componentString(false, "conversations", "create"), author, plugin);
    }

    @Override
    public void handle(@Nullable String input) {
        if (input == null)
            return;

        this.getPlugin()
                .getManager()
                .createParty(input, this.getAuthor().getUniqueId())
                .thenAccept((party) -> {
                    if (party == null) {
                        this.getAuthor().sendMessage(ChatColor.translateAlternateColorCodes('&', getPlugin().config("prefix") + "&cA party with that name already exists."));
                        return;
                    }

                    this.getAuthor().sendMessage(ChatColor.translateAlternateColorCodes('&', getPlugin().config("prefix") + "&7Party created."));
                });
    }

}
