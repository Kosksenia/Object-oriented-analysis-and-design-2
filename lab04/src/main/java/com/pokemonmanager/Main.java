package com.pokemonmanager;

import com.pokemonmanager.game.PokemonManagerGameWithUOW;
import com.pokemonmanager.game.PokemonManagerGameWithoutUOW;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        String[] options = {"WITH Unit of Work (SAFE)", "WITHOUT Unit of Work (RISK)"};
        int choice = JOptionPane.showOptionDialog(null,
            "POKEMON MANAGER\n\nSelect game mode:\n\nWITH pattern - atomic transactions\nWITHOUT pattern - data may corrupt",
            "Pokemon Manager", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        
        if (choice == 0) new PokemonManagerGameWithUOW();
        else new PokemonManagerGameWithoutUOW();
    }
}