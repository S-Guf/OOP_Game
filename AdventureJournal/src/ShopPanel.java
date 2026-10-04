import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** Shop: spend Gold on HP/MP restoring items. */
public class ShopPanel extends JPanel {
    private final Player player;
    private final JLabel status = UI.label("", UI.BOLD, UI.GOLD);
    private final JLabel message = UI.label(" ", UI.BODY, Color.WHITE);
    private final Map<Item, JLabel> owned = new HashMap<>();

    public ShopPanel(GameFrame game, Player player) {
        this.player = player;
        setLayout(new BorderLayout(0, 10));
        setBackground(UI.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(UI.label("SHOP", UI.TITLE, Color.WHITE));
        top.add(status);
        add(top, BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(0, 1, 8, 8));
        list.setOpaque(false);
        for (Item it : Item.ALL) {
            JPanel row = new JPanel(new BorderLayout(20, 0));
            row.setBackground(UI.PANEL);
            row.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            row.add(UI.label(it.getName() + "   " + it.describe() + "   -   " + it.getPrice() + " G",
                    UI.BODY, Color.WHITE), BorderLayout.CENTER);
            JLabel count = UI.label("", UI.BODY, UI.GOLD);
            owned.put(it, count);
            JButton buy = UI.button("Buy");
            buy.addActionListener(e -> buy(it));
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            east.setOpaque(false);
            east.add(count);
            east.add(buy);
            row.add(east, BorderLayout.EAST);
            list.add(row);
        }
        add(list, BorderLayout.CENTER);

        JButton leave = UI.button("Leave Shop");
        leave.addActionListener(e -> game.showMap());
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(message, BorderLayout.CENTER);
        south.add(leave, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);
        refresh();
    }

    private void buy(Item it) {
        if (player.spendGold(it.getPrice())) {
            player.addItem(it, 1);
            message.setText("Bought " + it.getName() + "!");
        } else {
            message.setText("Not enough gold!");
        }
        refresh();
    }

    private void refresh() {
        status.setText(UI.status(player));
        owned.forEach((it, label) -> label.setText("Owned: " + player.getItemCount(it)));
    }
}
