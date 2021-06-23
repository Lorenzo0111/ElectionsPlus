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
import me.lorenzo0111.elections.conversation.conversations.CreatePartyConversation;
import me.lorenzo0111.elections.handlers.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class PartiesMenu extends PaginatedGui {
    private final Player owner;
    private final List<Party> parties;
    private final ElectionsPlus plugin;

    public PartiesMenu(Player owner, List<Party> parties, ElectionsPlus plugin) {
        super(5, Messages.componentString(false, "guis", "parties"));

        this.owner = owner;
        this.parties = parties;
        this.plugin = plugin;
    }

    public void setup() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.setDefaultClickAction(e -> e.setCancelled(true));
            this.setItem(3,3, ItemBuilder.from(Material.ARROW).name(Messages.component(false,"guis", "back")).asGuiItem(e -> this.previous()));
            this.setItem(3,7, ItemBuilder.from(Material.ARROW).name(Messages.component(false,"guis", "next")).asGuiItem(e -> this.next()));

            if (owner.hasPermission("elections.party.create")) {
                this.setItem(5, 5, ItemBuilder.from(Objects.requireNonNull(XMaterial.STONE_BUTTON.parseItem()))
                        .name(Messages.component(false, "guis", "create-party"))
                        .lore(Messages.component(false, "guis", "create-party-lore"))
                        .asGuiItem(e -> {
                            e.getWhoClicked().closeInventory();
                            ConversationUtil.createConversation(plugin,new CreatePartyConversation(owner,plugin));
                        }));
            }

            this.getFiller().fillBottom(ItemBuilder.from(Objects.requireNonNull(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())).asGuiItem());

            for (Party party : parties) {
                SkullBuilder item = ItemBuilder.skull()
                        .name(Component.text("§9" + party.getName()))
                        .lore(canEdit(owner,party) ? Messages.component(false, "guis", "edit-party") : Messages.component(false, "guis", "no-edit-party"));

                item.owner(Bukkit.getOfflinePlayer(party.getOwner()));

                if (party.getIcon() != null)
                    item.texture(party.getIcon());

                this.addItem(item.asGuiItem(e -> new EditPartyMenu(owner,party,item,plugin).setup()));

            }

            this.open(owner);
        });
    }

    private boolean canEdit(Player player, Party party) {
        return player.getUniqueId().equals(party.getOwner());
    }

    public Player getOwner() {
        return owner;
    }

    public List<Party> getParties() {
        return parties;
    }

    public ElectionsPlus getPlugin() {
        return plugin;
    }
}
