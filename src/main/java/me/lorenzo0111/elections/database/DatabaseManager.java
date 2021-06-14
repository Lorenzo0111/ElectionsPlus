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
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.elections.api.objects.Election;
import me.lorenzo0111.elections.api.objects.Party;
import me.lorenzo0111.elections.api.objects.Vote;
import me.lorenzo0111.pluginslib.database.connection.JavaConnection;
import me.lorenzo0111.pluginslib.database.objects.Column;
import me.lorenzo0111.pluginslib.database.objects.Table;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager implements IDatabaseManager {
    private final Table votesTable;
    private final Table partiesTable;
    private final Table electionsTable;
    private final Connection connection;

    public DatabaseManager(ElectionsPlus plugin) throws SQLException {
        this(plugin,jdbc(plugin),plugin.getConfig().getString("username"),plugin.getConfig().getString("password"));
    }

    public DatabaseManager(ElectionsPlus plugin, String jdbc, @Nullable String username, @Nullable String password) throws SQLException {
        this.connection = DriverManager.getConnection(jdbc,username,password);

        // Votes
        List<Column> votesColumns = new ArrayList<>();
        votesColumns.add(new Column("uuid", "TEXT"));
        votesColumns.add(new Column("party", "TEXT"));
        votesColumns.add(new Column("election", "TEXT"));
        this.votesTable = new Table(plugin,new JavaConnection(connection),"votes",votesColumns);
        this.votesTable.create();

        // Parties
        List<Column> partiesColumns = new ArrayList<>();
        partiesColumns.add(new Column("owner", "TEXT"));
        partiesColumns.add(new Column("name", "TEXT"));
        partiesColumns.add(new Column("members", "TEXT"));
        partiesColumns.add(new Column("icon", "TEXT nullable"));
        this.partiesTable = new Table(plugin,new JavaConnection(connection),"parties",partiesColumns);
        this.partiesTable.create();

        // Elections
        List<Column> electionsColumns = new ArrayList<>();
        electionsColumns.add(new Column("name", "TEXT"));
        electionsColumns.add(new Column("parties", "TEXT"));
        electionsColumns.add(new Column("open", "INTEGER"));
        this.electionsTable = new Table(plugin,new JavaConnection(connection),"elections",electionsColumns);
        this.electionsTable.create();

    }

    private static String jdbc(JavaPlugin plugin) {
        String jdbc = plugin.getConfig().getString("database.jdbc");

        if (jdbc == null) {
            String database = plugin.getConfig().getString("database");
            String ip = plugin.getConfig().getString("ip");
            int port = plugin.getConfig().getInt("port",3306);

            Objects.requireNonNull(database);
            Objects.requireNonNull(ip);
            jdbc = String.format("jdbc:mysql://%s:%s/%s",ip,port,database);
        }

        return jdbc;
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
                        future.complete(election);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                });


        return future;
    }

    @Override
    public void closeConnection() throws SQLException {
        connection.close();
    }

    @Override
    public CompletableFuture<List<Election>> getElections() {
        CompletableFuture<List<Election>> future = new CompletableFuture<>();

        getParties()
                .thenAccept((parties) -> getElectionsTable().run(new BukkitRunnable() {

                    @Override
                    public void run() {
                        try {
                            Statement statement = connection.createStatement();
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
                    Statement statement = connection.createStatement();
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
                        partyFuture.complete(party);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        return partyFuture;
    }

    @Override
    public void deleteParty(String name) {
        partiesTable.removeWhere("name",name);
    }

    @Override
    public void deleteParty(Party party) {
        partiesTable.removeWhere("name",party);
    }

    @Override
    public void updateParty(Party party) {
        partiesTable.removeWhere("name",party)
                .thenRun(() -> partiesTable.add(party));
    }

    @Override
    public void updateElection(Election election) {
        electionsTable.removeWhere("name",election)
                .thenRun(() -> electionsTable.add(election));
    }

    @Override
    public void deleteElection(Election election) {
        electionsTable.removeWhere("name",election);
    }

    @Override
    public CompletableFuture<Boolean> vote(Player player, Party party, Election election) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Vote vote = new Vote(player.getUniqueId(),party.getName(),election.getName());
        this.getVotesTable()
                .find("uuid",player.getUniqueId())
                .thenAccept((set) -> {
                    try {
                        while (set.next()) {
                            if (set.getString("election").equals(election.getName())) {
                                future.complete(false);
                                return;
                            }
                        }

                        this.getVotesTable().add(vote);
                        future.complete(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        return future;
    }
}
