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

package me.lorenzo0111.elections;

import me.lorenzo0111.elections.commands.ElectionsCommand;
import me.lorenzo0111.elections.database.DatabaseManager;
import me.lorenzo0111.elections.database.IDatabaseManager;
import me.lorenzo0111.pluginslib.command.Customization;
import me.lorenzo0111.pluginslib.dependency.beta.SlimJarDependencyManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public final class ElectionsPlus extends JavaPlugin {
    private boolean loaded;
    private IDatabaseManager manager;
    private static ElectionsPlus instance;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.getLogger().info("Loading dependencies..");
        this.load();
    }

    @Override
    public void onDisable() {
        if (!this.loaded) {
            this.getLogger().warning("Plugin is not initialized.");
        }

         try {
             this.getManager().closeConnection();
         } catch (SQLException e) {
             e.printStackTrace();
         }


    }

    public void start() {
        this.loaded = true;
        switch (getConfig().getString("database.type", "NULL").toUpperCase()) {
            case "SQLITE":
                try {
                    File file = new File(this.getDataFolder(), "database.db");

                    if (file.exists() || file.createNewFile()) {
                        this.manager = new DatabaseManager(this, "jdbc:sqlite:" + file.getAbsolutePath(), null, null);
                        break;
                    }

                    this.getLogger().warning("Unable to create the database file");

                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "MYSQL":
                try {
                    this.manager = new DatabaseManager(this);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "REDIS":
                this.getLogger().warning("The redis feature is not implemented yet");
                break;
            default:
                this.getLogger().severe("Invalid database type");
                break;
        }

        Customization customization = new Customization(getConfig("prefix") + "&7Running &eElections &7v" + this.getDescription().getVersion() + " by Lorenzo0111",getConfig("prefix") + "&cCommand not found",getConfig("prefix") + "&7Run /$cmd help for command help.");
        new ElectionsCommand(this,"elections",customization);
    }

    private void load() {
        try {
            this.getLogger().info("Loading libraries..");
            this.getLogger().info("Note: This might take a few minutes on first run.");

            SlimJarDependencyManager manager = new SlimJarDependencyManager(this);
            long time = manager.build();
            this.getLogger().info("Loaded all libraries in " + time + "ms");
            this.start();

        } catch (ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfig(String path) {
        return this.getConfig().getString(path);
    }

    public IDatabaseManager getManager() {
        return manager;
    }

    public static ElectionsPlus getInstance() {
        return instance;
    }
}
