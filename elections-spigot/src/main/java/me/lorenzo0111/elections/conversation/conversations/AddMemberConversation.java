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
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.Conversation;
import me.lorenzo0111.elections.handlers.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class AddMemberConversation extends Conversation {
    private final Party party;

    public AddMemberConversation(Party party, Player author, ElectionsPlus plugin) {
        super(Messages.get("conversations", "add"), author, plugin);

        this.party = party;
    }

    @Override
    public void handle(@Nullable String input) {
        /* XXX(tadhunt) - when handle() is called during conversation processing, no sendMessage() calls work...
        this.getPlugin().getLogger().info("handling AddMemberConversation input: " + input);
        this.getPlugin().getLogger().info("author is: " + this.getAuthor().getUniqueId().toString());
        this.getAuthor().sendMessage("what's going on already?");
        */

        if (input == null)
            return;

        Player player = Bukkit.getPlayer(input);
        if (player == null) {
            this.getAuthor().sendMessage(Messages.componentString(true, Messages.single("name", input), "errors", "user-not-online"));
            return;
        }

        party.addMember(player.getUniqueId());
        this.getAuthor().sendMessage(Messages.componentString(true, Messages.multiple("name", player.getName(), "party", party.getName()), "parties", "user-added"));
    }
}
