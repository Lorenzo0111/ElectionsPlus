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
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.ConversationUtil;
import me.lorenzo0111.elections.conversation.conversations.NameConversation;
import me.lorenzo0111.elections.handlers.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class CreateElectionMenu extends BaseGui {
    private String name;
    private final Player player;
    private final ElectionsPlus plugin;
    private final List<Party> parties = new ArrayList<>();

    public CreateElectionMenu(ElectionsPlus plugin, String name, Player player) {
        super(5, Messages.componentString(false,"guis", "create"), EnumSet.noneOf(InteractionModifier.class));

        this.name = name;
        this.player = player;
        this.plugin = plugin;

        this.setup();
    }

    public void setup() {
        this.setDefaultClickAction((e) -> e.setCancelled(true));

        GuiItem nameItem = ItemBuilder.from(Material.BOOK)
                .name(Messages.component(false,Messages.single("name", name), "guis", "current-name"))
                .lore(Messages.component(false,"guis", "edit-name"))
                .asGuiItem(e -> {
                    e.getWhoClicked().closeInventory();
                    ConversationUtil.createConversation(plugin,new NameConversation(player,plugin,this));
                });

        GuiItem close = ItemBuilder.from(Material.BARRIER)
                .name(Messages.component(false,"guis", "cancel"))
                .asGuiItem(e -> e.getWhoClicked().closeInventory());

        GuiItem save = ItemBuilder.from(Material.EMERALD_BLOCK)
                .name(Messages.component(false,"guis", "save"))
                .lore(Messages.component(false, "guis", "save-lore"))
                .asGuiItem(e -> {
                    e.getWhoClicked().closeInventory();
                    plugin.getManager()
                            .createElection(name, parties)
                            .thenAccept(election -> {
                                if (election == null) {
                                    Messages.send(player, true, "errors", "election-exists");
                                    return;
                                }

                                Messages.send(player, true, Messages.single("name", name), "election", "created");
                            });
                });


        this.setItem(5,5, close);
        this.setItem(5,6, nameItem);
        this.setItem(5,9, save);

        this.setItem(2,2, ItemBuilder.from(Objects.requireNonNull(XMaterial.STONE_BUTTON.parseItem()))
                .name(Messages.component(false, "guis","add-name"))
                .lore(Messages.component(false, "guis","add-lore"))
                .asGuiItem(e -> {
                    e.getWhoClicked().closeInventory();
                    Messages.send(e.getWhoClicked(), true, "loading");
                    plugin.getManager()
                            .getParties()
                            .thenAccept((parties1) -> new AddPartyMenu(plugin, this, parties1, (Player)e.getWhoClicked(), parties).setup());
                }));

        this.getFiller().fill(ItemBuilder.from(Objects.requireNonNull(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())).asGuiItem());

        this.open(player);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Party> getParties() {
        return parties;
    }
}
