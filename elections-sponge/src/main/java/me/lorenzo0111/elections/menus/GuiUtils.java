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
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.action.runnable.UIRunnable;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.codehusky.huskyui.states.element.Element;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public class GuiUtils {

    public static Page.PageBuilder create(Text title) {
        return Page.builder().setAutoPaging(true).setTitle(title);
    }

    public static Element build(StateContainer state, ItemStack item, ActionType type, UIRunnable runnable) {
        return new ActionableElement(new RunnableAction(state, type, "good", runnable), item);
    }

    public static void element(StateContainer state, Page.PageBuilder page, ItemStack item, ActionType type, UIRunnable runnable) {
        page.addElement(build(state,item,type,runnable));
    }

    public static void element(StateContainer state, Page.PageBuilder page, int slot, ItemStack item, ActionType type, UIRunnable runnable) {
        page.putElement(slot, build(state,item,type,runnable));
    }

    public static ItemStack.Builder texture(ItemStack.Builder builder, String texture) {
        final GameProfile profile = GameProfile.of(UUID.randomUUID(),null);
        profile.getPropertyMap().put("textures", ProfileProperty.of("textures", texture));
        builder.add(Keys.REPRESENTED_PLAYER, profile);
        return builder;
    }
}
