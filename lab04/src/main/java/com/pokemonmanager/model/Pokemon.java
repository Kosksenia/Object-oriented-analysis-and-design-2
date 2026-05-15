package com.pokemonmanager.model;

public class Pokemon {
    private int id;
    private String name;
    private String type;
    private int level;
    private int exp;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int trainerId;
    private int evolutionStage;
    private String evolutionName;
    private int evolutionLevel;
    private boolean isWild;
    private double x;
    private double y;
    private boolean canEvolve;
    
    public Pokemon() {
        this.level = 5;
        this.exp = 0;
        this.hp = 50;
        this.maxHp = 50;
        this.attack = 30;
        this.defense = 30;
        this.evolutionStage = 1;
        this.isWild = true;
        this.canEvolve = false;
        setupEvolutionByName();
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        setupEvolutionByName();
    }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getLevel() { return level; }
    public void setLevel(int level) { 
        this.level = level;
        checkEvolution();
    }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }
    public int getEvolutionStage() { return evolutionStage; }
    public void setEvolutionStage(int evolutionStage) { 
        this.evolutionStage = evolutionStage;
        setupEvolutionByName();
    }
    public String getEvolutionName() { return evolutionName; }
    public void setEvolutionName(String evolutionName) { this.evolutionName = evolutionName; }
    public int getEvolutionLevel() { return evolutionLevel; }
    public void setEvolutionLevel(int evolutionLevel) { this.evolutionLevel = evolutionLevel; }
    public boolean isWild() { return isWild; }
    public void setWild(boolean wild) { isWild = wild; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public boolean isCanEvolve() { return canEvolve; }
    public void setCanEvolve(boolean canEvolve) { this.canEvolve = canEvolve; }
    
    public int getMaxEvolutionStage() {
        String lowerName = name.toLowerCase();
        if (lowerName.equals("pikachu") || lowerName.equals("raichu")) {
            return 2;
        }
        if (lowerName.equals("charmander") || lowerName.equals("charmeleon") || lowerName.equals("charizard")) {
            return 3;
        }
        if (lowerName.equals("squirtle") || lowerName.equals("wartortle") || lowerName.equals("blastoise")) {
            return 3;
        }
        if (lowerName.equals("bulbasaur") || lowerName.equals("ivysaur") || lowerName.equals("venusaur")) {
            return 3;
        }
        return 1;
    }
    
    private void setupEvolutionByName() {
        if (name == null) return;
        
        int maxStage = getMaxEvolutionStage();
        
        if (evolutionStage >= maxStage) {
            evolutionName = null;
            evolutionLevel = 0;
            canEvolve = false;
            return;
        }
        
        String lowerName = name.toLowerCase();
        
        if (lowerName.equals("pikachu") && evolutionStage == 1) {
            evolutionName = "Raichu";
            evolutionLevel = 1;
        }
        else if (lowerName.equals("charmander") && evolutionStage == 1) {
            evolutionName = "Charmeleon";
            evolutionLevel = 1;
        }
        else if (lowerName.equals("charmeleon") && evolutionStage == 2) {
            evolutionName = "Charizard";
            evolutionLevel = 1;
        }
        else if (lowerName.equals("squirtle") && evolutionStage == 1) {
            evolutionName = "Wartortle";
            evolutionLevel = 1;
        }
        else if (lowerName.equals("wartortle") && evolutionStage == 2) {
            evolutionName = "Blastoise";
            evolutionLevel = 1;
        }
        else if (lowerName.equals("bulbasaur") && evolutionStage == 1) {
            evolutionName = "Ivysaur";
            evolutionLevel = 1;
        }
        else if (lowerName.equals("ivysaur") && evolutionStage == 2) {
            evolutionName = "Venusaur";
            evolutionLevel = 1;
        }
        else {
            evolutionName = null;
            evolutionLevel = 0;
        }
        
        checkEvolution();
    }
    
    private void checkEvolution() {
        canEvolve = evolutionName != null && level >= evolutionLevel;
    }
    
    public boolean canEvolve() {
        return canEvolve;
    }
    
    public void applyEvolution() {
        if (canEvolve) {
            this.name = evolutionName;
            this.evolutionStage++;
            this.level += 5;
            this.attack += 15;
            this.defense += 10;
            this.hp += 20;
            this.maxHp += 20;
            setupEvolutionByName();
        }
    }
    
    public String getEvolutionInfo() {
        int maxStage = getMaxEvolutionStage();
        if (evolutionStage >= maxStage) {
            return "MAX EVOLUTION";
        }
        if (evolutionName != null) {
            return "Evolves to " + evolutionName + " at lvl " + evolutionLevel;
        }
        return "No evolution";
    }
}