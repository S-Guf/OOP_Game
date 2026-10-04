import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws a character sprite. Looks for images/<Name>.png (Warrior, Mage, Goblin, Orc, Boss).
 * If the file is missing, a placeholder box with the name is drawn instead.
 */
public final class Sprites {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Sprites() {}

    private static BufferedImage load(String name) {
        if (CACHE.containsKey(name)) return CACHE.get(name);
        BufferedImage img = null;
        File f = new File("images/" + name + ".png");
        if (f.exists()) {
            try { img = ImageIO.read(f); } catch (Exception ignored) { }
        }
        CACHE.put(name, img);
        return img;
    }

    public static void draw(Graphics2D g, String name, int x, int y, int w, int h,
                            boolean flash, Color placeholderColor) {
        BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D b = buf.createGraphics();
        b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage img = load(name);
        if (img != null) {
            double s = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
            int dw = (int) (img.getWidth() * s), dh = (int) (img.getHeight() * s);
            b.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            b.drawImage(img, (w - dw) / 2, h - dh, dw, dh, null);
        } else {
            b.setColor(placeholderColor);
            b.fillRoundRect(0, 0, w, h, 16, 16);
            b.setColor(Color.WHITE);
            b.setStroke(new BasicStroke(3));
            b.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
            b.setFont(new Font("SansSerif", Font.BOLD, 18));
            FontMetrics fm = b.getFontMetrics();
            b.drawString(name, (w - fm.stringWidth(name)) / 2, h / 2);
            b.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String hint = name + ".png";
            b.drawString(hint, (w - b.getFontMetrics().stringWidth(hint)) / 2, h / 2 + 18);
        }
        if (flash) {   // tint red only where the sprite has pixels
            b.setComposite(AlphaComposite.SrcAtop.derive(0.65f));
            b.setColor(Color.RED);
            b.fillRect(0, 0, w, h);
        }
        b.dispose();
        g.drawImage(buf, x, y, null);
    }
}
