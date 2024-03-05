package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/** Board holding tetrominoes and core game logic */
public class Board {
    private final ArrayList<Tetromino.Type> bag; // Contains the Next pieces
    private final Tetromino.Type[][] grid; // Static Blocks location (For collision and rendering)

    private Tetromino.Type heldPiece; // You don't need to store its location, so storing the type is a simpler
    private Tetromino currentPiece;
    private GameState gameState; // Mainly to signify to the UI the user has lost, or paused the game
    private boolean heldPieceLock; // You can only switch with the held piece once per block.
    private int score;

    public final int boardWidth;
    public final int boardHeight;

    public enum GameState {
        Playing,
        Paused,
        Stopped
    }

    public Board(int width, int height) {
        gameState = GameState.Playing;
        boardHeight = height;
        boardWidth = width;
        score = 0;

        grid = new Tetromino.Type[height][width];
        bag = new ArrayList<>();
        generateNewPiece();
    }

    /** Pause and Unpause the game */
    public void pause() {
        gameState = gameState == GameState.Playing
            ? GameState.Paused
            : GameState.Playing;
    }

    /**
     * Called when a new piece needs to be generated for the board.
     * This function also makes sure that there is perfect randomness
     * and diversity to future tetrominoes; so you don't get "I"
     * pieces all the time or "Z"s.
     * */
    public void generateNewPiece() {
        // Keep the bag full
        if (bag.size() < 5) {
            // List out all possible Tetromino types
            ArrayList<Tetromino.Type> allTypes = new ArrayList<>();
            Collections.addAll(allTypes, Tetromino.Type.values());
            // The bag should contain all possible types
            // With a completely random sequence they appear in.
            Collections.shuffle(allTypes);
            Collections.addAll(bag, allTypes.toArray(Tetromino.Type[]::new));
        }

        // Replace the currentPiece with a new type from the bag
        initializeWithType(bag.get(0));
        bag.remove(0);
    }

    /**
     *  Switch with the current held piece.
     *  Or if there's no current held piece, just store the current piece
     *  and do nothing, but replace the current piece with a new one.
     * */
    public void switchWithHeldPiece() {
        // Can only be used once per block.
        // Unlocked when currentPiece is attached to the grid.
        if (heldPieceLock) return;
        heldPieceLock = true;

        Tetromino.Type currentPieceType = currentPiece.getType();
        if (heldPiece == null) {
            // At the start of the game heldPiece is null
            // so, we can't really switch it with the currentPiece.
            heldPiece = currentPieceType;
            generateNewPiece();
            return;
        }

        initializeWithType(heldPiece);
        heldPiece = currentPieceType;
    }
    
    /**
     * Places a tetromino in the current starting
     * position with the type provided
     * */
    private void initializeWithType(Tetromino.Type type) {
        currentPiece = null;
        currentPiece = new Tetromino(type);

        int yPos = 1;
        int xPos = boardWidth / 2;

        Point[] initialPosition = currentPiece.translate(xPos, yPos);
        // If It can't put the tetromino in the initial position it
        // usually means Game Over.
        if (!doesCollide(initialPosition))
            currentPiece.setCurrentPosition(xPos, yPos);

        else gameState = GameState.Stopped;
    }

    /** Remove any full rows within the grid and add that as a point for the user */
    public void cleanupRows() {
        ArrayList<Integer> rowsToClear = new ArrayList<>();
        for (int row = boardHeight - 1; row > 0; row--) {
            // Count how many columns of that row is filled
            int colFilled = 0;
            for (Tetromino.Type col : grid[row])
                if (col != null) colFilled++;

            // If the Row is full add it to the rowsToClear queue
            if (colFilled >= boardWidth)
                rowsToClear.add(row);
        }

        // Starting from the top-most full row
        Collections.reverse(rowsToClear);
        for (int fullRowIndex : rowsToClear) {
            score += 100 + (50 * rowsToClear.size());

            // Go up from the full row and move all the subsequent row down,
            // Which will overwrite the full row.
            for (int row = fullRowIndex - 1; row > 0; row--)
                System.arraycopy(grid[row], 0, grid[row + 1], 0, boardWidth);
        }
    }

    /**
     * Check whether the given coordinates overlap
     * with other blocks in the grid or it is out of bounds
     * */
    public boolean doesCollide(Point[] newCoordinates) {
        for (Point block : newCoordinates) {

            if (block.x < 0 || block.x > boardWidth - 1 ||
                block.y < 0 || block.y > boardHeight - 1)
                return true;

            if (grid[block.y][block.x] != null)
                return true;
        }

        return false;
    }


    // Movements (Mainly called by UI)

    public void dropPiece() {
        // The default value should be the currentPiece
        dropPiece(currentPiece, true);
    }

    /** Keep moving the target piece until it collides with something */
    private void dropPiece(Tetromino target, boolean addScore) {
        // Keep Dropping the Piece
        // until it collides with something
        while (true)
            if (movePiece(target, 0, 1, addScore)) break;
    }

    /**
     * If forced is true, the invocation of the function is considered a
     * user input and a point is added to the user's score.
     * */
    public void movePieceDown(boolean forced) { movePiece(currentPiece, 0, 1, forced); }
    public void movePieceRight() { movePiece(currentPiece, 1, 0, false); }
    public void movePieceLeft() { movePiece(currentPiece, -1, 0, false); }

    /**
     * Returns true if it collided with something.
     * If it collides something while moving down, and the target tetromino
     * is the current piece, convert the current into a static block within
     * the grid, and generate a new piece.
     * */
    private boolean movePiece(Tetromino target, int deltaX, int deltaY, boolean addScore) {
        Point[] newCoordinates = target.translate(deltaX, deltaY);
        boolean doesCollideWithGrid = doesCollide(newCoordinates);

        if (!doesCollideWithGrid) {
            if (addScore) score += 1;
            Point currentPosition = target.getCurrentPosition();
            target.setCurrentPosition(
                currentPosition.x + deltaX,
                currentPosition.y + deltaY
            );
        }

        // If the provided tetromino is the currentPiece and not the
        // Shadow tetromino, attach it to the static block grid.
        if (target.equals(currentPiece) && doesCollideWithGrid && deltaY > 0) {
            // Convert the current piece blocks into a static block within the grid
            for (Point block : target.getBlockCoordinates())
                grid[block.y][block.x] = currentPiece.getType();
            
            cleanupRows();
            generateNewPiece();
            heldPieceLock = false; // Unlock Held Piece Lock
        }

        return doesCollideWithGrid;
    }

    public void rotatePieceCounterClockwise() { rotatePiece(false); }
    public void rotatePieceClockWise() { rotatePiece(true); }

    private void rotatePiece(boolean clockwise) {
        if (currentPiece == null) return;
        // Rotation doesn't make sense for a square.
        if (currentPiece.getType() == Tetromino.Type.Square) return;

        Point currentPosition = currentPiece.getCurrentPosition();
        Point[] newOffsets = currentPiece.rotate(clockwise);
        Point[] newCoordinates = Tetromino.addOffsetsAndPosition(newOffsets, currentPosition);

        if (!doesCollide(newCoordinates))
            currentPiece.setBlocksOffsets(newOffsets);

        // If it does collide with something try doing a Wall Kick
        else {
            // Try moving Left
            currentPosition.x += 1;
            newCoordinates = Tetromino.addOffsetsAndPosition(newOffsets, currentPosition);
            if (!doesCollide(newCoordinates)) {
                currentPiece.setBlocksOffsets(newOffsets);
                return;
            }

            // Try moving Right
            currentPosition.x -= 2;
            newCoordinates = Tetromino.addOffsetsAndPosition(newOffsets, currentPosition);
            if (!doesCollide(newCoordinates))
                currentPiece.setBlocksOffsets(newOffsets);
        }
    }


    // Getters
    
    public Tetromino.Type getHeldPieceType() {
        return heldPiece;
    }

    public Tetromino.Type getBlockTypeAt(int x, int y) {
        return grid[y][x];
    }

    public Tetromino.Type[] getNextPieces() {
        return bag.toArray(Tetromino.Type[]::new);
    }

    public GameState getGameState() {
        return gameState;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    /** Shadow refers to the predicted landing place of the currentPiece */
    public Tetromino getCurrentPieceShadow() {
        Tetromino shadow = currentPiece.duplicate();
        dropPiece(shadow, false);
        return shadow;
    }

    public int getScore() {
        return score;
    }
}
