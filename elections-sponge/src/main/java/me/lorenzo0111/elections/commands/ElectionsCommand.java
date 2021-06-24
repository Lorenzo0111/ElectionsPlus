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

package me.lorenzo0111.elections.commands;

import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.commands.childs.*;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.Customization;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectionsCommand extends Command {
    public ElectionsCommand(Object plugin, String command, List<String> argsSchema, @Nullable Customization customization) {
        super(plugin, command, argsSchema, customization);

        this.addSubcommand(new CreateChild(this, (ElectionsPlus) plugin));
        this.addSubcommand(new PartiesChild(this, (ElectionsPlus) plugin));
        this.addSubcommand(new ListChild(this));
        this.addSubcommand(new DisbandChild(this));
        this.addSubcommand(new HelpChild(this));
        this.addSubcommand(new VoteChild(this));
        this.addSubcommand(new ReloadChild(this));
        this.addSubcommand(new CloseChild(this));
        this.addSubcommand(new InfoChild(this));
    }
}
