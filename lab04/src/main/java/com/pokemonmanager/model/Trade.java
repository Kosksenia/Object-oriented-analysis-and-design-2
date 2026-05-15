package com.pokemonmanager.model;

public class Trade {
    private int id;
    private int fromTrainerId;
    private int toTrainerId;
    private int pokemonId;
    private String status;
    private boolean isCompleted;
    private String tradeDate;
    
    public Trade() {
        this.status = "pending";
        this.isCompleted = false;
    }
    
    public Trade(int fromTrainerId, int toTrainerId, int pokemonId) {
        this();
        this.fromTrainerId = fromTrainerId;
        this.toTrainerId = toTrainerId;
        this.pokemonId = pokemonId;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFromTrainerId() { return fromTrainerId; }
    public void setFromTrainerId(int fromTrainerId) { this.fromTrainerId = fromTrainerId; }
    public int getToTrainerId() { return toTrainerId; }
    public void setToTrainerId(int toTrainerId) { this.toTrainerId = toTrainerId; }
    public int getPokemonId() { return pokemonId; }
    public void setPokemonId(int pokemonId) { this.pokemonId = pokemonId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public String getTradeDate() { return tradeDate; }
    public void setTradeDate(String tradeDate) { this.tradeDate = tradeDate; }
}