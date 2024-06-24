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
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.config.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class AddPartyMenu extends PaginatedGui {
    private final List<Party> parties;
    private final Player owner;
    private final ElectionsPlus plugin;
    private final CreateElectionMenu menu;
    private final List<Party> added = new ArrayList<>();

    public AddPartyMenu(ElectionsPlus plugin, CreateElectionMenu menu, List<Party> party, Player owner) {
        super(3, 0, Messages.string(false, "guis.add-party",
                        Placeholder.unparsed("name", menu.getName())),
                new HashSet<>());

        this.menu = menu;
        this.parties = party;
        this.owner = owner;
        this.plugin = plugin;
    }

    public AddPartyMenu(ElectionsPlus plugin, CreateElectionMenu menu, List<Party> party, Player owner, List<Party> alreadyAdded) {
        this(plugin, menu, party, owner);

        this.added.addAll(alreadyAdded);
    }

    public void setup() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.setDefaultClickAction(e -> e.setCancelled(true));
            this.setItem(3, 3, ItemBuilder.from(Material.ARROW)
                    .name(Messages.component(false, "guis.back"))
                    .asGuiItem(e -> this.previous()));
            this.setItem(3, 7, ItemBuilder.from(Material.ARROW)
                    .name(Messages.component(false, "guis.next"))
                    .asGuiItem(e -> this.next()));
            this.setItem(3, 5, ItemBuilder.from(Objects.requireNonNull(XMaterial.EMERALD_BLOCK.parseItem()))
                    .name(Messages.component(false, "guis.save"))
                    .asGuiItem(e -> {
                e.getWhoClicked().closeInventory();
                menu.getParties().addAll(added);
                menu.setup();
            }));
            this.getFiller().fillBottom(ItemBuilder.from(Objects.requireNonNull(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())).asGuiItem());

            for (Party party : parties) {
                SkullBuilder item = ItemBuilder.skull()
                        .owner(Bukkit.getOfflinePlayer(party.getOwner()))
                        .name(Component.text("§9" + party.getName()))
                        .lore(Messages.component(false, "guis.add"),
                                Messages.component(false, "guis.remove"));

                if (party.getIcon() != null) {
                    item.texture(party.getIcon());
                }

                this.addItem(item.asGuiItem(this.createAddAction(party, item)));
            }

            this.open(owner);
        });
    }

    public GuiAction<InventoryClickEvent> createAddAction(Party party, SkullBuilder item) {
        return (e -> {
            switch (e.getClick()) {
                case LEFT:
                    if (!added.contains(party))
                        added.add(party);
                    item.name(Messages.component(false, "guis.party-added", Placeholder.unparsed("name", party.getName())));
                    this.updatePageItem(e.getSlot(), item.asGuiItem(createAddAction(party, item)));
                    break;
                case RIGHT:
                    added.remove(party);
                    item.name(Component.text("§9" + party.getName()));
                    this.updatePageItem(e.getSlot(), item.asGuiItem(createAddAction(party, item)));
                    break;
                default:
                    break;
            }
        });
    }
}
