package com.pokemonmanager.model;

public class Trainer {
    private int id;
    private String username;
    private int level;
    private int exp;
    private int pokemonCount;
    private int money;
    private String location;
    private double x;
    private double y;
    
    public Trainer() {
        this.level = 1;
        this.exp = 0;
        this.pokemonCount = 0;
        this.money = 5000;
        this.location = "forest";
        this.x = 400;
        this.y = 300;
    }
    
    public Trainer(String username) {
        this();
        this.username = username;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getPokemonCount() { return pokemonCount; }
    public void setPokemonCount(int pokemonCount) { this.pokemonCount = pokemonCount; }
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}