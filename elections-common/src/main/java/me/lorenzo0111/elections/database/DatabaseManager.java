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
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.api.objects.Vote;
import me.lorenzo0111.elections.cache.CacheManager;
import me.lorenzo0111.elections.scheduler.IAdvancedScheduler;
import me.lorenzo0111.elections.tasks.CacheTask;
import me.lorenzo0111.pluginslib.database.connection.HikariConnection;
import me.lorenzo0111.pluginslib.database.connection.IConnectionHandler;
import me.lorenzo0111.pluginslib.database.connection.SQLiteConnection;
import me.lorenzo0111.pluginslib.database.objects.Column;
import me.lorenzo0111.pluginslib.database.objects.Table;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DatabaseManager implements IDatabaseManager {
    private Table votesTable;
    private Table partiesTable;
    private Table electionsTable;
    private final IConnectionHandler connectionHandler;
    private final CacheManager cache;

    public DatabaseManager(IAdvancedScheduler scheduler, CacheManager cache, ConfigurationNode config, IConnectionHandler handler) {
        this.connectionHandler = handler;

        this.tables(scheduler,cache,config);
        this.cache = cache;
    }

    public DatabaseManager(ConfigurationNode configuration, CacheManager cache, Path directory, IAdvancedScheduler scheduler) throws SQLException {
        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);

        config.setPoolName("MultiLang MySQL Connection Pool");
        config.setDataSourceClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("serverName", configuration.node("database","ip").getString());
        config.addDataSourceProperty("port", configuration.node("database","port").getString());
        config.addDataSourceProperty("databaseName", configuration.node("database","database").getString());
        config.addDataSourceProperty("user", configuration.node("database","username").getString());
        config.addDataSourceProperty("password", configuration.node("database","password").getString());
        config.addDataSourceProperty("useSSL", configuration.node("database","ssl").getString());

        IConnectionHandler handler = null;

        try {
            handler = new HikariConnection(new HikariDataSource(config));
        } catch (Exception e) {
            try {
                handler = new SQLiteConnection(directory);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        this.connectionHandler = handler;

        this.tables(scheduler,cache,configuration);
        this.cache = cache;
    }

    private void tables(IAdvancedScheduler scheduler, CacheManager cache, ConfigurationNode config) {
        // Votes
        List<Column> votesColumns = new ArrayList<>();
        votesColumns.add(new Column("uuid", "TEXT"));
        votesColumns.add(new Column("party", "TEXT"));
        votesColumns.add(new Column("election", "TEXT"));
        this.votesTable = new Table(scheduler,connectionHandler,"votes",votesColumns);
        this.votesTable.create();

        // Parties
        List<Column> partiesColumns = new ArrayList<>();
        partiesColumns.add(new Column("owner", "TEXT"));
        partiesColumns.add(new Column("name", "TEXT"));
        partiesColumns.add(new Column("members", "TEXT"));
        partiesColumns.add(new Column("icon", "TEXT nullable"));
        this.partiesTable = new Table(scheduler,connectionHandler,"parties",partiesColumns);
        this.partiesTable.create();

        // Elections
        List<Column> electionsColumns = new ArrayList<>();
        electionsColumns.add(new Column("name", "TEXT"));
        electionsColumns.add(new Column("parties", "TEXT"));
        electionsColumns.add(new Column("open", "INTEGER"));
        this.electionsTable = new Table(scheduler,connectionHandler,"elections",electionsColumns);
        this.electionsTable.create();

        scheduler.repeating(new CacheTask(this,cache),60 * 20L, config.node("cache-duration").getInt(5),TimeUnit.MINUTES);
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
                        cache.getElections().add(election.getName(),election);
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
                .thenAccept((parties) -> getElectionsTable().run(() -> {
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
                }));



        return future;
    }

    @Override
    public CompletableFuture<List<Party>> getParties() {
        CompletableFuture<List<Party>> future = new CompletableFuture<>();

        getPartiesTable().run(() -> {
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
        });

        return future;
    }

    @Override
    public CompletableFuture<Party> createParty(String name, UUID owner) {
        CompletableFuture<Party> partyFuture = new CompletableFuture<>();

        partiesTable.find("name",name)
                .thenAccept((it) -> {
                    try {
                        if (it.next()) {
                            partyFuture.complete(null);
                            return;
                        }

                        Party party = new Party(name, owner);
                        partiesTable.add(party);
                        cache.getParties().add(party.getName(),party);
                        partyFuture.complete(party);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        return partyFuture;
    }

    @Override
    public void deleteParty(String name) {
        cache.getParties().remove(name);
        partiesTable.removeWhere("name",name);
    }

    @Override
    public void deleteParty(Party party) {
        cache.getParties().remove(party.getName());
        partiesTable.removeWhere("name",party);
    }

    @Override
    public void updateParty(Party party) {
        cache.getParties().remove(party.getName());
        cache.getParties().add(party.getName(),party);
        partiesTable.removeWhere("name",party)
                .thenRun(() -> partiesTable.add(party));
    }

    @Override
    public void updateElection(Election election) {
        cache.getElections().remove(election.getName());
        cache.getElections().add(election.getName(),election);
        electionsTable.removeWhere("name",election)
                .thenRun(() -> electionsTable.add(election));
    }

    @Override
    public void deleteElection(Election election) {
        cache.getElections().remove(election.getName());
        electionsTable.removeWhere("name",election);
    }

    @Override
    public CompletableFuture<List<Vote>> getVotes() {
        CompletableFuture<List<Vote>> future = new CompletableFuture<>();

        this.getVotesTable().run(() -> {
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
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> vote(UUID player, Party party, Election election) {
        Vote vote = new Vote(player,party.getName(),election.getName());
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
                        cache.getVotes()
                                .add(vote.getElection()+"||"+vote.getPlayer(), vote);
                        future.complete(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        return future;
    }
}