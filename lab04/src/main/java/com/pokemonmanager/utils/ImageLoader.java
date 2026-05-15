package com.pokemonmanager.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {
    private static Map<String, ImageIcon> cache = new HashMap<>();
    private static String[] extensions = {"gif", "webp", "png", "jpg", "jpeg"};
    
    private static Map<String, String> fileMapping = new HashMap<>();
    
    static {
        fileMapping.put("Pikachu", "pikachu-pokemon");
        fileMapping.put("pikachu", "pikachu-pokemon");
        fileMapping.put("Raichu", "raichu");
        fileMapping.put("raichu", "raichu");
        fileMapping.put("Charmander", "charmander-gif-pokemon");
        fileMapping.put("charmander", "charmander-gif-pokemon");
        fileMapping.put("Charmeleon", "pokémon-charmeleongif");
        fileMapping.put("charmeleon", "pokémon-charmeleongif");
        fileMapping.put("Charizard", "charizard");
        fileMapping.put("charizard", "charizard");
        fileMapping.put("Squirtle", "squirtle");
        fileMapping.put("squirtle", "squirtle");
        fileMapping.put("Wartortle", "Wartortle");
        fileMapping.put("wartortle", "Wartortle");
        fileMapping.put("Blastoise", "blastoise-pokemon");
        fileMapping.put("blastoise", "blastoise-pokemon");
        fileMapping.put("Bulbasaur", "joia");
        fileMapping.put("bulbasaur", "joia");
    
        fileMapping.put("Ivysaur", "ivysaur");
        fileMapping.put("Venusaur", "venusaur");
        
        fileMapping.put("light-moon-dancing", "light-moon-dancing");
        fileMapping.put("evolution", "light-moon-dancing");
        fileMapping.put("animation-illustration", "animation-illustration");
        fileMapping.put("battle", "animation-illustration");
        fileMapping.put("pokemon-pokeball", "pokemon-pokeball");
        fileMapping.put("catch", "pokemon-pokeball");
        
        fileMapping.put("Pokeball", "Pok%3F_Ball");
        fileMapping.put("pokeball", "Pok%3F_Ball");
        fileMapping.put("Potion", "Potion");
        fileMapping.put("potion", "Potion");
        fileMapping.put("Sinnoh_Stone", "Sinnoh_Stone");
        fileMapping.put("Trainer_M", "Trainer_M");
        
        fileMapping.put("forest", "498");
        fileMapping.put("mountain", "OIP");
        fileMapping.put("water", "R");
        fileMapping.put("498", "498");
        fileMapping.put("OIP", "OIP");
        fileMapping.put("R", "R");
    }
    
    public static ImageIcon loadImage(String name) {
        if (cache.containsKey(name)) return cache.get(name);
        
        String fileName = fileMapping.getOrDefault(name, name);
        
        ImageIcon icon = tryLoad(fileName);
        if (icon != null) {
            cache.put(name, icon);
            return icon;
        }
        
        System.out.println("NOT FOUND: " + name);
        return null;
    }
    
    private static ImageIcon tryLoad(String fileName) {
        for (String ext : extensions) {
            try {
                String path = "/images/" + fileName + "." + ext;
                URL url = ImageLoader.class.getResource(path);
                if (url != null) {
                    System.out.println("Loaded: " + fileName);
                    return new ImageIcon(url);
                }
            } catch (Exception e) {}
        }
        
        for (String ext : extensions) {
            try {
                File file = new File("images/" + fileName + "." + ext);
                if (file.exists()) {
                    System.out.println("Loaded from file: " + fileName);
                    return new ImageIcon(file.getAbsolutePath());
                }
            } catch (Exception e) {}
        }
        
        return null;
    }
    
    public static ImageIcon loadPokemonGif(String name) {
        ImageIcon icon = loadImage(name);
        if (icon != null) {
            Image img = icon.getImage();
            return new ImageIcon(img);
        }
        return null;
    }
    
    public static ImageIcon loadPokemonImage(String name) {
        ImageIcon icon = loadImage(name);
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }
    
    public static ImageIcon loadSmallPokemonImage(String name) {
        ImageIcon icon = loadImage(name);
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }
    
    public static ImageIcon loadGif(String name) {
        return loadImage(name);
    }
}