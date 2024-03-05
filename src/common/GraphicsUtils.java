package common;

import java.awt.*;

/**
 * Wraps around Graphics2D and provides common tools for rendering.
 * Mainly used by the ui Panels.
 * */
public class GraphicsUtils {
    // Constants may change based on guiScale
    public final Color FG_TEXT_COLOR = new Color(187, 187, 187); // Foreground Color
    public final Color BG_COLOR = new Color(54, 57, 63); // Background Color
    public final Font SUB_HEADER_FONT;
    public final Font HEADER_FONT;
    public final Font PLAIN_FONT;

    private final Graphics2D graphics;
    private final int screenHeight;
    private final int screenWidth;

    public GraphicsUtils(Graphics2D g, int screenWidth, int screenHeight, double guiScale) {
        SUB_HEADER_FONT = new Font("Monocraft", Font.PLAIN, (int) (15 * guiScale));
        HEADER_FONT = new Font("Monocraft", Font.BOLD, (int) (50 * guiScale));
        PLAIN_FONT = new Font("Monocraft", Font.BOLD, (int) (15 * guiScale));

        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.graphics = g;
    }

    // Basically Just Almost Self-Contained Code Snippets.

    /** Fills the entire screen with the BG_COLOR */
    public void drawBackground() {
        graphics.setColor(BG_COLOR);
        graphics.fillRect(0, 0, screenWidth, screenHeight);
    }

    /**
     * Draws centered text, by using FontMetrics to get the exact
     * middle pixel position of the Font.
     * */
    public void drawCenteredText(String text, Font font, int yPos) {
        FontMetrics metrics = graphics.getFontMetrics(font);
        int xPos = (screenWidth - metrics.stringWidth(text)) / 2;
        graphics.setFont(font);
        graphics.drawString(text, xPos, yPos);
    }

    /**
     * Interrupt Pages is basically the `Game Over` page and the `Game Paused` page.
     * They "interrupt" whatever was previously displayed on the panel.
    */
    public void drawInterruptPage(String header, String content) {
        graphics.setColor(FG_TEXT_COLOR);
        drawCenteredText(header, HEADER_FONT, screenHeight / 2);
        drawCenteredText(content, PLAIN_FONT, screenHeight / 2 + HEADER_FONT.getSize() + 10);
    }
}
