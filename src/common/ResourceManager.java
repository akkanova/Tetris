package common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * A static methods class that loads the images and other files required.
 * Kind of like a File Manager.
 * */
public class ResourceManager {
    private static final String baseDir = "res/"; // Base Resource Files Directory (Prefix)
    private static final String highestScoreFile = baseDir + "highest-score";
    private static String texturePack;

    /**
     * `Texture pack` refers to the folder location within
     * the baseDir of the 20px by 20px texture blocks used by
     * PlayAreaPanel
     * */
    public static void setTexturePack(String texturePack) {
        ResourceManager.texturePack = texturePack;
    }

    /**
     * Like loadImage but automatically prefixes the texture pack
     * directory and suffixes the image file extension (.png)
     * */
    public static BufferedImage loadBlockTexture(String block, int blockSize) {
        // Create a higher scale of the imported image
        BufferedImage originalImage = loadImage(texturePack + "/" + block + ".png");
        BufferedImage newImage = new BufferedImage(
            blockSize, blockSize, BufferedImage.TYPE_4BYTE_ABGR
        );

        Graphics2D g2d = newImage.createGraphics();
        // https://docs.oracle.com/javase/tutorial/2d/advanced/quality.html
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(originalImage, 0, 0, blockSize, blockSize, null);
        g2d.dispose();

        return newImage;
    }

    /**
     * Does Exception handling and baseDir prefixing for
     * importing images located within the baseDir
     * */
    public static BufferedImage loadImage(String filename) {
        String fullPath = baseDir + filename;
        try {
            return ImageIO.read(new File(fullPath));
        } catch (IOException e) {
            System.err.println("Failed to load image: " + fullPath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks your system if you already installed
     * that font, then registers it for you if it isn't.
     * Do not include the extension (.tff) when specifying `name`
     * */
    public static void loadFont(String name) {
        // First check whether that font exists
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String fontName : ge.getAvailableFontFamilyNames())
            if (fontName.equals(name)) return;

        String fullPath = baseDir + name + ".ttf";
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fullPath));
            ge.registerFont(font);

        } catch (IOException e) {
            System.err.println("Failed to load font: " + fullPath);
            e.printStackTrace();

        } catch (FontFormatException e) {
            System.err.println("Font " + fullPath + " is invalid");
            e.printStackTrace();
        }
    }

    /** Reads the Highest Score file and returns its integer content */
    public static int loadPreviousHighestScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(highestScoreFile))) {
            return Integer.parseInt(reader.readLine());

        } catch (FileNotFoundException e) {
            // It is normal to not exists at the very beginning.
            return 0;

        } catch (IOException e) {
            System.err.println("Cannot open file " + highestScoreFile);
            e.printStackTrace();
            return 0;
        }
    }

    /** Saves the highest score under the file location of `res/highest-score` */
    public static void saveHighestScore(int highestScore) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(highestScoreFile))) {
            writer.write(Integer.toString(highestScore));

        } catch (IOException e) {
            System.err.println("Cannot write to file " + highestScoreFile);
            e.printStackTrace();
        }
    }
}
