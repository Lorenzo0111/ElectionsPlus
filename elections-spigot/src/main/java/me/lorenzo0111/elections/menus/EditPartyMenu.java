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
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.ConversationUtil;
import me.lorenzo0111.elections.conversation.conversations.IconConversation;
import me.lorenzo0111.elections.handlers.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Objects;

public class EditPartyMenu extends BaseGui {
    private final Player owner;
    private final Party party;
    private final SkullBuilder item;
    private final ElectionsPlus plugin;

    public EditPartyMenu(Player owner, Party party, SkullBuilder item, ElectionsPlus plugin) {
        super(5, Messages.componentString(false, "guis", "edit-party-title"), EnumSet.noneOf(InteractionModifier.class));

        this.owner = owner;
        this.party = party;
        this.item = item;
        this.plugin = plugin;
    }

    public void setup() {
        this.setDefaultClickAction((e) -> e.setCancelled(true));

        this.setItem(2, 5, item.lore(Component.empty()).asGuiItem());
        this.setItem(4,3, ItemBuilder.from(Material.BARRIER).name(Messages.component(false, "guis", "delete")).lore(Messages.component(false, "guis", "delete-party-lore")).asGuiItem(e -> {
            e.getWhoClicked().closeInventory();
            if (party.getOwner().equals(owner.getUniqueId())) {
                plugin.getManager()
                        .deleteParty(party);
                Messages.send(e.getWhoClicked(),true,"party-deleted");
                return;
            }

            Messages.send(e.getWhoClicked(),true,"no-permission-delete");
        }));
        this.setItem(4,5, ItemBuilder
                .from(Objects.requireNonNull(XMaterial.OAK_SIGN.parseItem()))
                .name(Messages.component(false, "guis", "members"))
                .lore(Messages.component(false,"guis","members-lore"), Messages.component(false, "guis", "refresh"))
                .asGuiItem(e -> new MembersMenu(plugin,party,owner).setup()));

        GuiItem item;

        if (owner.hasPermission("elections.party.icon")) {
            item = ItemBuilder.from(Material.EMERALD)
                    .name(Messages.component(false, "guis", "icon"))
                    .lore(Messages.component(false, "guis", "icon-lore"), Messages.component(false, "guis", "icon-lore2"))
                    .asGuiItem(e -> {
                    e.getWhoClicked().closeInventory();
                    ConversationUtil.createConversation(plugin,new IconConversation(this,party,owner,plugin));
                });
        } else {
            item = ItemBuilder.from(Material.BARRIER)
                    .name(Messages.component(false, "guis", "no-icon"))
                    .lore(Messages.component(false, "guis", "no-icon-description"), Messages.component(false, "guis", "no-icon-description2"))
                    .asGuiItem();
        }

        this.setItem(4, 7, item);

        this.open(owner);
    }

}
