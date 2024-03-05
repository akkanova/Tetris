package ui;

import common.GraphicsUtils;
import common.ResourceManager;
import game.Board;
import game.Tetromino;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PlayAreaPanel extends JPanel {
    public Board board;

    private double guiScale;
    private int highestScore;
    private int screenHeight;
    private int screenWidth;
    private int blockSize;
    private Timer timer;

    public PlayAreaPanel() {
        // Use double buffered, which uses additional memory
        // space to achieve fast, flicker-free updates
        super(true);
        setFocusable(true); // A component needs to be focusable to use a KeyListener
        highestScore = ResourceManager.loadPreviousHighestScore();

        // Listen for keyboard presses when this panel is focused
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Board.GameState gameState = board.getGameState();
                int key = e.getKeyCode();

                // Whatever the gameState is, the user should
                // be allowed to forcibly restart the game
                if (key == KeyEvent.VK_F4) {
                    restart();
                    return;
                }

                // GameState Based Key Bindings

                if (gameState == Board.GameState.Stopped &&
                    key == KeyEvent.VK_ENTER) {
                    restart();
                    return;
                }

                if ((gameState == Board.GameState.Playing ||
                     gameState == Board.GameState.Paused) &&
                    (key == KeyEvent.VK_ESCAPE ||
                     key == KeyEvent.VK_F1))
                    board.pause();

                // Movement Key Bindings
                if (gameState == Board.GameState.Playing)
                    switch (key) {
                        case KeyEvent.VK_UP, KeyEvent.VK_Z -> board.rotatePieceClockWise();
                        case KeyEvent.VK_X     -> board.rotatePieceCounterClockwise();
                        case KeyEvent.VK_C     -> board.switchWithHeldPiece();
                        case KeyEvent.VK_DOWN  -> board.movePieceDown(true);
                        case KeyEvent.VK_RIGHT -> board.movePieceRight();
                        case KeyEvent.VK_LEFT  -> board.movePieceLeft();
                        case KeyEvent.VK_SPACE -> board.dropPiece();
                    }

                repaint();
            }
        });
    }

    /** Called after user presses the Enter key in the Menu */
    public void start(int width, int height, double guiScale) {
        blockSize = width / GameWindow.BLOCKS_WIDTH;
        screenHeight = height;
        screenWidth = width;
        board = new Board(
            GameWindow.BLOCKS_WIDTH - 6,
            GameWindow.BLOCKS_HEIGHT - 2
        );

        this.guiScale = guiScale;
        this.timer = new Timer(350, e -> {
            // Game loop
            if (board.getGameState() != Board.GameState.Playing)
                return;

            // Just move the current piece by one block down.
            board.movePieceDown(false);
            repaint(); // Call Java Swing UI repaint.
        });

        timer.start();
        repaint();
    }

    /** Clear timer (to avoid memory leaks) and call start again */
    public void restart() {
        timer.stop();
        timer = null;
        start(screenWidth, screenHeight, guiScale);
    }


    // Graphics Rendering

    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        GraphicsUtils utils = new GraphicsUtils(g, screenWidth, screenHeight, guiScale);

        // Draw Background
        utils.drawBackground();
        g.setColor(utils.FG_TEXT_COLOR);

        // Draw Game-Over Screen
        if (board.getGameState() == Board.GameState.Stopped) {
            int score = board.getScore();
            if (score > highestScore) {
                highestScore = score;
                ResourceManager.saveHighestScore(score);
            }

            utils.drawInterruptPage("GAME OVER", "Press Enter to Try Again.");
            utils.drawCenteredText(
                "Highest Score: " + highestScore,
                utils.PLAIN_FONT,
                screenHeight / 2 + utils.HEADER_FONT.getSize() +
                utils.PLAIN_FONT.getSize() + 15
            );
            utils.drawCenteredText(
                "Score: " + score,
                utils.PLAIN_FONT,
                screenHeight / 2 + utils.HEADER_FONT.getSize() +
                utils.PLAIN_FONT.getSize() * 2 + 15
            );
            return;
        }

        // Draw Paused Screen
        if (board.getGameState() == Board.GameState.Paused) {
            utils.drawInterruptPage("PAUSED", "Press Esc to Unpause.");
            return;
        }

        // Draw Foreground Blocks
        int columns = GameWindow.BLOCKS_WIDTH;
        int rows = GameWindow.BLOCKS_HEIGHT;

        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++) {

                int xPos = col * blockSize;
                int yPos = row * blockSize;

                // Draw the Border
                if (row == 0 || row == board.boardHeight + 1 ||
                    col == 0 || col == board.boardWidth + 1 ||
                    col == columns - 1) {

                    g.drawImage(
                        ResourceManager.loadBlockTexture("Border", blockSize),
                        xPos, yPos, blockSize, blockSize, null
                    );

                // Draw the Tetromino block
                } else if ((row <= board.boardHeight) && (col <= board.boardWidth)) {
                   Tetromino.Type type =
                       board.getBlockTypeAt(col - 1, row - 1);

                   if (type != null) g.drawImage(
                        ResourceManager.loadBlockTexture(type.name(), blockSize),
                        xPos, yPos, blockSize, blockSize, null
                   );
                }
            }
        }

        // Draw Current Tetromino Piece's Shadow
        drawTetromino(g, board.getCurrentPieceShadow(), "Shadow");

        // Draw Current Tetromino Piece
        Tetromino currentPiece = board.getCurrentPiece();
        drawTetromino(g, currentPiece, currentPiece.getType().name());

        // Draw Score & Other Text
        int sidePanelXOffset = columns - 4;
        int sidePanelXPos = (int) (blockSize * (sidePanelXOffset + 0.5));
        Font sidePanelFont = new Font("Monocraft", Font.PLAIN, (int) (10 * guiScale));

        g.setFont(sidePanelFont);
        g.drawString("Score", sidePanelXPos, blockSize + sidePanelFont.getSize());
        g.drawString(Integer.toString(board.getScore()), sidePanelXPos, blockSize + sidePanelFont.getSize() * 2);
        g.drawString("Hold", sidePanelXPos, blockSize + sidePanelFont.getSize() * 5);
        g.drawString("Next", sidePanelXPos, blockSize + sidePanelFont.getSize() * 15);

        // Draw Held Piece
        Tetromino.Type heldPieceType = board.getHeldPieceType();
        if (heldPieceType != null) {
            Tetromino heldPiece = new Tetromino(heldPieceType);
            heldPiece.setCurrentPosition(sidePanelXOffset, 4);
            drawTetromino(g, heldPiece, heldPieceType.name());
        }

        // Draw Next Pieces
        Tetromino.Type[] nextTypes = board.getNextPieces();

        for (int pieceIndex = 0; pieceIndex < 3; pieceIndex++) {
            Tetromino.Type nextType = nextTypes[pieceIndex];
            Tetromino nextPiece = new Tetromino(nextType);

            nextPiece.setCurrentPosition(sidePanelXOffset, 4 * pieceIndex + 9);
            drawTetromino(g, nextPiece, nextType.name());
        }

        g.dispose();
    }

    /**
     * Simple utils function used in paintComponent().
     * Texture refers to the image filename (not including extension) of the texture
     * within the texture pack folder.
     * */
    private void drawTetromino(Graphics2D g, Tetromino tetromino, String texture) {
        for (Point block : tetromino.getBlockCoordinates()) {
            int xPos = (block.x + 1) * blockSize;
            int yPos = (block.y + 1) * blockSize;

            g.drawImage(
                ResourceManager.loadBlockTexture(texture, blockSize),
                xPos, yPos, blockSize, blockSize, null
            );
        }
    }
}
