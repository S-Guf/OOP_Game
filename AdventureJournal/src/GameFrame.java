import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Main window + game state machine (Menu -> Select -> Map -> Battle/Shop -> Result). */
public class GameFrame extends JFrame {
    public static final String[] NODES = {"Start", "Battle", "Battle", "Battle", "Boss"};
    public static final int SHOP_NODE = 2;   // shop branches off the second battle node

    private Player player;
    private int pos = 0;   // index of the node the player is standing on

    public GameFrame() {
        super("Adventure Journal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);
        showMenu();
    }

    private void setScreen(JPanel p) {
        getContentPane().removeAll();
        getContentPane().add(p);
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------ screens
    public void showMenu() {
        player = null;
        pos = 0;
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.add(UI.center(UI.label("Adventure Journal", UI.TITLE, Color.WHITE)));
        col.add(UI.center(UI.label("\u0E1A\u0E31\u0E19\u0E17\u0E36\u0E01\u0E01\u0E32\u0E23\u0E1C\u0E08\u0E0D\u0E20\u0E31\u0E22", UI.HEAD, UI.GOLD)));
        col.add(Box.createVerticalStrut(40));
        JButton start = UI.center(UI.button("Start"));
        JButton exit = UI.center(UI.button("Exit"));
        start.addActionListener(e -> showSelect());
        exit.addActionListener(e -> System.exit(0));
        col.add(start);
        col.add(Box.createVerticalStrut(12));
        col.add(exit);
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UI.BG);
        root.add(col);
        setScreen(root);
    }

    public void showSelect() {
        JPanel cards = new JPanel(new GridLayout(1, 2, 40, 0));
        cards.setOpaque(false);
        cards.add(selectCard(new Warrior()));
        cards.add(selectCard(new Mage()));
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UI.BG);
        root.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        root.add(UI.center(UI.label("Choose your hero", UI.TITLE, Color.WHITE)), BorderLayout.NORTH);
        root.add(cards, BorderLayout.CENTER);
        JButton back = UI.button("Back");
        back.addActionListener(e -> showMenu());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setOpaque(false);
        south.add(back);
        root.add(south, BorderLayout.SOUTH);
        setScreen(root);
    }

    private JPanel selectCard(Player p) {
        JPanel c = new JPanel(new BorderLayout(10, 10));
        c.setBackground(UI.PANEL);
        c.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        c.add(UI.label(p.getName(), UI.HEAD, Color.WHITE), BorderLayout.NORTH);
        c.add(new SpriteView(p), BorderLayout.CENTER);
        JLabel stats = UI.label("<html>HP: " + p.getMaxHp() + "<br>MP: " + p.getMaxMp()
                + "<br>ATK: " + p.getAtk() + "<br>DEF: " + p.getDef()
                + "<br>Skill: " + p.getSkillName() + " (" + p.getSkillCost() + " MP)</html>", UI.BODY, Color.WHITE);
        JButton select = UI.button("Select");
        select.addActionListener(e -> startGame(p));
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(stats, BorderLayout.CENTER);
        bottom.add(select, BorderLayout.SOUTH);
        c.add(bottom, BorderLayout.SOUTH);
        return c;
    }

    public void startGame(Player p) {
        player = p;
        pos = 0;
        showMap();
    }

    public void showMap() { setScreen(new MapPanel(this, player, pos)); }
    public void showShop() { setScreen(new ShopPanel(this, player)); }

    public void startBattle() {
        setScreen(new BattlePanel(this, player, createMonster(pos + 1)));
    }

    private Monster createMonster(int node) {
        switch (node) {
            case 1: return new Goblin();
            case 2: return new Goblin();
            case 3: return new Orc();
            default: return new Boss();
        }
    }

    // ------------------------------------------------------------ battle results
    public void onBattleWon(Monster m) {
        List<String> lines = new ArrayList<>();
        lines.add("EXP +" + m.getExpReward());
        lines.add("GOLD +" + m.getGoldReward());
        player.addGold(m.getGoldReward());
        for (String msg : player.gainExp(m.getExpReward()))
            lines.add("<font color='#ffd54f'>" + msg + "</font>");
        lines.add(player.getName() + "  Lv." + player.getLevel()
                + "   EXP " + player.getExp() + "/" + player.getMaxExp());
        pos++;
        boolean last = pos == NODES.length - 1;
        if (last) lines.add("<b>You cleared the adventure!</b>");
        resultScreen(last ? "BOSS DEFEATED!" : "VICTORY!", new Color(120, 220, 120), lines,
                last ? "MAIN MENU" : "NEXT", last ? this::showMenu : this::showMap);
    }

    public void onBattleLost() {
        resultScreen("DEFEAT", new Color(230, 80, 80),
                List.of("Your journey ends here..."), "MAIN MENU", this::showMenu);
    }

    private void resultScreen(String title, Color tc, List<String> lines, String btnText, Runnable action) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(UI.PANEL);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tc, 3), BorderFactory.createEmptyBorder(30, 60, 30, 60)));
        box.add(UI.center(UI.label(title, UI.TITLE, tc)));
        box.add(Box.createVerticalStrut(20));
        for (String s : lines)
            box.add(UI.center(UI.label("<html><div style='text-align:center'>" + s + "</div></html>",
                    UI.BODY, Color.WHITE)));
        box.add(Box.createVerticalStrut(24));
        JButton b = UI.center(UI.button(btnText));
        b.addActionListener(e -> action.run());
        box.add(b);
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UI.BG);
        root.add(box);
        setScreen(root);
    }

    /** Sprite preview on the character-select screen. */
    private static class SpriteView extends JComponent {
        private final Character c;
        SpriteView(Character c) { this.c = c; setPreferredSize(new Dimension(170, 200)); }
        @Override protected void paintComponent(Graphics g) {
            int x = (getWidth() - c.getSpriteW()) / 2;
            Sprites.draw((Graphics2D) g, c.getSpriteName(), x, 5, c.getSpriteW(), c.getSpriteH(),
                    false, c.getPlaceholderColor());
        }
    }
}
