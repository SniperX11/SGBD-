package com.sgbd;

import com.sgbd.ui.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Launches the Swing UI on the Event Dispatch Thread (EDT) as required
 * by the Swing threading model.
 */
public class Main {

    public static void main(String[] args) {
        // Use the system look-and-feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Falls back to the default cross-platform L&F
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
