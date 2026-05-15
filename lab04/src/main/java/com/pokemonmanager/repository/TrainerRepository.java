package com.pokemonmanager.repository;

import com.pokemonmanager.model.Trainer;
import java.sql.*;

public class TrainerRepository {
    private Connection connection;
    
    public TrainerRepository(Connection connection) {
        this.connection = connection;
    }
    
    public Trainer findById(int id) throws SQLException {
        String sql = "SELECT * FROM trainers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Trainer t = new Trainer();
                t.setId(rs.getInt("id"));
                t.setUsername(rs.getString("username"));
                t.setLevel(rs.getInt("level"));
                t.setExp(rs.getInt("exp"));
                t.setPokemonCount(rs.getInt("pokemon_count"));
                t.setMoney(rs.getInt("money"));
                t.setLocation(rs.getString("location"));
                return t;
            }
        }
        return null;
    }
    
    public void updateLocation(int trainerId, String location, double lat, double lon) throws SQLException {
        String sql = "UPDATE trainers SET location = ?, latitude = ?, longitude = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, location);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lon);
            stmt.setInt(4, trainerId);
            stmt.executeUpdate();
        }
    }
    
    public void addMoney(int trainerId, int amount) throws SQLException {
        String sql = "UPDATE trainers SET money = money + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setInt(2, trainerId);
            stmt.executeUpdate();
        }
    }
    
    public void updateInventory(int trainerId, String itemName, int delta) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE trainer_id = ? AND item_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setInt(2, trainerId);
            stmt.setString(3, itemName);
            int rows = stmt.executeUpdate();
            if (rows == 0 && delta > 0) {
                String insert = "INSERT INTO inventory (trainer_id, item_name, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(insert)) {
                    ps.setInt(1, trainerId);
                    ps.setString(2, itemName);
                    ps.setInt(3, delta);
                    ps.executeUpdate();
                }
            }
        }
    }
    
    public int getItemCount(int trainerId, String itemName) throws SQLException {
        String sql = "SELECT quantity FROM inventory WHERE trainer_id = ? AND item_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            stmt.setString(2, itemName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("quantity");
            return 0;
        }
    }
}