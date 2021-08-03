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

import com.google.inject.Inject;
import me.lorenzo0111.elections.api.IElectionsPlusAPI;
import me.lorenzo0111.elections.api.implementations.ElectionsPlusAPI;
import me.lorenzo0111.elections.cache.CacheManager;
import me.lorenzo0111.elections.commands.ElectionsCommand;
import me.lorenzo0111.elections.constants.Getters;
import me.lorenzo0111.elections.database.DatabaseManager;
import me.lorenzo0111.elections.database.IDatabaseManager;
import me.lorenzo0111.elections.handlers.Configs;
import me.lorenzo0111.elections.handlers.Messages;
import me.lorenzo0111.elections.listeners.JoinListener;
import me.lorenzo0111.elections.scheduler.IAdvancedScheduler;
import me.lorenzo0111.elections.scheduler.SpongeScheduler;
import me.lorenzo0111.pluginslib.command.Customization;
import me.lorenzo0111.pluginslib.conversation.ConversationUtil;
import me.lorenzo0111.pluginslib.database.connection.SQLiteConnection;
import me.lorenzo0111.pluginslib.dependency.DependencyManager;
import me.lorenzo0111.pluginslib.updater.UpdateChecker;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("unused")
@Plugin(id = "electionsplus", name = "ElectionsPlus", version = "1.2", authors = "Lorenzo0111")
public class ElectionsPlus {
    private final CacheManager cache;
    private final Path directory;
    @Inject
    private Logger logger;
    private static ElectionsPlus instance;
    private ConfigurationNode config;
    private ConfigurationNode messages;
    private IDatabaseManager manager;
    private IElectionsPlusAPI api;
    private final IAdvancedScheduler scheduler;

    @Inject
    public ElectionsPlus(@ConfigDir(sharedRoot = false) Path directory) {
        this.directory = directory;
        this.cache = new CacheManager();
        this.scheduler = new SpongeScheduler(this);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws ConfigurateException, SQLException {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin("electionsplus").orElse(null);
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        instance = this;

        // Load libraries
        try {
            logger.info("Loading libraries..");
            logger.info("Note: This might take a few minutes on first run.");

            long time = new DependencyManager("ElectionsPlus", directory)
                    .build();

            logger.info("Loaded all libraries in " + time + "ms");
        } catch (ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        this.reload();
        Messages.init(messages,config.node("prefix").getString());

        Getters.updater(new UpdateChecker(new SpongeScheduler(this),plugin.getVersion().orElse(""),plugin.getName(),93463,null,null,null));

        GlobalMain.init(directory);

        this.api = new ElectionsPlusAPI(this);
        Sponge.getServiceManager().setProvider(this,IElectionsPlusAPI.class, api);
        Sponge.getEventManager().registerListeners(this,new JoinListener());
        ConversationUtil.init(this);
        switch (config().node("database.type").getString("NULL").toUpperCase()) {
            case "SQLITE":
                try {
                    this.manager = new DatabaseManager(new SpongeScheduler(this),cache,config,new SQLiteConnection(directory));
                    Getters.database(manager);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
                break;
            case "MYSQL":
                this.manager = new DatabaseManager(config,cache,directory,new SpongeScheduler(this));
                break;
            case "REDIS":
                this.getLogger().warn("The redis feature is not implemented yet");
                break;
            default:
                this.getLogger().error("Invalid database type");
                break;
        }

        Customization customization = new Customization(config("prefix") + "&7Running &eElections &7v1.1 by Lorenzo0111",config("prefix") + "&cCommand not found",config("prefix") + "&7Run /$cmd help for command help.");
        new ElectionsCommand(this,"elections", Collections.singletonList("name"),customization);
    }

    @Listener
    public void onServerClose(GameStoppedEvent event) {
        GlobalMain.shutdown();
    }

    public void reload() throws ConfigurateException {
        this.messages = Configs.messages();
        this.config = Configs.config();
    }

    public ConfigurationNode config() {
        return this.config;
    }

    public String config(Object... path) {
        return config().node(path).getString("");
    }

    public ConfigurationNode messages() {
        return this.messages;
    }

    public Logger getLogger() {
        return logger;
    }

    public IDatabaseManager getManager() {
        return manager;
    }

    public static ElectionsPlus getInstance() {
        return instance;
    }

    public IElectionsPlusAPI getApi() { return api; }

    public Path getDirectory() {
        return directory;
    }

    public CacheManager getCache() {
        return cache;
    }

    public IAdvancedScheduler getScheduler() { return scheduler; }
}
