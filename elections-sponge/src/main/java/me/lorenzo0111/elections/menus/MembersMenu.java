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
import me.lorenzo0111.elections.conversation.AddMemberConversation;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.conversation.ConversationUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.*;

public class MembersMenu {
    private final Party party;
    private final Player owner;
    private final ElectionsPlus plugin;
    private final List<UUID> added = new ArrayList<>();
    private final StateContainer state;
    private final Page.PageBuilder page;

    public MembersMenu(ElectionsPlus plugin, Party party, Player owner) {
        this.state = new StateContainer();
        this.page = GuiUtils.create(Messages.text(Messages.component(false, Placeholder.parsed("name", party.getName()), "guis", "members-title")));

        this.party = party;
        this.owner = owner;
        this.plugin = plugin;
    }

    public void setup() {
        page.setUpdatable(true);
        page.setUpdater((page) -> {
            page.getObserver().closeInventory();
            this.setup();
        });

        GuiUtils.element(state,page,22, ItemStack.builder()
                .itemType(ItemTypes.STONE_BUTTON)
                .add(Keys.DISPLAY_NAME, Messages.text("guis","add-member"))
                .build(),
                ActionType.CLOSE, (e) -> ConversationUtil.startConversation(e.getObserver(), new AddMemberConversation(plugin,party)));

        for (UUID uuid : party.getMembers()) {
            Optional<Player> player = Sponge.getServer().getPlayer(uuid);
            if (!player.isPresent())
                continue;
            if (added.contains(uuid))
                continue;
            added.add(uuid);

            ItemStack.Builder item = ItemStack.builder()
                    .itemType(ItemTypes.SKULL)
                    .add(Keys.DISPLAY_NAME,Text.of("§9" + player.get().getName()))
                    .add(Keys.ITEM_LORE, Collections.singletonList(Messages.text("guis", "kick-member")))
                    .add(Keys.REPRESENTED_PLAYER, player.get().getProfile());

            GuiUtils.element(state,page,item.build(), ActionType.REFRESH, e -> party.removeMember(player.get().getUniqueId()));
        }

        state.setInitialState(page.build("page"));
        state.launchFor(owner);
    }

}
