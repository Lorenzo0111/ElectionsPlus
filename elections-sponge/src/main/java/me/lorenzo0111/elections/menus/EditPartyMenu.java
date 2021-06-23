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
import com.codehusky.huskyui.states.element.Element;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.conversation.IconConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.conversation.ConversationUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class EditPartyMenu {
    private final Player owner;
    private final Party party;
    private final ItemStack.Builder item;
    private final ElectionsPlus plugin;
    private final StateContainer state;
    private final Page.PageBuilder page;

    public EditPartyMenu(Player owner, Party party, ItemStack.Builder item, ElectionsPlus plugin) {
        this.state = new StateContainer();
        this.page = GuiUtils.create(Messages.text("guis", "edit-party-title"));

        this.owner = owner;
        this.party = party;
        this.item = item;
        this.plugin = plugin;
    }

    public void setup() {
        GuiUtils.element(state,page,13,item.add(Keys.ITEM_LORE, Collections.emptyList()).build(), ActionType.NORMAL, (e) -> {});

        GuiUtils.element(state,page,29, ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, Messages.text("guis", "delete"))
                .add(Keys.ITEM_LORE, Collections.singletonList(Messages.text("guis", "delete-party-lore")))
                .build(),
                ActionType.CLOSE,
                (event) -> {
                    if (party.getOwner().equals(owner.getUniqueId())) {
                        plugin.getManager()
                                .deleteParty(party);
                        Messages.send(Messages.audience(event.getObserver()),true,"party-deleted");
                        return;
                    }

                    Messages.send(Messages.audience(event.getObserver()),true,"no-permission-delete");
                });

        GuiUtils.element(state,page,31, ItemStack.builder()
                .itemType(ItemTypes.SIGN)
                .add(Keys.DISPLAY_NAME, Messages.text("guis", "members"))
                .add(Keys.ITEM_LORE, Arrays.asList(Messages.text("guis", "members-lore"), Messages.text("guis", "refresh")))
                .build(), ActionType.CLOSE, (e) -> new MembersMenu(plugin,party,e.getObserver()).setup());

        Element item;

        if (owner.hasPermission("elections.party.icon")) {
            item = GuiUtils.build(state, ItemStack.builder()
                    .itemType(ItemTypes.EMERALD)
                    .add(Keys.DISPLAY_NAME, Messages.text("guis", "icon"))
                    .add(Keys.ITEM_LORE, Arrays.asList(Messages.text("guis", "icon-lore"), Messages.text("guis", "icon-lore2")))
                    .build(), ActionType.CLOSE, (e) -> ConversationUtil.startConversation(owner,new IconConversation(this,party,plugin)));
        } else {
            item = GuiUtils.build(state, ItemStack.builder()
                    .itemType(ItemTypes.BARRIER)
                    .add(Keys.DISPLAY_NAME, Messages.text("guis", "no-icon"))
                    .add(Keys.ITEM_LORE, Arrays.asList(Messages.text("guis", "no-icon-description"), Messages.text("guis", "no-icon-description2")))
                    .build(), ActionType.CLOSE, (e) -> {});
        }

        page.putElement(33, item);

        state.setInitialState(page.build("page"));
        state.launchFor(owner);
    }

}
