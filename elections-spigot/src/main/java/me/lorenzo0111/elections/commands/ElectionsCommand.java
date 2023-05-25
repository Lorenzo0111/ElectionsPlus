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
import me.lorenzo0111.pluginslib.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ElectionsCommand extends Command implements TabExecutor {

    public ElectionsCommand(ElectionsPlus plugin, String command, @Nullable Customization customization) {
        super(plugin, command, customization);

        this.addSubcommand(new CreateChild(this, plugin));
        this.addSubcommand(new AddPartyToElectionChild(this, plugin));
        this.addSubcommand(new PartiesChild(this, plugin));
        this.addSubcommand(new ListChild(this));
        this.addSubcommand(new DisbandChild(this, plugin));
        this.addSubcommand(new HelpChild(this));
        this.addSubcommand(new VoteChild(this, plugin));
        this.addSubcommand(new ReloadChild(this));
        this.addSubcommand(new InfoChild(this));
        this.addSubcommand(new CloseChild(this, plugin));
        this.addSubcommand(new ProceedChild(this, plugin));
        this.addSubcommand(new CreateVoteBlockChild(this, plugin));

        Objects.requireNonNull(plugin.getCommand(command)).setTabCompleter(this);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            this.subcommands.forEach((s) -> list.add(s.getName()));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create"))
            list.add("[name]");

        return list;
    }

    public List<SubCommand> getSubCommands() {
        return subcommands;
    }
}
