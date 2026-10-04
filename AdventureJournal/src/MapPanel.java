import javax.swing.*;
import java.awt.*;

/** Map: Start -> Battle -> Battle (branch to Shop) -> Battle -> Boss. */
public class MapPanel extends JPanel {
    public MapPanel(GameFrame game, Player player, int pos) {
        setLayout(new BorderLayout());
        setBackground(UI.BG);

        JLabel status = UI.label(UI.status(player), UI.BOLD, UI.GOLD);
        status.setBorder(BorderFactory.createEmptyBorder(14, 20, 6, 20));
        add(status, BorderLayout.NORTH);
        add(new MapCanvas(pos), BorderLayout.CENTER);

        JButton go = UI.button("Go to " + GameFrame.NODES[pos + 1] + "  \u25B6");
        go.addActionListener(e -> game.startBattle());
        JButton shop = UI.button("Visit Shop");
        shop.setEnabled(pos == GameFrame.SHOP_NODE);
        shop.setToolTipText("The shop is on the branch after the second battle");
        shop.addActionListener(e -> game.showShop());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        south.setOpaque(false);
        south.add(shop);
        south.add(go);
        add(south, BorderLayout.SOUTH);
    }

    private static class MapCanvas extends JPanel {
        private final int pos;

        MapCanvas(int pos) { this.pos = pos; setBackground(UI.BG); }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = GameFrame.NODES.length, bw = 110, bh = 50, margin = 40;
            int w = getWidth(), h = getHeight(), y = h / 2 - 60;
            int[] xs = new int[n];
            for (int i = 0; i < n; i++) xs[i] = margin + i * (w - 2 * margin - bw) / (n - 1);
            int sx = xs[GameFrame.SHOP_NODE], shopY = y + bh + 70;

            g.setStroke(new BasicStroke(4));
            g.setColor(new Color(130, 130, 150));
            for (int i = 0; i < n - 1; i++) g.drawLine(xs[i] + bw, y + bh / 2, xs[i + 1], y + bh / 2);
            g.drawLine(sx + bw / 2, y + bh, sx + bw / 2, shopY);

            for (int i = 0; i < n; i++) {
                Color c = i < pos ? new Color(60, 140, 80) : i == pos ? UI.GOLD : new Color(90, 94, 115);
                box(g, xs[i], y, bw, bh, GameFrame.NODES[i], c, i == pos ? Color.BLACK : Color.WHITE);
            }
            box(g, sx, shopY, bw, bh, "Shop", new Color(120, 80, 160), Color.WHITE);

            int cx = xs[pos] + bw / 2;   // "YOU" marker
            g.setColor(UI.GOLD);
            g.fillPolygon(new int[]{cx - 8, cx + 8, cx}, new int[]{y - 22, y - 22, y - 8}, 3);
            g.setFont(UI.BOLD);
            g.drawString("YOU", cx - g.getFontMetrics().stringWidth("YOU") / 2, y - 28);
            g.dispose();
        }

        private void box(Graphics2D g, int x, int y, int w, int h, String text, Color fill, Color fg) {
            g.setColor(fill);
            g.fillRoundRect(x, y, w, h, 14, 14);
            g.setColor(Color.WHITE);
            g.drawRoundRect(x, y, w, h, 14, 14);
            g.setColor(fg);
            g.setFont(UI.BOLD);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h + fm.getAscent()) / 2 - 3);
        }
    }
}
