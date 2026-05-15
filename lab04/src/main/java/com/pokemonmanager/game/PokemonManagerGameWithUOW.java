package com.pokemonmanager.game;

import com.pokemonmanager.db.DatabaseConnection;
import com.pokemonmanager.model.Pokemon;
import com.pokemonmanager.model.Trainer;
import com.pokemonmanager.repository.PokemonRepository;
import com.pokemonmanager.unitofwork.PokemonUnitOfWork;
import com.pokemonmanager.utils.ImageLoader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class PokemonManagerGameWithUOW extends JFrame {
    private Connection connection;
    private Trainer currentTrainer;
    private java.util.List<Pokemon> myPokemons;
    private java.util.List<Pokemon> wildPokemons;
    
    private JPanel mapPanel;
    private JPanel pokemonPanel;
    private JTextArea logArea;
    private JLabel trainerNameLabel;
    private JLabel trainerLevelLabel;
    private JLabel trainerMoneyLabel;
    private JLabel trainerCountLabel;
    
    private double playerX = 400;
    private double playerY = 300;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    
    public PokemonManagerGameWithUOW() {
        connection = DatabaseConnection.getConnection();
        showLoginScreen();
    }
    
    private void showLoginScreen() {
        String name = JOptionPane.showInputDialog(this, "Введите имя тренера:", "Pokemon Manager", JOptionPane.QUESTION_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            loginOrCreateTrainer(name.trim());
            initGameGUI();
        } else {
            System.exit(0);
        }
    }
    
    private void loginOrCreateTrainer(String name) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM trainers WHERE username = ?");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                currentTrainer = new Trainer();
                currentTrainer.setId(rs.getInt("id"));
                currentTrainer.setUsername(rs.getString("username"));
                currentTrainer.setLevel(rs.getInt("level"));
                currentTrainer.setExp(rs.getInt("exp"));
                currentTrainer.setMoney(rs.getInt("money"));
                currentTrainer.setPokemonCount(rs.getInt("pokemon_count"));
                currentTrainer.setX(rs.getDouble("x"));
                currentTrainer.setY(rs.getDouble("y"));
                playerX = currentTrainer.getX();
                playerY = currentTrainer.getY();
            } else {
                String insert = "INSERT INTO trainers (username, level, money, x, y) VALUES (?, 1, 5000, 400, 300)";
                try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.executeUpdate();
                    ResultSet gk = ps.getGeneratedKeys();
                    if (gk.next()) {
                        currentTrainer = new Trainer();
                        currentTrainer.setId(gk.getInt(1));
                        currentTrainer.setUsername(name);
                        currentTrainer.setLevel(1);
                        currentTrainer.setMoney(5000);
                        currentTrainer.setX(400);
                        currentTrainer.setY(300);
                        giveStarterPokemon();
                    }
                }
            }
            loadData();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    private void giveStarterPokemon() {
        String[] starters = {"Pikachu", "Charmander", "Squirtle", "Bulbasaur"};
        String[] types = {"electric", "fire", "water", "grass"};
        int choice = JOptionPane.showOptionDialog(null, "ВЫБЕРИТЕ СТАРТОВОГО ПОКЕМОНА!", "Стартовый покемон",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, starters, starters[0]);
        
        if (choice >= 0) {
            try {
                String insert = "INSERT INTO pokemons (name, type, level, trainer_id, is_wild) VALUES (?, ?, 5, ?, 0)";
                try (PreparedStatement ps = connection.prepareStatement(insert)) {
                    ps.setString(1, starters[choice]);
                    ps.setString(2, types[choice]);
                    ps.setInt(3, currentTrainer.getId());
                    ps.executeUpdate();
                }
                
                String update = "UPDATE trainers SET pokemon_count = pokemon_count + 1 WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(update)) {
                    ps.setInt(1, currentTrainer.getId());
                    ps.executeUpdate();
                }
                
                JOptionPane.showMessageDialog(null, "Вы получили " + starters[choice]);
            } catch (SQLException e) {}
        }
    }
    
    private void loadData() throws SQLException {
        PokemonRepository repo = new PokemonRepository(connection);
        myPokemons = repo.findByTrainerId(currentTrainer.getId());
        if (myPokemons == null) myPokemons = new ArrayList<>();
        currentTrainer.setPokemonCount(myPokemons.size());
        
        wildPokemons = repo.findWildPokemons();
        if (wildPokemons == null) wildPokemons = new ArrayList<>();
    }
    
    private void initGameGUI() {
        setTitle("POKEMON MANAGER - С ПАТТЕРНОМ UNIT OF WORK");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawMap(g2d);
                drawWildPokemons(g2d);
                drawPlayer(g2d);
            }
        };
        mapPanel.setPreferredSize(new Dimension(800, 500));
        mapPanel.setBackground(new Color(34, 139, 34));
        mapPanel.setFocusable(true);
        
        mapPanel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_UP: upPressed = true; break;
                    case KeyEvent.VK_DOWN: downPressed = true; break;
                    case KeyEvent.VK_LEFT: leftPressed = true; break;
                    case KeyEvent.VK_RIGHT: rightPressed = true; break;
                }
            }
            public void keyReleased(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_UP: upPressed = false; break;
                    case KeyEvent.VK_DOWN: downPressed = false; break;
                    case KeyEvent.VK_LEFT: leftPressed = false; break;
                    case KeyEvent.VK_RIGHT: rightPressed = false; break;
                }
            }
        });
        
        javax.swing.Timer moveTimer = new javax.swing.Timer(20, e -> movePlayer());
        moveTimer.start();
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));
        rightPanel.setBackground(new Color(50, 50, 80));
        
        pokemonPanel = new JPanel();
        pokemonPanel.setLayout(new BoxLayout(pokemonPanel, BoxLayout.Y_AXIS));
        pokemonPanel.setBackground(new Color(50, 50, 80));
        JScrollPane pokemonScroll = new JScrollPane(pokemonPanel);
        pokemonScroll.setBorder(BorderFactory.createTitledBorder("ВАШИ ПОКЕМОНЫ"));
        rightPanel.add(pokemonScroll, BorderLayout.CENTER);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 60));
        logArea.setForeground(Color.CYAN);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(250, 150));
        logScroll.setBorder(BorderFactory.createTitledBorder("ЖУРНАЛ"));
        rightPanel.add(logScroll, BorderLayout.SOUTH);
        
        JPanel buttonPanel = createButtonPanel();
        
        add(mapPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
        
        mapPanel.requestFocusInWindow();
        updatePokemonList();
        
        log("Добро пожаловать, " + currentTrainer.getUsername() + "!");
        log("Используйте стрелки для движения");
        log("Подойдите к покемону и нажмите ПОЙМАТЬ");
        log("Нажмите ЭВОЛЮЦИЯ для эволюции покемона");
        log("Нажмите ОБМЕН для обмена с другим тренером");
        log("Нажмите БИТВА для сражения с диким покемоном");
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 20, 10));
        panel.setBackground(new Color(60, 60, 100));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        ImageIcon trainerImg = ImageLoader.loadImage("Trainer_M");
        JLabel avatar = new JLabel();
        if (trainerImg != null) avatar.setIcon(trainerImg);
        else avatar.setText("Т");
        panel.add(avatar);
        
        trainerNameLabel = new JLabel("Тренер: " + currentTrainer.getUsername());
        trainerNameLabel.setForeground(Color.YELLOW);
        trainerNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(trainerNameLabel);
        
        trainerLevelLabel = new JLabel("Уровень: " + currentTrainer.getLevel());
        trainerLevelLabel.setForeground(Color.WHITE);
        panel.add(trainerLevelLabel);
        
        trainerCountLabel = new JLabel("Покемонов: " + currentTrainer.getPokemonCount());
        trainerCountLabel.setForeground(Color.WHITE);
        panel.add(trainerCountLabel);
        
        trainerMoneyLabel = new JLabel(currentTrainer.getMoney() + " монет");
        trainerMoneyLabel.setForeground(new Color(255, 215, 0));
        panel.add(trainerMoneyLabel);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 10, 10));
        panel.setBackground(new Color(70, 70, 110));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JButton catchBtn = createButton("ПОЙМАТЬ", new Color(50, 200, 50));
        JButton evolveBtn = createButton("ЭВОЛЮЦИЯ", new Color(255, 150, 50));
        JButton tradeBtn = createButton("ОБМЕН", new Color(100, 150, 255));
        JButton battleBtn = createButton("БИТВА", new Color(255, 80, 80));
        JButton shopBtn = createButton("МАГАЗИН", new Color(255, 200, 0));
        JButton refreshBtn = createButton("ОБНОВИТЬ", new Color(150, 150, 150));
        
        catchBtn.addActionListener(e -> catchNearbyPokemon());
        evolveBtn.addActionListener(e -> evolvePokemon());
        tradeBtn.addActionListener(e -> tradePokemon());
        battleBtn.addActionListener(e -> startBattle());
        shopBtn.addActionListener(e -> openShop());
        refreshBtn.addActionListener(e -> refreshGame());
        
        panel.add(catchBtn);
        panel.add(evolveBtn);
        panel.add(tradeBtn);
        panel.add(battleBtn);
        panel.add(shopBtn);
        panel.add(refreshBtn);
        
        return panel;
    }
    
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void drawMap(Graphics2D g2d) {
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
        
        g2d.setColor(new Color(255, 255, 255, 50));
        for (int x = 0; x < mapPanel.getWidth(); x += 50) {
            g2d.drawLine(x, 0, x, mapPanel.getHeight());
        }
        for (int y = 0; y < mapPanel.getHeight(); y += 50) {
            g2d.drawLine(0, y, mapPanel.getWidth(), y);
        }
    }
    
    private void drawPlayer(Graphics2D g2d) {
        int x = (int) playerX;
        int y = (int) playerY;
        
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x - 15, y - 15, 30, 30);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - 15, y - 15, 30, 30);
        
        ImageIcon trainerImg = ImageLoader.loadImage("Trainer_M");
        if (trainerImg != null) {
            g2d.drawImage(trainerImg.getImage(), x - 12, y - 12, 24, 24, null);
        } else {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            g2d.drawString("Т", x - 6, y + 8);
        }
    }
    
    private void drawWildPokemons(Graphics2D g2d) {
        for (Pokemon p : wildPokemons) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            
            if (x >= -60 && x <= mapPanel.getWidth() + 60 && y >= -60 && y <= mapPanel.getHeight() + 60) {
                ImageIcon pokemonImg = ImageLoader.loadSmallPokemonImage(p.getName());
                if (pokemonImg != null) {
                    g2d.drawImage(pokemonImg.getImage(), x - 22, y - 22, 44, 44, null);
                } else {
                    g2d.setColor(new Color(255, 80, 80, 200));
                    g2d.fillOval(x - 20, y - 20, 40, 40);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(x - 20, y - 20, 40, 40);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString(p.getName(), x - 15, y - 25);
                }
                
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString("Lv." + p.getLevel(), x - 10, y + 30);
                
                double dist = Math.sqrt(Math.pow(p.getX() - playerX, 2) + Math.pow(p.getY() - playerY, 2));
                if (dist < 50) {
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.drawOval(x - 35, y - 35, 70, 70);
                }
            }
        }
    }
    
    private void movePlayer() {
        double speed = 5;
        double newX = playerX;
        double newY = playerY;
        
        if (upPressed) newY -= speed;
        if (downPressed) newY += speed;
        if (leftPressed) newX -= speed;
        if (rightPressed) newX += speed;
        
        newX = Math.max(20, Math.min(newX, mapPanel.getWidth() - 20));
        newY = Math.max(20, Math.min(newY, mapPanel.getHeight() - 20));
        
        if (newX != playerX || newY != playerY) {
            playerX = newX;
            playerY = newY;
            updatePlayerPosition();
        }
        
        mapPanel.repaint();
    }
    
    private void updatePlayerPosition() {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE trainers SET x = ?, y = ? WHERE id = ?");
            stmt.setDouble(1, playerX);
            stmt.setDouble(2, playerY);
            stmt.setInt(3, currentTrainer.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {}
    }
    
    private void catchNearbyPokemon() {
        if (wildPokemons == null || wildPokemons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет диких покемонов!");
            mapPanel.requestFocusInWindow();
            return;
        }
        
        Pokemon nearest = null;
        double minDist = 60;
        
        for (Pokemon p : wildPokemons) {
            double dist = Math.sqrt(Math.pow(p.getX() - playerX, 2) + Math.pow(p.getY() - playerY, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        
        if (nearest == null) {
            JOptionPane.showMessageDialog(this, "Нет покемонов рядом! Подойдите ближе!");
            mapPanel.requestFocusInWindow();
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Дикий " + nearest.getName() + " (Lv." + nearest.getLevel() + ") рядом!\n\nПоймать?",
            "Поимка покемона", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            performCatch(nearest);
        } else {
            mapPanel.requestFocusInWindow();
        }
    }
    
    private void performCatch(Pokemon p) {
        log("Ловля " + p.getName() + "...");
        
        PokemonUnitOfWork uow = new PokemonUnitOfWork(connection);
        uow.registerUpdate("UPDATE pokemons SET trainer_id = ?, is_wild = 0 WHERE id = ?", 
            currentTrainer.getId(), p.getId());
        uow.registerUpdate("UPDATE trainers SET pokemon_count = pokemon_count + 1 WHERE id = ?", 
            currentTrainer.getId());
        
        if (uow.commit()) {
            log("Успех! " + p.getName() + " пойман!");
            showCatchAnimation(p.getName());
            refreshGame();
            JOptionPane.showMessageDialog(this, p.getName() + " пойман!");
        } else {
            log("Ошибка при ловле " + p.getName());
            JOptionPane.showMessageDialog(this, "Покемон сбежал!");
        }
        
        mapPanel.requestFocusInWindow();
    }
    
    private void showCatchAnimation(String pokemonName) {
        JDialog anim = new JDialog(this, "Поимка", true);
        anim.setSize(500, 450);
        anim.setLocationRelativeTo(this);
        anim.setUndecorated(true);
        anim.setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.BLACK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel pokeballLabel = new JLabel();
        ImageIcon catchGif = ImageLoader.loadGif("pokemon-pokeball");
        if (catchGif != null) {
            pokeballLabel.setIcon(catchGif);
        } else {
            pokeballLabel.setText("ЛОВЛЯ");
            pokeballLabel.setFont(new Font("Arial", Font.BOLD, 24));
            pokeballLabel.setForeground(Color.YELLOW);
        }
        pokeballLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(pokeballLabel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel pokemonImgLabel = new JLabel();
        ImageIcon pokemonImg = ImageLoader.loadPokemonImage(pokemonName);
        if (pokemonImg != null) {
            pokemonImgLabel.setIcon(pokemonImg);
        } else {
            pokemonImgLabel.setText(pokemonName);
            pokemonImgLabel.setFont(new Font("Arial", Font.BOLD, 16));
            pokemonImgLabel.setForeground(Color.YELLOW);
        }
        pokemonImgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(pokemonImgLabel, BorderLayout.CENTER);
        
        JLabel statusLabel = new JLabel("Бросаю покебол...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setBackground(Color.BLACK);
        statusLabel.setOpaque(true);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        anim.add(mainPanel);
        
        new Thread(() -> {
            String[] messages = {"Бросаю покебол...", "Попал!", pokemonName + " пойман!", pokemonName + " добавлен в коллекцию!"};
            for (int i = 0; i < messages.length; i++) {
                final String msg = messages[i];
                SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            anim.dispose();
            SwingUtilities.invokeLater(() -> mapPanel.requestFocusInWindow());
        }).start();
        
        anim.setVisible(true);
    }
    
    private void evolvePokemon() {
        if (myPokemons == null || myPokemons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no pokemons to evolve!");
            mapPanel.requestFocusInWindow();
            return;
        }
    
        java.util.List<Pokemon> evolvable = new ArrayList<>();
        for (Pokemon p : myPokemons) {
            if (p.canEvolve()) {
                evolvable.add(p);
            }
        }
    
        if (evolvable.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pokemons ready to evolve!");
            mapPanel.requestFocusInWindow();
            return;
        }
    
        String[] names = evolvable.stream().map(p -> p.getName() + " (Lv." + p.getLevel() + ") -> " + p.getEvolutionName()).toArray(String[]::new);
        int choice = JOptionPane.showOptionDialog(this, "Choose pokemon to evolve", "Evolution",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
    
        if (choice >= 0) {
            Pokemon p = evolvable.get(choice);
            String oldName = p.getName();
            String newName = p.getEvolutionName();
            int newStage = p.getEvolutionStage() + 1;
        
            int confirm = JOptionPane.showConfirmDialog(this,
                oldName + " (Lv." + p.getLevel() + ") -> " + newName + "\n\nStart evolution?",
                "Evolution", JOptionPane.YES_NO_OPTION);
        
            if (confirm == JOptionPane.YES_OPTION) {
                showEvolutionAnimation(oldName, newName);
            
                PokemonUnitOfWork uow = new PokemonUnitOfWork(connection);
                uow.registerUpdate("UPDATE pokemons SET name = ?, evolution_stage = ?, level = level + 5, attack = attack + 15, defense = defense + 10, hp = hp + 20, max_hp = max_hp + 20 WHERE id = ?",
                    newName, newStage, p.getId());
                uow.registerUpdate("INSERT INTO achievements (trainer_id, achievement_name, is_unlocked) VALUES (?, 'Evolution Master', 1)",
                    currentTrainer.getId());
            
                if (uow.commit()) {
                    log(oldName + " evolved into " + newName + "!");
                    refreshGame();
                    JOptionPane.showMessageDialog(this, oldName + " evolved into " + newName + "!");
                } else {
                    log("Evolution failed!");
                }
            }
        }
    
        mapPanel.requestFocusInWindow();
    }
    private void showEvolutionAnimation(String oldName, String newName) {
        JDialog anim = new JDialog(this, "Эволюция", true);
        anim.setSize(700, 550);
        anim.setLocationRelativeTo(this);
        anim.setUndecorated(true);
        anim.setLayout(new BorderLayout());
    
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);
    
        JPanel pokemonPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        pokemonPanel.setBackground(Color.BLACK);
        pokemonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        JPanel oldPanel = new JPanel(new BorderLayout());
        oldPanel.setBackground(Color.BLACK);
        JLabel oldPokemonLabel = new JLabel();
        ImageIcon oldGif = ImageLoader.loadPokemonGif(oldName);
        if (oldGif != null) {
            oldPokemonLabel.setIcon(oldGif);
        } else {
            ImageIcon oldImg = ImageLoader.loadPokemonImage(oldName);
            if (oldImg != null) {
                oldPokemonLabel.setIcon(oldImg);
            } else {
                oldPokemonLabel.setText(oldName);
                oldPokemonLabel.setFont(new Font("Arial", Font.BOLD, 20));
                oldPokemonLabel.setForeground(Color.WHITE);
            }
        }
        oldPokemonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel oldNameLabel = new JLabel(oldName, SwingConstants.CENTER);
        oldNameLabel.setForeground(Color.WHITE);
        oldNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        oldPanel.add(oldPokemonLabel, BorderLayout.CENTER);
        oldPanel.add(oldNameLabel, BorderLayout.SOUTH);
    
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.BLACK);
        JLabel evolutionGifLabel = new JLabel();
        ImageIcon evoGif = ImageLoader.loadGif("light-moon-dancing");
        if (evoGif != null) {
            evolutionGifLabel.setIcon(evoGif);
        } else {
            evolutionGifLabel.setText("✨ ЭВОЛЮЦИЯ ✨");
            evolutionGifLabel.setFont(new Font("Arial", Font.BOLD, 20));
            evolutionGifLabel.setForeground(Color.YELLOW);
        }
        evolutionGifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(evolutionGifLabel, BorderLayout.CENTER);
    
        JPanel newPanel = new JPanel(new BorderLayout());
        newPanel.setBackground(Color.BLACK);
        JLabel newPokemonLabel = new JLabel();
        ImageIcon newGif = ImageLoader.loadPokemonGif(newName);
        if (newGif != null) {
            newPokemonLabel.setIcon(newGif);
        } else {
            ImageIcon newImg = ImageLoader.loadPokemonImage(newName);
            if (newImg != null) {
                newPokemonLabel.setIcon(newImg);
            } else {
                newPokemonLabel.setText(newName);
                newPokemonLabel.setFont(new Font("Arial", Font.BOLD, 20));
                newPokemonLabel.setForeground(Color.YELLOW);
            }
        }
        newPokemonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel newNameLabel = new JLabel(newName, SwingConstants.CENTER);
        newNameLabel.setForeground(Color.YELLOW);
        newNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        newPanel.add(newPokemonLabel, BorderLayout.CENTER);
        newPanel.add(newNameLabel, BorderLayout.SOUTH);
    
        pokemonPanel.add(oldPanel);
        pokemonPanel.add(centerPanel);
        pokemonPanel.add(newPanel);
    
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setForeground(Color.ORANGE);
        progress.setBackground(Color.DARK_GRAY);
    
        JLabel statusLabel = new JLabel(oldName + " эволюционирует в " + newName + "...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setBackground(Color.BLACK);
        statusLabel.setOpaque(true);
    
        mainPanel.add(pokemonPanel, BorderLayout.CENTER);
        mainPanel.add(progress, BorderLayout.SOUTH);
        mainPanel.add(statusLabel, BorderLayout.NORTH);
    
        anim.add(mainPanel);
    
        new Thread(() -> {
            for (int i = 0; i <= 100; i += 10) {
                final int val = i;
                SwingUtilities.invokeLater(() -> {
                    progress.setValue(val);
                    if (val < 30) {
                        statusLabel.setText("Накопление энергии... " + val + "%");
                    } else if (val < 70) {
                        statusLabel.setText("Трансформация... " + val + "%");
                    } else {
                        statusLabel.setText("Почти готово... " + val + "%");
                    }
                });
                try { Thread.sleep(150); } catch (InterruptedException e) {}
            }
        
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Эволюция завершена! " + oldName + " -> " + newName + "!");
            });
        
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
            anim.dispose();
        }).start();
    
        anim.setVisible(true);
    }
    
    private void tradePokemon() {
        if (myPokemons == null || myPokemons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет покемонов для обмена!");
            mapPanel.requestFocusInWindow();
            return;
        }
        
        String[] pokemonNames = myPokemons.stream().map(Pokemon::getName).toArray(String[]::new);
        int choice = JOptionPane.showOptionDialog(this, "Выберите покемона для обмена", "Обмен",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, pokemonNames, pokemonNames[0]);
        
        if (choice >= 0) {
            String targetIdStr = JOptionPane.showInputDialog(this, "Введите ID тренера (1 - Эш, 2 - Гэри):");
            if (targetIdStr != null && !targetIdStr.isEmpty()) {
                try {
                    int targetId = Integer.parseInt(targetIdStr);
                    Pokemon p = myPokemons.get(choice);
                    
                    PokemonUnitOfWork uow = new PokemonUnitOfWork(connection);
                    uow.registerUpdate("UPDATE pokemons SET trainer_id = ? WHERE id = ?", targetId, p.getId());
                    uow.registerUpdate("UPDATE trainers SET pokemon_count = pokemon_count - 1 WHERE id = ?", currentTrainer.getId());
                    uow.registerUpdate("UPDATE trainers SET pokemon_count = pokemon_count + 1 WHERE id = ?", targetId);
                    uow.registerUpdate("UPDATE trainers SET money = money + 500 WHERE id = ?", currentTrainer.getId());
                    uow.registerInsert("INSERT INTO trades (from_trainer_id, to_trainer_id, pokemon_id, status, is_completed) VALUES (?, ?, ?, 'completed', 1)",
                        currentTrainer.getId(), targetId, p.getId());
                    
                    if (uow.commit()) {
                        log("Обмен " + p.getName() + " завершён! +500 монет");
                        refreshGame();
                        JOptionPane.showMessageDialog(this, "Обмен завершён!\n+500 монет");
                    } else {
                        log("Ошибка обмена!");
                        JOptionPane.showMessageDialog(this, "Ошибка обмена!");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Неверный ID тренера!");
                }
            }
        }
        
        mapPanel.requestFocusInWindow();
    }
    
    private void startBattle() {
        if (myPokemons == null || myPokemons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "У вас нет покемонов для битвы!");
            mapPanel.requestFocusInWindow();
            return;
        }
        
        Pokemon nearest = null;
        double minDist = 100;
        
        for (Pokemon p : wildPokemons) {
            double dist = Math.sqrt(Math.pow(p.getX() - playerX, 2) + Math.pow(p.getY() - playerY, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        
        if (nearest == null) {
            JOptionPane.showMessageDialog(this, "Нет диких покемонов рядом для битвы!");
            mapPanel.requestFocusInWindow();
            return;
        }
        
        Pokemon myP = myPokemons.get(0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "БИТВА\n\n" +
            "Ваш " + myP.getName() + " (Lv." + myP.getLevel() + ")\n" +
            "VS\n" +
            "Дикий " + nearest.getName() + " (Lv." + nearest.getLevel() + ")\n\n" +
            "Начать битву?",
            "Битва", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            showBattleAnimation(myP, nearest);
            
            Random rand = new Random();
            int win = rand.nextInt(100);
            
            if (win > 20) {
                int expGain = 50 + rand.nextInt(50);
                int moneyGain = 100 + rand.nextInt(200);
                
                log("ПОБЕДА! " + myP.getName() + " получил " + expGain + " опыта и +" + moneyGain + " монет!");
                
                PokemonUnitOfWork uow = new PokemonUnitOfWork(connection);
                uow.registerUpdate("UPDATE pokemons SET exp = exp + ?, level = level + 1 WHERE id = ?", expGain, myP.getId());
                uow.registerUpdate("UPDATE trainers SET exp = exp + ?, money = money + ? WHERE id = ?", expGain / 2, moneyGain, currentTrainer.getId());
                uow.registerInsert("INSERT INTO battles (trainer1_id, trainer2_id, winner_id, reward, battle_date) VALUES (?, ?, ?, ?, datetime('now'))",
                    currentTrainer.getId(), 999, currentTrainer.getId(), moneyGain);
                
                if (uow.commit()) {
                    refreshGame();
                    JOptionPane.showMessageDialog(this, "ПОБЕДА!\n\n" + myP.getName() + " получил " + expGain + " опыта!\n+" + moneyGain + " монет!");
                }
            } else {
                log("ПОРАЖЕНИЕ! Тренируйте покемонов!");
                JOptionPane.showMessageDialog(this, "ПОРАЖЕНИЕ!\n\nТренируйте покемонов!", "Поражение", JOptionPane.WARNING_MESSAGE);
            }
        }
        
        mapPanel.requestFocusInWindow();
    }
    
   private void showBattleAnimation(Pokemon myPokemon, Pokemon opponentPokemon) {
        JDialog anim = new JDialog(this, "Битва", true);
        anim.setSize(800, 600);
        anim.setLocationRelativeTo(this);
        anim.setUndecorated(true);
        anim.setLayout(new BorderLayout());
    
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 50));
    
        JLabel vsLabel = new JLabel("БИТВА", SwingConstants.CENTER);
        vsLabel.setFont(new Font("Arial", Font.BOLD, 32));
        vsLabel.setForeground(Color.RED);
        mainPanel.add(vsLabel, BorderLayout.NORTH);
    
        JPanel battlePanel = new JPanel(new GridLayout(1, 3, 20, 20));
        battlePanel.setBackground(new Color(30, 30, 50));
        battlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        JPanel myPanel = new JPanel(new BorderLayout());
        myPanel.setBackground(new Color(30, 30, 50));
        JLabel myPokemonLabel = new JLabel();
        ImageIcon myGif = ImageLoader.loadPokemonGif(myPokemon.getName());
        if (myGif != null) {
            myPokemonLabel.setIcon(myGif);
        } else {
            ImageIcon myImg = ImageLoader.loadPokemonImage(myPokemon.getName());
            if (myImg != null) {
                myPokemonLabel.setIcon(myImg);
            } else {
                myPokemonLabel.setText(myPokemon.getName());
                myPokemonLabel.setFont(new Font("Arial", Font.BOLD, 16));
                myPokemonLabel.setForeground(Color.WHITE);
            }
        }
        myPokemonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel myNameLabel = new JLabel(myPokemon.getName() + " Lv." + myPokemon.getLevel(), SwingConstants.CENTER);
        myNameLabel.setForeground(Color.WHITE);
        myNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        myPanel.add(myPokemonLabel, BorderLayout.CENTER);
        myPanel.add(myNameLabel, BorderLayout.SOUTH);
    
        JPanel vsPanel = new JPanel(new BorderLayout());
        vsPanel.setBackground(new Color(30, 30, 50));
        JLabel battleGifLabel = new JLabel();
        ImageIcon battleGif = ImageLoader.loadGif("animation-illustration");
        if (battleGif != null) {
            battleGifLabel.setIcon(battleGif);
        } else {
            battleGifLabel.setText("VS");
            battleGifLabel.setFont(new Font("Arial", Font.BOLD, 30));
            battleGifLabel.setForeground(Color.RED);
        }
        battleGifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        vsPanel.add(battleGifLabel, BorderLayout.CENTER);
    
        JPanel opponentPanel = new JPanel(new BorderLayout());
        opponentPanel.setBackground(new Color(30, 30, 50));
        JLabel opponentPokemonLabel = new JLabel();
        ImageIcon oppGif = ImageLoader.loadPokemonGif(opponentPokemon.getName());
        if (oppGif != null) {
            opponentPokemonLabel.setIcon(oppGif);
        } else {
            ImageIcon oppImg = ImageLoader.loadPokemonImage(opponentPokemon.getName());
            if (oppImg != null) {
                opponentPokemonLabel.setIcon(oppImg);
            } else {
                opponentPokemonLabel.setText(opponentPokemon.getName());
                opponentPokemonLabel.setFont(new Font("Arial", Font.BOLD, 16));
                opponentPokemonLabel.setForeground(Color.WHITE);
            }
        }
        opponentPokemonLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel opponentNameLabel = new JLabel(opponentPokemon.getName() + " Lv." + opponentPokemon.getLevel(), SwingConstants.CENTER);
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        opponentPanel.add(opponentPokemonLabel, BorderLayout.CENTER);
        opponentPanel.add(opponentNameLabel, BorderLayout.SOUTH);
    
        battlePanel.add(myPanel);
        battlePanel.add(vsPanel);
        battlePanel.add(opponentPanel);
    
        mainPanel.add(battlePanel, BorderLayout.CENTER);
    
        JTextArea battleLog = new JTextArea(6, 40);
        battleLog.setEditable(false);
        battleLog.setBackground(Color.BLACK);
        battleLog.setForeground(Color.WHITE);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setPreferredSize(new Dimension(700, 120));
        mainPanel.add(logScroll, BorderLayout.SOUTH);
    
        anim.add(mainPanel);
    
        new Thread(() -> {
            String[] messages = {
                myPokemon.getName() + " использует быструю атаку!",
                opponentPokemon.getName() + " получает урон!",
                opponentPokemon.getName() + " использует удар!",
                myPokemon.getName() + " уклоняется!",
                myPokemon.getName() + " использует громовой удар!",
                "Критическое попадание!",
                opponentPokemon.getName() + " повержен!"
            };
        
            for (String msg : messages) {
                final String message = msg;
                SwingUtilities.invokeLater(() -> {
                    battleLog.append(message + "\n");
                    battleLog.setCaretPosition(battleLog.getDocument().getLength());
                });
                try { Thread.sleep(800); } catch (InterruptedException e) {}
            }
        
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
            anim.dispose();
        }).start();
    
        anim.setVisible(true);
    }
    private void openShop() {
        String[] items = {"Покебол (50)", "Зелье (100)", "Камень эволюции (500)"};
        String[] itemNames = {"Pokeball", "Potion", "Sinnoh_Stone"};
        int[] prices = {50, 100, 500};
        
        int choice = JOptionPane.showOptionDialog(this,
            "МАГАЗИН - Денег: " + currentTrainer.getMoney() + " монет\n\nВыберите товар:",
            "Магазин", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, items, items[0]);
        
        if (choice >= 0 && currentTrainer.getMoney() >= prices[choice]) {
            PokemonUnitOfWork uow = new PokemonUnitOfWork(connection);
            uow.registerUpdate("UPDATE trainers SET money = money - ? WHERE id = ?", prices[choice], currentTrainer.getId());
            uow.registerUpdate("UPDATE inventory SET quantity = quantity + 1 WHERE trainer_id = ? AND item_name = ?",
                currentTrainer.getId(), itemNames[choice]);
            
            if (uow.commit()) {
                log("Куплен " + items[choice]);
                refreshGame();
                JOptionPane.showMessageDialog(this, "Куплен " + items[choice]);
            } else {
                log("Ошибка покупки!");
            }
        } else if (choice >= 0) {
            JOptionPane.showMessageDialog(this, "Недостаточно денег!");
        }
        
        mapPanel.requestFocusInWindow();
    }
    
    private void refreshGame() {
        try {
            loadData();
            updatePokemonList();
            updateTrainerStats();
            mapPanel.repaint();
        } catch (SQLException e) {
            log("Ошибка обновления: " + e.getMessage());
        }
    }
    
    private void updatePokemonList() {
        pokemonPanel.removeAll();
        for (Pokemon p : myPokemons) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(new Color(70, 70, 100));
            card.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            card.setPreferredSize(new Dimension(200, 60));
            
            ImageIcon pokemonImg = ImageLoader.loadSmallPokemonImage(p.getName());
            JLabel imgLabel = new JLabel();
            if (pokemonImg != null) {
                imgLabel.setIcon(pokemonImg);
            } else {
                imgLabel.setText(p.getName());
                imgLabel.setForeground(Color.WHITE);
            }
            card.add(imgLabel, BorderLayout.WEST);
            
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setBackground(new Color(70, 70, 100));
            JLabel nameLabel = new JLabel(p.getName() + " Lv." + p.getLevel());
            nameLabel.setForeground(Color.WHITE);
            JLabel hpLabel = new JLabel("HP: " + p.getHp() + "/" + p.getMaxHp());
            hpLabel.setForeground(Color.GREEN);
            infoPanel.add(nameLabel);
            infoPanel.add(hpLabel);
            card.add(infoPanel, BorderLayout.CENTER);
            
            if (p.canEvolve()) {
                JLabel evoLabel = new JLabel("ЭВО");
                evoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                evoLabel.setForeground(Color.ORANGE);
                card.add(evoLabel, BorderLayout.EAST);
            }
            
            pokemonPanel.add(card);
        }
        pokemonPanel.revalidate();
        pokemonPanel.repaint();
    }
    
    private void updateTrainerStats() {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM trainers WHERE id = ?");
            stmt.setInt(1, currentTrainer.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentTrainer.setLevel(rs.getInt("level"));
                currentTrainer.setExp(rs.getInt("exp"));
                currentTrainer.setMoney(rs.getInt("money"));
                currentTrainer.setPokemonCount(rs.getInt("pokemon_count"));
                
                trainerLevelLabel.setText("Уровень: " + currentTrainer.getLevel());
                trainerCountLabel.setText("Покемонов: " + currentTrainer.getPokemonCount());
                trainerMoneyLabel.setText(currentTrainer.getMoney() + " монет");
            }
        } catch (SQLException e) {}
    }
    
    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}