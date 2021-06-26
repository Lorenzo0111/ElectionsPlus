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

import me.lorenzo0111.elections.api.IElectionsPlusAPI;
import me.lorenzo0111.elections.api.implementations.ElectionsPlusAPI;
import me.lorenzo0111.elections.cache.CacheManager;
import me.lorenzo0111.elections.commands.ElectionsCommand;
import me.lorenzo0111.elections.constants.Getters;
import me.lorenzo0111.elections.database.DatabaseManager;
import me.lorenzo0111.elections.database.IDatabaseManager;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.listeners.JoinListener;
import me.lorenzo0111.elections.scheduler.BukkitScheduler;
import me.lorenzo0111.pluginslib.audience.BukkitAudienceManager;
import me.lorenzo0111.pluginslib.command.Customization;
import me.lorenzo0111.pluginslib.config.ConfigExtractor;
import me.lorenzo0111.pluginslib.database.connection.SQLiteConnection;
import me.lorenzo0111.pluginslib.dependency.slimjar.SlimJarDependencyManager;
import me.lorenzo0111.pluginslib.updater.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public final class ElectionsPlus extends JavaPlugin {
    private final CacheManager cache = new CacheManager();
    private boolean loaded;
    private IDatabaseManager manager;
    private static ElectionsPlus instance;
    private ElectionsPlusAPI api;

    private ConfigurationNode config;
    private ConfigurationNode messages;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        new Metrics(this,11735);
        Getters.updater(new UpdateChecker(new BukkitScheduler(this), this.getDescription().getVersion(), this.getName(),93463, "https://www.spigotmc.org/resources/93463/", null, null));
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

         Bukkit.getScheduler().cancelTasks(this);

         Messages.close();

         if (BukkitAudienceManager.initialized())
             BukkitAudienceManager.shutdown();
    }

    public void start() throws ConfigurateException {
        this.loaded = true;

        this.reload();

        GenericMain.init(getDataFolder().toPath());

        this.api = new ElectionsPlusAPI(this);
        Bukkit.getServicesManager().register(IElectionsPlusAPI.class,api,this, ServicePriority.Normal);
        Bukkit.getPluginManager().registerEvents(new JoinListener(),this);
        switch (getConfig().getString("database.type", "NULL").toUpperCase()) {
            case "SQLITE":
                try {
                    this.manager = new DatabaseManager(new BukkitScheduler(this),cache,config(),new SQLiteConnection(getDataFolder().toPath()));
                    Getters.database(manager);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
                break;
            case "MYSQL":
                try {
                    this.manager = new DatabaseManager(config,cache, getDataFolder().toPath(), new BukkitScheduler(this));
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

        Customization customization = new Customization(config("prefix") + "&7Running &eElections &7v" + this.getDescription().getVersion() + " by Lorenzo0111",config("prefix") + "&cCommand not found",config("prefix") + "&7Run /$cmd help for command help.");
        new ElectionsCommand(this,"elections",customization);
    }

    private void load() {
        try {
            this.getLogger().info("Loading libraries..");
            this.getLogger().info("Note: This might take a few minutes on first run.");

            SlimJarDependencyManager manager = new SlimJarDependencyManager(getName(),getDataFolder().toPath());
            long time = manager.build();
            this.getLogger().info("Loaded all libraries in " + time + "ms");
            this.start();
        } catch (ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() throws ConfigurateException {
        ConfigExtractor messagesExtractor = new ConfigExtractor(this.getClass(),this.getDataFolder(),"messages.yml");
        messagesExtractor.extract();
        this.messages = messagesExtractor.toConfigurate();

        ConfigExtractor configExtractor = new ConfigExtractor(this.getClass(),this.getDataFolder(),"config.yml");
        configExtractor.extract();
        this.config = configExtractor.toConfigurate();
        Messages.init(messages,config("prefix"),this);
    }

    public ConfigurationNode config() {
        return this.config;
    }

    public String config(Object... path) {
        return this.config().node(path).getString("");
    }

    public IDatabaseManager getManager() {
        return manager;
    }

    public static ElectionsPlus getInstance() {
        return instance;
    }

    public ElectionsPlusAPI getApi() {
        return api;
    }

    public UpdateChecker getUpdater() {
        return Getters.updater();
    }

    public CacheManager getCache() {
        return cache;
    }
}
