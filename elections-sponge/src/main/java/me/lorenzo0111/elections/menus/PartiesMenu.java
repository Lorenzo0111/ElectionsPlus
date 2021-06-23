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
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.CreatePartyConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.conversation.ConversationUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;

public class PartiesMenu {
    private final Player owner;
    private final List<Party> parties;
    private final ElectionsPlus plugin;
    private final StateContainer state;
    private final Page.PageBuilder page;

    public PartiesMenu(Player owner, List<Party> parties, ElectionsPlus plugin) {
        this.state = new StateContainer();
        this.page = GuiUtils.create(Messages.text("guis", "parties"));

        this.owner = owner;
        this.parties = parties;
        this.plugin = plugin;
    }

    public void setup() {
        ElectionsPlus.getInstance().getScheduler().sync(() -> {
            if (owner.hasPermission("elections.party.create")) {
                GuiUtils.element(state,page,35, ItemStack.builder()
                .itemType(ItemTypes.STONE_BUTTON)
                .add(Keys.DISPLAY_NAME, Messages.text("guis", "create-party"))
                .add(Keys.ITEM_LORE, Collections.singletonList(Messages.text("guis", "create-party-lore")))
                .build(), ActionType.CLOSE, (e) -> ConversationUtil.startConversation(owner,new CreatePartyConversation(plugin)));
            }

            for (Party party : parties) {
                ItemStack.Builder item = ItemStack.builder()
                        .itemType(ItemTypes.SKULL)
                        .add(Keys.DISPLAY_NAME, Text.of("§9" + party.getName()))
                        .add(Keys.ITEM_LORE, Collections.singletonList(canEdit(owner,party) ? Messages.text( "guis", "edit-party") : Messages.text( "guis", "no-edit-party")));

                item.add(Keys.REPRESENTED_PLAYER, Sponge.getServer().getPlayer(party.getOwner()).get().getProfile());

                if (party.getIcon() != null)
                    GuiUtils.texture(item,party.getIcon());

                GuiUtils.element(state,page,item.build(),ActionType.NORMAL, e -> new EditPartyMenu(owner,party,item,plugin).setup());
            }

            state.addState(page.build("page"));
            state.launchFor(owner);
        });
    }

    private boolean canEdit(Player player, Party party) {
        return player.getUniqueId().equals(party.getOwner());
    }

    public List<Party> getParties() {
        return parties;
    }

    public ElectionsPlus getPlugin() {
        return plugin;
    }
}
