package com.pokemonmanager.db;

import java.sql.*;

public class DatabaseConnection {
    private static Connection connection = null;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:pokemon.db");
                createTables();
                System.out.println("Database connected");
            } catch (Exception e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
        return connection;
    }
    
    private static void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Drop old tables
            stmt.execute("DROP TABLE IF EXISTS trainers");
            stmt.execute("DROP TABLE IF EXISTS pokemons");
            stmt.execute("DROP TABLE IF EXISTS inventory");
            stmt.execute("DROP TABLE IF EXISTS achievements");
            stmt.execute("DROP TABLE IF EXISTS trades");
            stmt.execute("DROP TABLE IF EXISTS battles");
            
            // Create trainers table
            stmt.execute("CREATE TABLE trainers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "level INTEGER DEFAULT 1," +
                "exp INTEGER DEFAULT 0," +
                "pokemon_count INTEGER DEFAULT 0," +
                "money INTEGER DEFAULT 5000," +
                "x REAL DEFAULT 400," +
                "y REAL DEFAULT 300)");
            
            // Create pokemons table
            stmt.execute("CREATE TABLE pokemons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "level INTEGER DEFAULT 5," +
                "exp INTEGER DEFAULT 0," +
                "hp INTEGER DEFAULT 50," +
                "max_hp INTEGER DEFAULT 50," +
                "attack INTEGER DEFAULT 30," +
                "defense INTEGER DEFAULT 30," +
                "trainer_id INTEGER," +
                "evolution_stage INTEGER DEFAULT 1," +
                "evolution_name TEXT," +
                "evolution_level INTEGER," +
                "is_wild INTEGER DEFAULT 1," +
                "x REAL," +
                "y REAL)");
            
            // Create inventory table
            stmt.execute("CREATE TABLE inventory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trainer_id INTEGER," +
                "item_name TEXT," +
                "quantity INTEGER DEFAULT 0)");
            
            // Create achievements table
            stmt.execute("CREATE TABLE achievements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trainer_id INTEGER," +
                "achievement_name TEXT," +
                "is_unlocked INTEGER DEFAULT 0)");
            
            // Create trades table
            stmt.execute("CREATE TABLE trades (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "from_trainer_id INTEGER," +
                "to_trainer_id INTEGER," +
                "pokemon_id INTEGER," +
                "status TEXT DEFAULT 'pending'," +
                "is_completed INTEGER DEFAULT 0," +
                "trade_date TEXT)");
            
            // Create battles table
            stmt.execute("CREATE TABLE battles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "trainer1_id INTEGER," +
                "trainer2_id INTEGER," +
                "winner_id INTEGER," +
                "reward INTEGER DEFAULT 100," +
                "battle_date TEXT)");
            
            // Insert trainers
            stmt.execute("INSERT INTO trainers (id, username, level, money, x, y) VALUES (1, 'Ash', 5, 10000, 400, 300)");
            stmt.execute("INSERT INTO trainers (id, username, level, money, x, y) VALUES (2, 'Gary', 6, 8000, 600, 400)");
            
            // Insert wild pokemons
            stmt.execute("INSERT INTO pokemons (name, type, level, is_wild, x, y, evolution_name, evolution_level) VALUES " +
                "('Pikachu', 'electric', 5, 1, 200, 300, 'Raichu', 1), " +
                "('Charmander', 'fire', 5, 1, 400, 200, 'Charmeleon', 1), " +
                "('Squirtle', 'water', 5, 1, 600, 400, 'Wartortle', 1), " +
                "('Bulbasaur', 'grass', 5, 1, 300, 500, 'Ivysaur', 1), " +
                "('Charmeleon', 'fire', 16, 1, 450, 250, 'Charizard', 1), " +
                "('Wartortle', 'water', 16, 1, 650, 450, 'Blastoise', 1), " +
                "('Ivysaur', 'grass', 16, 1, 350, 550, 'Venusaur', 1)");
            
            // Insert inventory
            stmt.execute("INSERT INTO inventory (trainer_id, item_name, quantity) VALUES (1, 'Pokeball', 20)");
            stmt.execute("INSERT INTO inventory (trainer_id, item_name, quantity) VALUES (1, 'Potion', 10)");
            stmt.execute("INSERT INTO inventory (trainer_id, item_name, quantity) VALUES (2, 'Pokeball', 15)");
            
            System.out.println("Tables created and data inserted");
            
        } catch (SQLException e) {
            System.err.println("Table error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}