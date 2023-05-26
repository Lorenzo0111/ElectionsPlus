/*
 * This file is part of PluginsLib, licensed under the MIT License.
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

import me.lorenzo0111.pluginslib.StringUtils;
import me.lorenzo0111.pluginslib.database.DatabaseSerializable;
import me.lorenzo0111.pluginslib.database.connection.IConnectionHandler;
import me.lorenzo0111.pluginslib.database.connection.JavaConnection;
import me.lorenzo0111.pluginslib.database.objects.Column;
import me.lorenzo0111.pluginslib.database.query.Queries;
import me.lorenzo0111.pluginslib.scheduler.IScheduler;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SQL Table
 */
@SuppressWarnings("unused")
public class ETable {
    private final IScheduler scheduler;
    private final IConnectionHandler connection;
    private final String name;
    private final List<Column> columns;

    public ETable(IScheduler scheduler, IConnectionHandler connection, String name, List<Column> columns) {
        this.scheduler = scheduler;
        this.connection = connection;
        this.name = name;
        this.columns = columns;
    }

    /**
     * Create the table in the database — Doesn't return until the table has been created.
     */
    public void create() {
        StringBuilder query = new StringBuilder(Queries.builder().query(Queries.CREATE_START).table(name).build());

        columns.forEach(column -> query.append(String.format("`%s` %s,",column.getName(),column.getType())));

        try {
            Statement statement = connection.getConnection().createStatement();
            statement.executeUpdate(StringUtils.removeLastChar(query.toString()) + ");");
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Convert a ResultSet to a Database Serializable
     * @param set ResultSet to convert
     * @param serializable an instance of a serializable that has the {@link DatabaseSerializable#from(Map)} method
     * @return A future with a list of serializable
     */
    public CompletableFuture<List<DatabaseSerializable>> convertResult(ResultSet set, DatabaseSerializable serializable) {
        CompletableFuture<List<DatabaseSerializable>> future = new CompletableFuture<>();

        this.run(() -> {
            try {
                List<DatabaseSerializable> list = new ArrayList<>();

                while (set.next()) {
                    Map<String,Object> map = new HashMap<>();
                    columns.forEach((column) -> {
                        try {
                            map.put(column.getName(),set.getObject(column.getName()));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    list.add(serializable.from(map));
                }

                future.complete(list);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return future;
    }

    /**
     * Get all items from table
     * @return a result set with all the entries of the table
     */
    public CompletableFuture<ResultSet> all() {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();

        this.run(() -> {
            try {
                PreparedStatement statement = connection.getConnection()
                        .prepareStatement(String.format(Queries.builder().query(Queries.ALL).table(name).build(), getName()));

                ResultSet resultSet = statement.executeQuery();
                future.complete(resultSet);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return future;
    }

    /**
     * Add a {@link DatabaseSerializable} to the table
     * @param serializable Item to add to the table
     */
    public void add(DatabaseSerializable serializable) {
        this.run(() -> {
            try {
                StringBuilder builder = new StringBuilder(Queries.builder().query(Queries.INSERT_START).table(name).build());

                Map<String,Object> map = serializable.serialize();

                for (String column : map.keySet()) {
                    builder.append(column).append(",");
                }

                builder = new StringBuilder(StringUtils.removeLastChar(builder.toString()));

                builder.append(")");

                builder.append(" VALUES (");

                for (int i = 0; i < map.size(); i++) {
                    builder.append("?,");
                }

                builder = new StringBuilder(StringUtils.removeLastChar(builder.toString()));

                builder.append(");");

                final PreparedStatement statement = connection.getConnection().prepareStatement(builder.toString());

                int i = 1;
                for (Object obj : map.values()) {
                    statement.setObject(i,obj);
                    i++;
                }

                statement.executeUpdate();
                statement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Clear the table
     */
    public void clear() {
        this.run(() -> {
            try {
                Statement statement = connection.getConnection().createStatement();
                statement.executeUpdate(Queries.builder().query(Queries.CLEAR).table(name).build());
                statement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Remove something from the table
     * @param key key to find
     * @param value Value of the key
     * @return A completable future with the amount of the affected tables
     */
    public CompletableFuture<Integer> removeWhere(String key, Object value) {
        final CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

        this.run(() -> {
            try {
                final PreparedStatement statement = getConnection().prepareStatement(Queries.builder().query(Queries.DELETE_WHERE).table(name).keys(key).build());
                statement.setObject(1, value);

                completableFuture.complete(statement.executeUpdate());
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return completableFuture;
    }

    /**
     * Remove something from the table
     * @param key key to find
     * @param serializable serializable that contains the key
     * @return A completable future with the amount of the affected tables
     */
    public CompletableFuture<Integer> removeWhere(String key, DatabaseSerializable serializable) {
        return this.removeWhere(key,serializable.serialize().get(key));
    }

    /**
     * Find something inside the table
     * @param key Key to find
     * @param value Value to find
     * @return A ResultSet
     */
    public CompletableFuture<ResultSet> find(String key, Object value) {
        final CompletableFuture<ResultSet> completableFuture = new CompletableFuture<>();

        this.run(() -> {
            try {
                final PreparedStatement statement = getConnection().prepareStatement(Queries.builder().query(Queries.FIND).table(name).keys(key).build());
                statement.setObject(1, value);

                completableFuture.complete(statement.executeQuery());
                statement.closeOnCompletion();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return completableFuture;
    }

    /**
     * Run something async
     * @param runnable Runnable to run
     */
    public void run(Runnable runnable) {
        scheduler.async(runnable);
    }

    /**
     * @return Name of the table
     */
    public String getName() {
        return name;
    }

    /**
     * @return Connection
     * @throws SQLException if something goes wrong
     */
    public Connection getConnection() throws SQLException {
        return connection.getConnection();
    }

    /**
     * @return Table columns
     */
    public List<Column> getColumns() {
        return columns;
    }
}