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

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setFocusPainted(false);
        b.setBackground(new Color(70, 90, 160));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        return b;
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
