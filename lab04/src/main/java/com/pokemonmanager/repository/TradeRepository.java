package com.pokemonmanager.repository;

import com.pokemonmanager.model.Trade;
import java.sql.*;
import java.util.*;

public class TradeRepository {
    private Connection connection;
    
    public TradeRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void save(Trade trade) throws SQLException {
        String sql = "INSERT INTO trades (from_trainer_id, to_trainer_id, pokemon_id, status, is_completed, trade_date) VALUES (?, ?, ?, ?, ?, datetime('now'))";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trade.getFromTrainerId());
            stmt.setInt(2, trade.getToTrainerId());
            stmt.setInt(3, trade.getPokemonId());
            stmt.setString(4, trade.getStatus());
            stmt.setBoolean(5, trade.isCompleted());
            stmt.executeUpdate();
        }
    }
    
    public List<Trade> findByTrainerId(int trainerId) throws SQLException {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM trades WHERE from_trainer_id = ? OR to_trainer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            stmt.setInt(2, trainerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Trade t = new Trade();
                t.setId(rs.getInt("id"));
                t.setFromTrainerId(rs.getInt("from_trainer_id"));
                t.setToTrainerId(rs.getInt("to_trainer_id"));
                t.setPokemonId(rs.getInt("pokemon_id"));
                t.setStatus(rs.getString("status"));
                t.setCompleted(rs.getBoolean("is_completed"));
                t.setTradeDate(rs.getString("trade_date"));
                trades.add(t);
            }
        }
        return trades;
    }
}