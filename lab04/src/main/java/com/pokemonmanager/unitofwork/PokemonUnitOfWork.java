package com.pokemonmanager.unitofwork;

import java.sql.*;
import java.util.*;

public class PokemonUnitOfWork {
    private Connection connection;
    private List<String> insertQueries;
    private List<String> updateQueries;
    private List<Object[]> insertParams;
    private List<Object[]> updateParams;
    
    public PokemonUnitOfWork(Connection connection) {
        this.connection = connection;
        this.insertQueries = new ArrayList<>();
        this.updateQueries = new ArrayList<>();
        this.insertParams = new ArrayList<>();
        this.updateParams = new ArrayList<>();
    }
    
    public void registerInsert(String query, Object... params) {
        insertQueries.add(query);
        insertParams.add(params);
    }
    
    public void registerUpdate(String query, Object... params) {
        updateQueries.add(query);
        updateParams.add(params);
    }
    
    public boolean commit() {
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            for (int i = 0; i < insertQueries.size(); i++) {
                executeUpdate(insertQueries.get(i), insertParams.get(i));
            }
            
            for (int i = 0; i < updateQueries.size(); i++) {
                executeUpdate(updateQueries.get(i), updateParams.get(i));
            }
            
            connection.commit();
            clear();
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException e) {}
        }
    }
    
    private void executeUpdate(String query, Object[] params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }
    
    private void clear() {
        insertQueries.clear();
        updateQueries.clear();
        insertParams.clear();
        updateParams.clear();
    }
}