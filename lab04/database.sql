-- database.sql
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS trainers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    level INTEGER DEFAULT 1,
    exp INTEGER DEFAULT 0,
    pokemon_count INTEGER DEFAULT 0,
    money INTEGER DEFAULT 5000,
    x REAL DEFAULT 400,
    y REAL DEFAULT 300
);

CREATE TABLE IF NOT EXISTS pokemons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    level INTEGER DEFAULT 5,
    exp INTEGER DEFAULT 0,
    hp INTEGER DEFAULT 50,
    max_hp INTEGER DEFAULT 50,
    attack INTEGER DEFAULT 30,
    defense INTEGER DEFAULT 30,
    trainer_id INTEGER,
    evolution_stage INTEGER DEFAULT 1,
    evolution_name TEXT,
    evolution_level INTEGER,
    is_wild BOOLEAN DEFAULT 1,
    x REAL,
    y REAL
);

CREATE TABLE IF NOT EXISTS inventory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trainer_id INTEGER,
    item_name TEXT,
    quantity INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trainer_id INTEGER,
    achievement_name TEXT,
    is_unlocked BOOLEAN DEFAULT 0
);

CREATE TABLE IF NOT EXISTS trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    from_trainer_id INTEGER,
    to_trainer_id INTEGER,
    pokemon_id INTEGER,
    status TEXT DEFAULT 'pending',
    is_completed BOOLEAN DEFAULT 0,
    trade_date TEXT
);

CREATE TABLE IF NOT EXISTS battles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trainer1_id INTEGER,
    trainer2_id INTEGER,
    winner_id INTEGER,
    reward INTEGER DEFAULT 100,
    battle_date TEXT
);

-- Initial data
INSERT OR IGNORE INTO trainers (id, username, level, money, x, y) VALUES (1, 'Ash', 5, 10000, 400, 300);
INSERT OR IGNORE INTO trainers (id, username, level, money, x, y) VALUES (2, 'Gary', 6, 8000, 600, 400);

INSERT OR IGNORE INTO pokemons (name, type, level, is_wild, x, y, evolution_name, evolution_level) VALUES
('Pikachu', 'electric', 5, 1, 200, 300, 'Raichu', 1),
('Charmander', 'fire', 5, 1, 400, 200, 'Charmeleon', 1),
('Squirtle', 'water', 5, 1, 600, 400, 'Wartortle', 1),
('Bulbasaur', 'grass', 5, 1, 300, 500, 'Ivysaur', 1),
('Charmeleon', 'fire', 16, 1, 450, 250, 'Charizard', 1),
('Wartortle', 'water', 16, 1, 650, 450, 'Blastoise', 1),
('Ivysaur', 'grass', 16, 1, 350, 550, 'Venusaur', 1);

INSERT OR IGNORE INTO inventory (trainer_id, item_name, quantity) VALUES (1, 'Pokeball', 20);
INSERT OR IGNORE INTO inventory (trainer_id, item_name, quantity) VALUES (1, 'Potion', 10);
INSERT OR IGNORE INTO inventory (trainer_id, item_name, quantity) VALUES (2, 'Pokeball', 15);