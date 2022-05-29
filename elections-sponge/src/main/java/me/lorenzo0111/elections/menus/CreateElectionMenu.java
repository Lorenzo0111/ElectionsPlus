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
import me.lorenzo0111.elections.conversation.NameConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.conversation.ConversationUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateElectionMenu {
    private String name;
    private final Player player;
    private final ElectionsPlus plugin;
    private final List<Party> parties = new ArrayList<>();
    private final StateContainer state;
    private final Page.PageBuilder page;

    public CreateElectionMenu(ElectionsPlus plugin, String name, Player player) {
        this.state = new StateContainer();
        this.page = GuiUtils.create(Messages.text(Messages.component(false,"guis", "create")));

        this.name = name;
        this.player = player;
        this.plugin = plugin;

        this.setup();
    }

    public void setup() {
        GuiUtils.element(state,page, ItemStack.builder().itemType(ItemTypes.BOOK)
                .add(Keys.DISPLAY_NAME, Messages.text(Messages.component(false, Placeholder.parsed("name", name),"guis","current-name")))
                .add(Keys.ITEM_LORE, Collections.singletonList(Messages.text("guis", "edit-name")))
                .build(), ActionType.CLOSE, (event) -> ConversationUtil.startConversation(player,new NameConversation(plugin,this)));

        GuiUtils.element(state,page, ItemStack.builder().itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, Messages.text("guis", "cancel"))
                .build(), ActionType.CLOSE, (event) -> {});

        GuiUtils.element(state,page, ItemStack.builder().itemType(ItemTypes.EMERALD_BLOCK)
                .add(Keys.DISPLAY_NAME, Messages.text("guis", "save"))
                .add(Keys.ITEM_LORE, Collections.singletonList(Messages.text("guis", "save-lore")))
                .build(), ActionType.CLOSE, (event) -> plugin.getManager()
                        .createElection(name,parties)
                        .thenAccept(election -> {
                            if (election == null) {
                                Messages.send(Messages.audience(player), true, "errors", "election-exists");
                                return;
                            }

                            Messages.send(Messages.audience(player),true,"election-created");
                        }));


/*        this.setItem(5,5, close);
        this.setItem(5,6, nameItem);
        this.setItem(5,9, save);*/

        GuiUtils.element(state,page, ItemStack.builder().itemType(ItemTypes.STONE_BUTTON)
                .add(Keys.DISPLAY_NAME, Messages.text("guis", "add-name"))
                .add(Keys.ITEM_LORE, Collections.singletonList(Messages.text("guis", "add-lore")))
                .build(), ActionType.CLOSE, (event) -> plugin.getManager()
                .createElection(name,parties)
                .thenAccept(election -> {
                    Messages.send(Messages.audience(player),true,"loading");
                    plugin.getManager()
                            .getParties()
                            .thenAccept((parties1) -> new AddPartyMenu(plugin,this,parties1,event.getObserver(),parties).setup());
                }));

        state.setInitialState(page.build("page"));
        state.launchFor(player);
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
