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
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.handlers.Messages;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.List;

public class ElectionsMenu {
    private final Player owner;
    private final List<Election> elections;
    private final ElectionsPlus plugin;
    private final StateContainer state;
    private final Page.PageBuilder page;

    public ElectionsMenu(Player owner, List<Election> elections, ElectionsPlus plugin) {
        this.state = new StateContainer();
        this.page = GuiUtils.create(Messages.text("guis", "elections"));

        this.owner = owner;
        this.elections = elections;
        this.plugin = plugin;
    }

    public void setup() {
        plugin.getScheduler().sync(() -> {
            for (Election election : elections) {
                GuiUtils.element(state,page, ItemStack.builder()
                        .itemType(ItemTypes.BANNER)
                        .add(Keys.BANNER_BASE_COLOR, DyeColors.YELLOW)
                        .add(Keys.DISPLAY_NAME, Text.of("§9" + election.getName()))
                        .add(Keys.ITEM_LORE, Arrays.asList(Messages.text(Messages.component(false, Messages.single("state", election.isOpen() ? Messages.get("open") : Messages.get("close")),"guis", "state")), election.isOpen() ? Messages.text("guis", "vote") : Text.EMPTY, getRightLore(election)))
                        .build(), ActionType.CLOSE, (e) -> {
                            if (e.getObserver().hasPermission("elections.edit")) {
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
                        });
            }

            state.addState(page.build("page"));
            state.launchFor(owner);
        });
    }


    public Text getRightLore(Election election) {
        if (!owner.hasPermission("elections.edit"))
            return Text.EMPTY;

        if (election.isOpen())
            return Messages.text("guis", "close-election");

        return Messages.text( "guis", "delete-election");
    }
}
