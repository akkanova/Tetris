import common.ResourceManager;
import ui.GameWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // To ensure that the ui is created on the "Swing UI" Thread, (Which might be
        // different from this one, running main) we use this function. This is also to avoid
        // abnormal issues that can occur with Java's Multi-threaded asynchronicity.
        SwingUtilities.invokeLater(() -> {
            // Load all the initial resources first.
            ResourceManager.loadFont("Monocraft");
            ResourceManager.setTexturePack("default");
            new GameWindow();
        });
    }
}