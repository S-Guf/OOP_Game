import javax.swing.*;
import java.awt.*;

/** Shared colours, fonts and small Swing helpers. */
public final class UI {
    public static final Color BG = new Color(24, 26, 38);
    public static final Color PANEL = new Color(38, 42, 60);
    public static final Color GOLD = new Color(255, 213, 79);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 40);
    public static final Font HEAD = new Font("SansSerif", Font.BOLD, 24);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 16);

    private UI() {}

    /** Button with a pixel/fantasy texture (images/ui/Button*.png). Falls back to flat colour if missing. */
    public static JButton button(String text) {
        JButton b = new TexturedButton(text);
        b.setFont(BOLD);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        return b;
    }

    /** JButton that paints its own texture for normal / hover / pressed / disabled states. */
    private static class TexturedButton extends JButton {
        TexturedButton(String text) { super(text); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();
            String file = !isEnabled() ? "ui/ButtonDisabled"
                    : getModel().isPressed() ? "ui/ButtonPressed"
                    : getModel().isRollover() ? "ui/ButtonHover" : "ui/Button";
            Image tex = Sprites.get(file);
            if (tex != null) {
                g.drawImage(tex, 0, 0, w, h, null);
            } else {
                g.setColor(!isEnabled() ? new Color(80, 84, 100)
                        : getModel().isPressed() ? new Color(40, 50, 110) : new Color(70, 90, 160));
                g.fillRoundRect(0, 0, w, h, 14, 14);
            }
            // text with a small drop shadow (nudged down 1px while pressed)
            String t = getText();
            Font f = getFont();
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            while (t != null && fm.stringWidth(t) > w - 16 && f.getSize() > 9) {   // shrink long labels to fit
                f = f.deriveFont((float) f.getSize() - 1);
                g.setFont(f);
                fm = g.getFontMetrics();
            }
            boolean html = t != null && t.startsWith("<html>");
            if (!html && t != null) {
                int tx = (w - fm.stringWidth(t)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2 + (getModel().isPressed() ? 1 : 0);
                g.setColor(new Color(0, 0, 0, 150));
                g.drawString(t, tx + 1, ty + 1);
                g.setColor(isEnabled() ? getForeground() : new Color(170, 170, 180));
                g.drawString(t, tx, ty);
            }
            g.dispose();
        }
    }

    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    public static <T extends JComponent> T center(T c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    public static String status(Player p) {
        return p.getName() + "  Lv." + p.getLevel() + "    HP " + p.getHp() + "/" + p.getMaxHp()
                + "    MP " + p.getMp() + "/" + p.getMaxMp()
                + "    EXP " + p.getExp() + "/" + p.getMaxExp()
                + "    Gold " + p.getGold();
    }
}
