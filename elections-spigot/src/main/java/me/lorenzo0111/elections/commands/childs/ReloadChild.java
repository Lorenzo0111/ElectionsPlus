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

package me.lorenzo0111.elections.commands.childs;

import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import me.lorenzo0111.pluginslib.command.annotations.Permission;

import java.util.Map;

import org.spongepowered.configurate.ConfigurateException;

public class ReloadChild extends SubCommand {

    public ReloadChild(Command command) {
        super(command);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Permission("elections.reload")
    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        long start = System.currentTimeMillis();
        int errors = 0;

        try {
            ElectionsPlus.getInstance()
                    .reload();
        } catch (ConfigurateException e) {
            e.printStackTrace();
            errors++;
        }

        long elapsedMs = System.currentTimeMillis() - start;
        Map<String, String> placeholders = Messages.multiple("elapsed", String.valueOf(elapsedMs), "errors", String.valueOf(errors));
        
        Messages.send(sender.audience(), true, placeholders, "plugin", "reload");
    }
}
