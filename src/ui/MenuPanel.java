package ui;

import common.GraphicsUtils;
import common.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MenuPanel extends JPanel {
    private final GameWindow parent;

    public MenuPanel(GameWindow gameWindow) {
        setFocusable(true); // A component needs to be focusable to use a KeyListener
        parent = gameWindow;

        // Add KeyListener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) return;

                parent.setCurrentPanel("PlayAreaPanel");
                parent.playAreaPanel.grabFocus();
                parent.playAreaPanel.start(
                        parent.getScreenWidth(),
                        parent.getScreenHeight(),
                        parent.guiScale
                );
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        // When `ui.Window.pack()` is called it resizes itself accurately according to the size of
        // its child component's (this) size. Which is weirdly more accurate than `ui.Window.setSize(w,h)`.
        // This hacky solution prevents the weird out of bounds draw issues from occurring.
        return new Dimension(
            parent.getScreenWidth(),
            parent.getScreenHeight()
        );
    }


    // Graphics Rendering because the normal swing UI sucks

    @Override
    public void paintComponent(Graphics graphics) {
        int screenWidth = parent.getScreenWidth();
        int screenHeight = parent.getScreenHeight();

        // `graphics` is actually an instance of Graphics2D since you cannot
        // create an instance of Graphics (it's an abstract class)
        Graphics2D g = (Graphics2D) graphics;
        GraphicsUtils utils = new GraphicsUtils(g, screenWidth, screenHeight, parent.guiScale);

        // Draw Background
        utils.drawBackground();

        // Draw Foreground
        g.setColor(utils.FG_TEXT_COLOR);
        utils.drawCenteredText(
            "BLOCKS",
            utils.HEADER_FONT,
            utils.HEADER_FONT.getSize() + 20
        ); // Title

        utils.drawCenteredText(
            "STACKING GAME",
            utils.SUB_HEADER_FONT,
            utils.HEADER_FONT.getSize() + utils.SUB_HEADER_FONT.getSize() + 25
        ); // Sub-Title

        int imageSize = (int) (100 * parent.guiScale);
        int imageOffset = imageSize / 2;
        g.drawImage(
            ResourceManager.loadImage("enter-key.png"),
            screenWidth / 2 - imageOffset, screenHeight / 2 - imageOffset, // X and Y Position
            imageSize, imageSize, // X and Y Size
            null
        );

        utils.drawCenteredText(
            "Press ENTER to Start.",
            utils.PLAIN_FONT,
            screenHeight / 2 + imageOffset + (int) (20 * parent.guiScale)
        );
    }
}
