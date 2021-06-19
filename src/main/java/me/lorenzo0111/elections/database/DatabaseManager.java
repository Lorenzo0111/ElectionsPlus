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

package me.lorenzo0111.elections.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.api.objects.Vote;
import me.lorenzo0111.elections.tasks.CacheTask;
import me.lorenzo0111.pluginslib.database.connection.HikariConnection;
import me.lorenzo0111.pluginslib.database.connection.IConnectionHandler;
import me.lorenzo0111.pluginslib.database.connection.SQLiteConnection;
import me.lorenzo0111.pluginslib.database.objects.Column;
import me.lorenzo0111.pluginslib.database.objects.Table;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DatabaseManager implements IDatabaseManager {
    private Table votesTable;
    private Table partiesTable;
    private Table electionsTable;
    private final IConnectionHandler connectionHandler;

    public DatabaseManager(ElectionsPlus plugin, IConnectionHandler handler) {
        this.connectionHandler = handler;

        this.tables(plugin);
    }

    public DatabaseManager(ElectionsPlus plugin) throws SQLException {
        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);

        config.setPoolName("MultiLang MySQL Connection Pool");
        config.setDataSourceClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("serverName", plugin.getConfig("database.ip"));
        config.addDataSourceProperty("port", plugin.getConfig("database.port"));
        config.addDataSourceProperty("databaseName", plugin.getConfig("database.database"));
        config.addDataSourceProperty("user", plugin.getConfig("database.username"));
        config.addDataSourceProperty("password", plugin.getConfig("database.password"));
        config.addDataSourceProperty("useSSL", plugin.getConfig("database.ssl"));

        IConnectionHandler handler = null;

        try {
            handler = new HikariConnection(new HikariDataSource(config));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to connect to the database, using SQLite.. ",e);
            try {
                handler = new SQLiteConnection(plugin);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        this.connectionHandler = handler;

        this.tables(plugin);
    }

    private void tables(ElectionsPlus plugin) {
        // Votes
        List<Column> votesColumns = new ArrayList<>();
        votesColumns.add(new Column("uuid", "TEXT"));
        votesColumns.add(new Column("party", "TEXT"));
        votesColumns.add(new Column("election", "TEXT"));
        this.votesTable = new Table(plugin,connectionHandler,"votes",votesColumns);
        this.votesTable.create();

        // Parties
        List<Column> partiesColumns = new ArrayList<>();
        partiesColumns.add(new Column("owner", "TEXT"));
        partiesColumns.add(new Column("name", "TEXT"));
        partiesColumns.add(new Column("members", "TEXT"));
        partiesColumns.add(new Column("icon", "TEXT nullable"));
        this.partiesTable = new Table(plugin,connectionHandler,"parties",partiesColumns);
        this.partiesTable.create();

        // Elections
        List<Column> electionsColumns = new ArrayList<>();
        electionsColumns.add(new Column("name", "TEXT"));
        electionsColumns.add(new Column("parties", "TEXT"));
        electionsColumns.add(new Column("open", "INTEGER"));
        this.electionsTable = new Table(plugin,connectionHandler,"elections",electionsColumns);
        this.electionsTable.create();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,new CacheTask(this,plugin.getCache()),60 * 20L, TimeUnit.MINUTES.toSeconds(plugin.getConfig().getInt("cache-duration", 5)) * 20L);
    }

    public Table getPartiesTable() {
        return partiesTable;
    }

    public Table getVotesTable() {
        return votesTable;
    }

    public Table getElectionsTable() {
        return electionsTable;
    }

    @Override
    public CompletableFuture<Election> createElection(String name, List<Party> parties) {
        CompletableFuture<Election> future = new CompletableFuture<>();

        Election election = new Election(name,parties,true);

        this.getElectionsTable()
                .find("name",name)
                .thenAccept((result) -> {

                    try {

                        if (result.next()) {
                            future.complete(null);
                            return;
                        }

                        this.getElectionsTable().add(election);
                        ElectionsPlus.getInstance().getCache().getElections().add(election.getName(),election);
                        future.complete(election);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                });


        return future;
    }

    @Override
    public void closeConnection() throws SQLException {
        connectionHandler.close();
    }

    @Override
    public CompletableFuture<List<Election>> getElections() {
        CompletableFuture<List<Election>> future = new CompletableFuture<>();

        getParties()
                .thenAccept((parties) -> getElectionsTable().run(new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            Statement statement = connectionHandler.getConnection().createStatement();
                            ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM %s;", getElectionsTable().getName()));

                            List<Election> elections = new ArrayList<>();
                            Gson gson = new Gson();
                            while (resultSet.next()) {

                                List<Party> addedParties = new ArrayList<>();
                                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                                List<String> names = new ArrayList<>(gson.fromJson(resultSet.getString("parties"), type));
                                names.forEach((n) -> parties.stream()
                                        .filter((p) -> p.getName().equals(n))
                                        .findFirst()
                                        .ifPresent(addedParties::add));

                                Election election = new Election(resultSet.getString("name"),addedParties,resultSet.getInt("open") == 1);

                                elections.add(election);
                            }

                            future.complete(elections);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            future.complete(null);
                        }
                    }

                }));



        return future;
    }

    @Override
    public CompletableFuture<List<Party>> getParties() {
        CompletableFuture<List<Party>> future = new CompletableFuture<>();

        getPartiesTable().run(new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    Statement statement = connectionHandler.getConnection().createStatement();
                    ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM %s;", getPartiesTable().getName()));

                    List<Party> parties = new ArrayList<>();
                    Gson gson = new Gson();
                    while (resultSet.next()) {

                        Type type = new TypeToken<ArrayList<UUID>>() {}.getType();
                        List<UUID> members = new ArrayList<>(gson.fromJson(resultSet.getString("members"), type));

                        Party party = new Party(resultSet.getString("name"),UUID.fromString(resultSet.getString("owner")),members);

                        if (resultSet.getString("icon") != null)
                            party.setIcon(resultSet.getString("icon"));

                        parties.add(party);
                    }

                    future.complete(parties);
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.complete(null);
                }
            }

        });

        return future;
    }

    @Override
    public CompletableFuture<Party> createParty(String name, Player owner) {
        CompletableFuture<Party> partyFuture = new CompletableFuture<>();

        partiesTable.find("name",name)
                .thenAccept((it) -> {
                    try {
                        if (it.next()) {
                            partyFuture.complete(null);
                            return;
                        }

                        Party party = new Party(name, owner.getUniqueId());
                        partiesTable.add(party);
                        ElectionsPlus.getInstance().getCache().getParties().add(party.getName(),party);
                        partyFuture.complete(party);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        return partyFuture;
    }

    @Override
    public void deleteParty(String name) {
        ElectionsPlus.getInstance().getCache().getParties().remove(name);
        partiesTable.removeWhere("name",name);
    }

    @Override
    public void deleteParty(Party party) {
        ElectionsPlus.getInstance().getCache().getParties().remove(party.getName());
        partiesTable.removeWhere("name",party);
    }

    @Override
    public void updateParty(Party party) {
        ElectionsPlus.getInstance().getCache().getParties().remove(party.getName());
        ElectionsPlus.getInstance().getCache().getParties().add(party.getName(),party);
        partiesTable.removeWhere("name",party)
                .thenRun(() -> partiesTable.add(party));
    }

    @Override
    public void updateElection(Election election) {
        ElectionsPlus.getInstance().getCache().getElections().remove(election.getName());
        ElectionsPlus.getInstance().getCache().getElections().add(election.getName(),election);
        electionsTable.removeWhere("name",election)
                .thenRun(() -> electionsTable.add(election));
    }

    @Override
    public void deleteElection(Election election) {
        ElectionsPlus.getInstance().getCache().getElections().remove(election.getName());
        electionsTable.removeWhere("name",election);
    }

    @Override
    public CompletableFuture<List<Vote>> getVotes() {
        CompletableFuture<List<Vote>> future = new CompletableFuture<>();

        this.getVotesTable().run(new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = votesTable.getConnection().prepareStatement(String.format("SELECT * FROM %s;", votesTable.getName()));
                    ResultSet set = statement.executeQuery();
                    List<Vote> votes = new ArrayList<>();
                    while (set.next()) {
                        votes.add(new Vote(UUID.fromString(set.getString("uuid")),set.getString("party"),set.getString("election")));
                    }
                    future.complete(votes);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> vote(OfflinePlayer player, Party party, Election election) {
        Vote vote = new Vote(player.getUniqueId(),party.getName(),election.getName());
        return this.vote(vote);
    }

    @Override
    public CompletableFuture<Boolean> vote(Vote vote) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        this.getVotesTable()
                .find("uuid",vote.getPlayer())
                .thenAccept((set) -> {
                    try {
                        while (set.next()) {
                            if (set.getString("election").equals(vote.getElection())) {
                                future.complete(false);
                                return;
                            }
                        }

                        this.getVotesTable().add(vote);
                        ElectionsPlus.getInstance()
                                .getCache()
                                .getVotes()
                                .add(vote.getElection()+"||"+vote.getPlayer(), vote);
                        future.complete(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        return future;
    }
}
