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

package me.lorenzo0111.elections.menus;

import com.cryptomorin.xseries.XMaterial;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.ConversationUtil;
import me.lorenzo0111.elections.conversation.conversations.AddMemberConversation;
import me.lorenzo0111.elections.handlers.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MembersMenu extends PaginatedGui {
    private final Party party;
    private final Player owner;
    private final ElectionsPlus plugin;
    private final List<OfflinePlayer> added = new ArrayList<>();

    public MembersMenu(ElectionsPlus plugin, Party party, Player owner) {
        super(3, Messages.componentString(false, Messages.single("name",party.getName()), "guis", "members-title"));

        this.party = party;
        this.owner = owner;
        this.plugin = plugin;
    }

    public void setup() {
        this.setDefaultClickAction(e -> e.setCancelled(true));
        this.setItem(3,3, ItemBuilder.from(Material.ARROW).name(Messages.component(false,"guis", "back")).asGuiItem(e -> this.previous()));
        this.setItem(3,7, ItemBuilder.from(Material.ARROW).name(Messages.component(false,"guis", "next")).asGuiItem(e -> this.next()));
        this.setItem(3,5, ItemBuilder.from(Objects.requireNonNull(XMaterial.STONE_BUTTON.parseItem())).name(Messages.component(false, "guis", "add-member")).asGuiItem(e -> {
            e.getWhoClicked().closeInventory();
            ConversationUtil.createConversation(plugin,new AddMemberConversation(party,owner,plugin));
        }));
        this.getFiller().fillBottom(ItemBuilder.from(Objects.requireNonNull(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())).asGuiItem());

        for (UUID uuid : party.getMembers()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.getName() == null)
                continue;
            if (added.contains(player))
                continue;
            added.add(player);

            SkullBuilder item = ItemBuilder.skull()
                    .name(Component.text("§9" + player.getName()))
                    .lore(Messages.component(false, "guis", "kick-member"),Messages.component(false, "guis", "set-owner"))
                    .owner(player);

            this.addItem(item.asGuiItem(e -> {
                switch (e.getClick()) {
                    case LEFT:
                        party.removeMember(player.getUniqueId());
                        this.close(e.getWhoClicked());
                        break;
                    case RIGHT:
                        this.close(e.getWhoClicked());
                        party.setOwner(player.getUniqueId());
                        break;
                    default:
                        break;
                }

            }));
        }

        this.open(owner);
    }

}
