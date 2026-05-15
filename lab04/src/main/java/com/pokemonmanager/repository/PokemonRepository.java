package com.pokemonmanager.repository;

import com.pokemonmanager.model.Pokemon;
import java.sql.*;
import java.util.*;

public class PokemonRepository {
    private Connection connection;
    
    public PokemonRepository(Connection connection) {
        this.connection = connection;
    }
    
    public List<Pokemon> findWildPokemons() throws SQLException {
        List<Pokemon> list = new ArrayList<>();
        String sql = "SELECT * FROM pokemons WHERE is_wild = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pokemon p = new Pokemon();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setType(rs.getString("type"));
                p.setLevel(rs.getInt("level"));
                p.setExp(rs.getInt("exp"));
                p.setHp(rs.getInt("hp"));
                p.setMaxHp(rs.getInt("max_hp"));
                p.setAttack(rs.getInt("attack"));
                p.setDefense(rs.getInt("defense"));
                p.setEvolutionName(rs.getString("evolution_name"));
                p.setEvolutionLevel(rs.getInt("evolution_level"));
                p.setEvolutionStage(rs.getInt("evolution_stage"));
                p.setWild(true);
                p.setX(rs.getDouble("x"));
                p.setY(rs.getDouble("y"));
                list.add(p);
            }
        }
        return list;
    }
    
    public List<Pokemon> findByTrainerId(int trainerId) throws SQLException {
        List<Pokemon> list = new ArrayList<>();
        String sql = "SELECT * FROM pokemons WHERE trainer_id = ? AND is_wild = 0";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pokemon p = new Pokemon();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setType(rs.getString("type"));
                p.setLevel(rs.getInt("level"));
                p.setExp(rs.getInt("exp"));
                p.setHp(rs.getInt("hp"));
                p.setMaxHp(rs.getInt("max_hp"));
                p.setAttack(rs.getInt("attack"));
                p.setDefense(rs.getInt("defense"));
                p.setTrainerId(rs.getInt("trainer_id"));
                p.setEvolutionName(rs.getString("evolution_name"));
                p.setEvolutionLevel(rs.getInt("evolution_level"));
                p.setEvolutionStage(rs.getInt("evolution_stage"));
                p.setWild(false);
                list.add(p);
            }
        }
        return list;
    }
    
    public void catchPokemon(int pokemonId, int trainerId) throws SQLException {
        String sql = "UPDATE pokemons SET trainer_id = ?, is_wild = 0 WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            stmt.setInt(2, pokemonId);
            stmt.executeUpdate();
        }
    }
    
    public void evolvePokemon(int pokemonId, String newName, int newStage, int newLevel, int newAttack, int newDefense, int newHp) throws SQLException {
        String sql = "UPDATE pokemons SET name = ?, evolution_stage = ?, level = ?, attack = ?, defense = ?, hp = ?, max_hp = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, newStage);
            stmt.setInt(3, newLevel);
            stmt.setInt(4, newAttack);
            stmt.setInt(5, newDefense);
            stmt.setInt(6, newHp);
            stmt.setInt(7, newHp);
            stmt.setInt(8, pokemonId);
            stmt.executeUpdate();
        }
    }
}