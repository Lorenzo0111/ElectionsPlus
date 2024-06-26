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
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.config.Messages;
import me.lorenzo0111.pluginslib.audience.BukkitAudienceManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;

public class VoteMenu extends PaginatedGui {
    private final Player owner;
    private final Election election;

    public VoteMenu(Player owner, Election election) {
        super(3, 0, Messages.string(false, "vote.title", Placeholder.unparsed("name", election.getName())), new HashSet<>());

        this.owner = owner;
        this.election = election;
    }

    public void setup() {
        this.setDefaultClickAction(e -> e.setCancelled(true));
        this.getFiller().fillBorder(ItemBuilder.from(Objects.requireNonNull(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())).asGuiItem());
        this.setItem(3, 3, ItemBuilder.from(Material.ARROW)
                .name(Messages.component(false, "guis.back"))
                .asGuiItem(e -> this.previous()));
        this.setItem(3, 7, ItemBuilder.from(Material.ARROW)
                .name(Messages.component(false, "guis.next"))
                .asGuiItem(e -> this.next()));

        for (Party party : election.getParties()) {
            SkullBuilder builder = ItemBuilder.skull()
                    .name(Component.text("§9" + party.getName()))
                    .lore(Messages.component(false, "guis.vote"))
                    .owner(Bukkit.getOfflinePlayer(party.getOwner()));

            if (party.getIcon() != null) {
                builder.texture(party.getIcon());
            }

            this.addItem(builder.asGuiItem(e -> {
                this.close(e.getWhoClicked());
                ElectionsPlus.getInstance()
                        .getManager()
                        .vote(e.getWhoClicked().getUniqueId(), party, election)
                        .thenAccept((b) -> {
                            if (b) {
                                BukkitAudienceManager.audience(e.getWhoClicked()).sendMessage(
                                        Messages.component(true, "vote.success",
                                                Placeholder.unparsed("party", party.getName()),
                                                Placeholder.unparsed("election", election.getName()))
                                );
                                return;
                            }

                            BukkitAudienceManager.audience(e.getWhoClicked())
                                    .sendMessage(Messages.component(true, "vote.already"));
                        });
            }));
        }

        this.open(owner);
    }
}
