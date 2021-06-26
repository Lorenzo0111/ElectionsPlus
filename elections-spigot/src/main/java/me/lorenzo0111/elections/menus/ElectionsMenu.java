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
import dev.triumphteam.gui.guis.PaginatedGui;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.handlers.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class ElectionsMenu extends PaginatedGui {
    private final Player owner;
    private final List<Election> elections;
    private final ElectionsPlus plugin;

    public ElectionsMenu(Player owner, List<Election> elections, ElectionsPlus plugin) {
        super(3, 17, Messages.componentString(false,"guis","elections"), EnumSet.noneOf(InteractionModifier.class));

        this.owner = owner;
        this.elections = elections;
        this.plugin = plugin;
    }

    public void setup() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.setDefaultClickAction(e -> e.setCancelled(true));
            this.setItem(3,3, ItemBuilder.from(Material.ARROW).name(Messages.component(false,"guis", "back")).asGuiItem(e -> this.previous()));
            this.setItem(3,7, ItemBuilder.from(Material.ARROW).name(Messages.component(false,"guis", "next")).asGuiItem(e -> this.next()));
            this.getFiller().fillBorder(ItemBuilder.from(Objects.requireNonNull(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())).asGuiItem());

            for (Election election : elections) {
                this.addItem(ItemBuilder
                        .from(Objects.requireNonNull(XMaterial.YELLOW_BANNER.parseItem()))
                        .name(Component.text("§9" + election.getName()))
                        .lore(Messages.component(false, Messages.single("state", election.isOpen() ? Messages.get("open") : Messages.get("close")),"guis", "state"), election.isOpen() ? Messages.component(false, "guis", "vote") : Component.empty(), getRightLore(election))
                        .asGuiItem(e -> {
                            if (e.getWhoClicked().hasPermission("elections.edit") && e.getClick().equals(ClickType.RIGHT)) {
                                this.close(e.getWhoClicked());

                                if (election.isOpen()) {
                                    election.close();
                                    return;
                                }

                                plugin.getManager()
                                        .deleteElection(election);
                                return;
                            }

                            if (!election.isOpen())
                                return;

                            new VoteMenu(owner,election).setup();
                        }));
            }

            this.open(owner);
        });
    }


    public Component getRightLore(Election election) {
        if (!owner.hasPermission("elections.edit"))
            return Component.empty();

        if (election.isOpen())
            return Messages.component(false, "guis", "close-election");

        return Messages.component(false, "guis", "delete-election");
    }
}
