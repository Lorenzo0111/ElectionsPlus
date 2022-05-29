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

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.UIRunnable;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.handlers.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AddPartyMenu {
    private final List<Party> parties;
    private final Player owner;
    private final ElectionsPlus plugin;
    private final CreateElectionMenu menu;
    private final List<Party> added = new ArrayList<>();
    private final StateContainer state;
    private final Page.PageBuilder page;

    public AddPartyMenu(ElectionsPlus plugin, CreateElectionMenu menu, List<Party> party, Player owner) {
        this.state = new StateContainer();
        this.page = GuiUtils.create(Messages.text(Messages.component(false, Placeholder.parsed("name", menu.getName()), "guis", "add-party")));

        this.menu = menu;
        this.parties = party;
        this.owner = owner;
        this.plugin = plugin;
    }

    public AddPartyMenu(ElectionsPlus plugin, CreateElectionMenu menu, List<Party> party, Player owner, List<Party> alreadyAdded) {
        this(plugin,menu,party,owner);

        this.added.addAll(alreadyAdded);
    }

    public void setup() {
        ElectionsPlus.getInstance().getScheduler().sync(() -> {
            page.setUpdatable(true);
            /* page.setUpdater((page) -> {
                page.getElements().forEach((slot,item) -> {
                    String name = ChatColor.stripColor(item.getItem()
                            .get(Keys.DISPLAY_NAME)
                            .orElse(Text.EMPTY).toString());

                    parties.stream()
                            .filter((p) -> p.getName().equals(name))
                            .findFirst()
                            .ifPresent(item.getItem().offer(Keys.DISPLAY_NAME, Text.of()));
                });
            }); */

            GuiUtils.element(state,page,ItemStack.builder().itemType(ItemTypes.EMERALD_BLOCK).add(Keys.DISPLAY_NAME, Messages.text( "guis", "save")).build(), ActionType.CLOSE, (event) -> {
                menu.getParties().addAll(added);
                menu.setup();
            });

            for (Party party : parties) {
                List<Text> lore = new ArrayList<>();
                lore.add(Messages.text("guis", "add"));
                lore.add(Messages.text("guis", "remove"));

                ItemStack.Builder item = ItemStack.builder()
                        .itemType(ItemTypes.SKULL)
                        .add(Keys.REPRESENTED_PLAYER, Sponge.getServer().getPlayer(party.getOwner()).get().getProfile())
                        .add(Keys.DISPLAY_NAME, Text.of("§9" + party.getName()))
                        .add(Keys.ITEM_LORE, lore);

                if (party.getIcon() != null) {
                    GuiUtils.texture(item,party.getIcon());
                }

                GuiUtils.element(state,page,item.build(), ActionType.CLOSE,createAddAction(party,item));
            }

            state.addState(page.build("page"));
            state.launchFor(owner);
        });
    }

    public UIRunnable createAddAction(Party party, ItemStack.Builder item) {
        return (e -> {
            if (added.contains(party)) {
                added.remove(party);
                item.add(Keys.DISPLAY_NAME, Text.of("§9" + party.getName()));
                return;
            }

            added.add(party);
            item.add(Keys.DISPLAY_NAME,Text.of("§9" + party.getName() + Messages.get("guis","added")));
        });
    }
}
