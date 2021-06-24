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
import me.lorenzo0111.pluginslib.audience.User;
import me.lorenzo0111.pluginslib.command.Command;
import me.lorenzo0111.pluginslib.command.SubCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

public class HelpChild extends SubCommand {

    public HelpChild(Command command) {
        super(command);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void handleSubcommand(User<?> sender, String[] args) {
        sender.audience().sendMessage(Component.text(ChatColor.translateAlternateColorCodes('&', ((ElectionsPlus) getCommand().getPlugin()).config("prefix") + "&7Available commands:")));
        sender.audience().sendMessage(this.formatHelp("create <name>", "Create an election"));
        if (sender.hasPermission("elections.disband"))
            sender.audience().sendMessage(this.formatHelp("disband <name>", "Disband a party"));
        if (sender.hasPermission("elections.info"))
            sender.audience().sendMessage(this.formatHelp("info <name>", "Get votes of an election"));
        if (sender.hasPermission("elections.close"))
            sender.audience().sendMessage(this.formatHelp("close <name>", "Close an election"));
        sender.audience().sendMessage(this.formatHelp("list", "View the list of elections"));
        sender.audience().sendMessage(this.formatHelp("parties", "View the list of parties"));
        sender.audience().sendMessage(this.formatHelp("vote [name]", "Vote to an election"));
    }

    public Component formatHelp(String command,String description) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', ((ElectionsPlus) getCommand().getPlugin()).config("prefix") + String.format("&9/elections %s &8» &7%s", command, description)));
    }
}
