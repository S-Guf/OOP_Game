import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/** Turn-based battle screen with dash / hurt-flash animations and turn lock. */
public class BattlePanel extends JPanel {
    private static final int DASH_DISTANCE = 100;

    private final GameFrame game;
    private final Player player;
    private final Monster monster;
    private final StagePanel stage = new StagePanel();
    private final JTextArea log = new JTextArea();
    private final JButton btnAttack = UI.button("ATTACK");
    private final JButton btnSkill;
    private final JButton btnItem = UI.button("ITEM");
    private final JButton btnDefend = UI.button("DEFEND");
    private Image background;

    // skill projectile + hit effect state (drawn by StagePanel)
    private Image projImg;
    private boolean projVisible = false;
    private double projX, projY;
    private int projH = 90;
    private Image fxImg;
    private int fxCx, fxCy, fxSize;
    private float fxAlpha = 1f;

    public BattlePanel(GameFrame game, Player player, Monster monster) {
        this.game = game;
        this.player = player;
        this.monster = monster;
        btnSkill = UI.button("SKILL: " + player.getSkillName() + " (" + player.getSkillCost() + " MP)");

        setLayout(new BorderLayout());
        setBackground(UI.BG);

        background = Sprites.get("Stage" + (game.getPos() + 1));   // works from folder or from inside the .jar

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.setPreferredSize(new Dimension(460, 110));
        buttons.add(btnAttack);
        buttons.add(btnSkill);
        buttons.add(btnItem);
        buttons.add(btnDefend);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(new Color(15, 17, 26));
        log.setForeground(Color.WHITE);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(log);

        JPanel south = new JPanel(new BorderLayout(12, 0));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
        south.add(buttons, BorderLayout.WEST);
        south.add(scroll, BorderLayout.CENTER);
        south.setPreferredSize(new Dimension(0, 135));

        add(stage, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        btnAttack.addActionListener(e -> doAttack(false));
        btnSkill.addActionListener(e -> doAttack(true));
        btnItem.addActionListener(e -> showItemMenu());
        btnDefend.addActionListener(e -> doDefend());

        print("A wild " + monster.getName() + " appears!");
    }

    // ------------------------------------------------------------ player commands
    private void doAttack(boolean skill) {
        if (skill && !player.canUseSkill()) { print("Not enough MP!"); return; }
        lock(true);
        final int raw;
        final boolean pierce;
        if (skill) {
            player.spendMp(player.getSkillCost());
            raw = player.skillRawDamage();
            pierce = player.skillIgnoresDef();
            print(player.getName() + " uses " + player.getSkillName() + "!");
        } else {
            raw = player.getAtk();
            pierce = false;
            print(player.getName() + " attacks!");
        }
        final Consumer<Runnable> onHit = resume -> {
            int d = monster.takeDamage(raw, pierce);
            print(monster.getName() + " takes " + d + " damage.");
            print(monster.getName() + " HP: " + monster.getHp());
            if (skill) playFx(player.getSkillHitEffect(), stage.monsterMidX(), stage.monsterMidY(), 180);
            else       playFx("skills/Impact", stage.monsterMidX(), stage.monsterMidY(), 110);
            flash(monster, resume);
        };
        if (skill) castSkill(onHit, this::afterPlayerAction);          // Warrior: sword wave / Mage: fireball
        else       dash(player, +1, onHit, this::afterPlayerAction);   // normal attack: dash
    }

    private void doDefend() {
        lock(true);
        player.defending = true;
        print(player.getName() + " defends! Incoming damage is reduced.");
        afterPlayerAction();
    }

    private void showItemMenu() {
        JPopupMenu pm = new JPopupMenu();
        for (Item it : Item.ALL) {
            int n = player.getItemCount(it);
            JMenuItem mi = new JMenuItem(it.getName() + " x" + n + "   (" + it.describe() + ")");
            mi.setIcon(Sprites.icon(it.getImageName(), 28));
            mi.setEnabled(n > 0);
            mi.addActionListener(e -> useItem(it));
            pm.add(mi);
        }
        pm.show(btnItem, 0, -pm.getPreferredSize().height);
    }

    private void useItem(Item it) {
        if (!player.useItem(it)) return;
        lock(true);
        print(player.getName() + " uses " + it.getName() + ".  HP " + player.getHp() + "/"
                + player.getMaxHp() + "  MP " + player.getMp() + "/" + player.getMaxMp());
        stage.repaint();
        afterPlayerAction();
    }

    // ------------------------------------------------------------ turn flow
    private void afterPlayerAction() {
        stage.repaint();
        if (!monster.isAlive()) {
            print(monster.getName() + " is defeated!");
            delay(800, () -> game.onBattleWon(monster));
            return;
        }
        delay(500, this::monsterTurn);
    }

    private void monsterTurn() {
        print(monster.getName() + " attacks!");
        dash(monster, -1, resume -> {
            playFx("skills/MonsterClaw", stage.heroMidX(), stage.heroMidY(), 150);
            int d = player.takeDamage(monster.rollAttack());
            print(player.getName() + " takes " + d + " damage" + (player.defending ? " (defended)" : "") + ".");
            print(player.getName() + " HP: " + player.getHp() + "/" + player.getMaxHp());
            flash(player, resume);
        }, () -> {
            player.defending = false;
            if (!player.isAlive()) {
                print(player.getName() + " has fallen...");
                delay(900, game::onBattleLost);
            } else {
                lock(false);   // turn finished -> unlock buttons
            }
        });
    }

    // ------------------------------------------------------------ animation helpers
    /** Dash forward -> onHit (damage + flash, call resume when done) -> slide back -> done. */
    private void dash(Character who, int dir, Consumer<Runnable> onHit, Runnable done) {
        Timer t = new Timer(15, null);
        t.addActionListener(new ActionListener() {
            boolean back = false;
            @Override public void actionPerformed(ActionEvent e) {
                if (!back) {
                    who.offsetX += dir * 14;
                    if (dir * who.offsetX >= DASH_DISTANCE) {
                        back = true;
                        t.stop();
                        onHit.accept(t::start);   // resume = start sliding back
                    }
                } else {
                    who.offsetX -= dir * Math.max(3, Math.abs(who.offsetX) / 6);   // ease-out
                    if (dir * who.offsetX <= 0) {
                        who.offsetX = 0;
                        t.stop();
                        done.run();
                    }
                }
                stage.repaint();
            }
        });
        t.start();
    }

    /** Skill animation: lean back, throw the projectile (sword wave / fireball) across the stage, then onHit. */
    private void castSkill(Consumer<Runnable> onHit, Runnable done) {
        projImg = Sprites.get(player.getSkillProjectile());
        projH = player.getSkillProjectile().contains("Fireball") ? 80 : 100;
        final Timer t = new Timer(15, null);
        final int[] frame = {0};
        t.addActionListener(e -> {
            int f = ++frame[0];
            if (f <= 8) {
                player.offsetX = -f * 2;                 // wind-up
            } else if (f == 9) {
                player.offsetX = 14;                     // release: thrust forward
                projVisible = true;
                projX = stage.heroFrontX();
                projY = stage.heroMidY();
            } else if (projVisible) {
                player.offsetX = Math.max(0, player.offsetX - 2);
                projX += 24;
                if (projX >= stage.monsterMidX() - monster.getSpriteW() / 4) {   // reached the enemy
                    projVisible = false;
                    player.offsetX = 0;
                    t.stop();
                    stage.repaint();
                    onHit.accept(done);
                    return;
                }
            }
            stage.repaint();
        });
        t.start();
    }

    /** Expanding + fading effect image centred at (cx, cy). Runs in parallel with other animations. */
    private void playFx(String name, int cx, int cy, int size) {
        Image img = Sprites.get(name);
        if (img == null) return;
        fxImg = img; fxCx = cx; fxCy = cy;
        final int steps = 10;
        final Timer t = new Timer(30, null);
        final int[] n = {0};
        t.addActionListener(e -> {
            n[0]++;
            float p = n[0] / (float) steps;
            fxSize = (int) (size * (0.6 + 0.7 * p));
            fxAlpha = Math.max(0f, 1f - p * p);
            if (n[0] >= steps) { fxImg = null; t.stop(); }
            stage.repaint();
        });
        t.start();
    }

    /** Blink red twice, then restore the normal colours. */
    private void flash(Character target, Runnable done) {
        Timer t = new Timer(90, null);
        int[] n = {0};
        t.addActionListener(e -> {
            n[0]++;
            target.isHurtFlash = (n[0] % 2 == 1);
            stage.repaint();
            if (n[0] >= 4) {
                target.isHurtFlash = false;
                t.stop();
                done.run();
            }
        });
        t.start();
    }

    private void delay(int ms, Runnable r) {
        Timer t = new Timer(ms, e -> r.run());
        t.setRepeats(false);
        t.start();
    }

    private void lock(boolean locked) {
        btnAttack.setEnabled(!locked);
        btnSkill.setEnabled(!locked);
        btnItem.setEnabled(!locked);
        btnDefend.setEnabled(!locked);
    }

    private void print(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    // ------------------------------------------------------------ drawing
    private class StagePanel extends JPanel {
        StagePanel() { setPreferredSize(new Dimension(900, 400)); }

        int heroX()       { return (int) (getWidth() * 0.12); }
        int monsterX()    { return getWidth() - (int) (getWidth() * 0.12) - monster.getSpriteW(); }
        int ground()      { return getHeight() - 70; }
        int heroMidX()    { return heroX() + player.offsetX + player.getSpriteW() / 2; }
        int heroMidY()    { return ground() - player.getSpriteH() / 2; }
        int heroFrontX()  { return heroX() + player.offsetX + (int) (player.getSpriteW() * 0.85); }
        int monsterMidX() { return monsterX() + monster.offsetX + monster.getSpriteW() / 2; }
        int monsterMidY() { return ground() - monster.getSpriteH() / 2; }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.drawImage(background,0, 0,w, h,this);/////
            int ground = h - 70;
            g.setColor(Color.BLACK);
            g.fillRect(0, ground, w, 70);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.drawString("VS", w / 2 - g.getFontMetrics().stringWidth("VS") / 2, 70);

            drawFighter(g, player, (int) (w * 0.12), ground, true);
            drawFighter(g, monster, w - (int) (w * 0.12) - monster.getSpriteW(), ground, false);

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (projVisible && projImg != null) {       // flying sword wave / fireball
                int ph = projH, pw = ph * projImg.getWidth(this) / Math.max(1, projImg.getHeight(this));
                g.drawImage(projImg, (int) projX - pw / 2, (int) projY - ph / 2, pw, ph, this);
            }
            if (fxImg != null) {                        // hit effect
                g.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, Math.max(0f, fxAlpha))));
                g.drawImage(fxImg, fxCx - fxSize / 2, fxCy - fxSize / 2, fxSize, fxSize, this);
            }
            g.dispose();
        }

        private void drawFighter(Graphics2D g, Character c, int x, int ground, boolean showMp) {
            int y = ground - c.getSpriteH();
            Sprites.draw(g, c.getSpriteName(), x + c.offsetX, y, c.getSpriteW(), c.getSpriteH(),
                    c.isHurtFlash, c.getPlaceholderColor());
            g.setColor(Color.WHITE);
            g.setFont(UI.BOLD);
            String title = (c instanceof Player) ? c.getName() + "  Lv." + c.getLevel() : c.getName();
            g.drawString(title, x, ground + 18);
            bar(g, x, ground + 24, c.getSpriteW(), 12, c.getHp(), c.getMaxHp(),
                    new Color(220, 60, 60), "HP " + c.getHp() + "/" + c.getMaxHp());
            if (showMp)
                bar(g, x, ground + 40, c.getSpriteW(), 12, c.getMp(), c.getMaxMp(),
                        new Color(60, 120, 230), "MP " + c.getMp() + "/" + c.getMaxMp());
        }

        private void bar(Graphics2D g, int x, int y, int w, int h, int v, int max, Color c, String text) {
            g.setColor(new Color(10, 10, 15));
            g.fillRect(x, y, w, h);
            g.setColor(c);
            g.fillRect(x, y, (int) (w * (double) v / Math.max(1, max)), h);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, w, h);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(text, x + (w - g.getFontMetrics().stringWidth(text)) / 2, y + h - 2);
        }
    }
}
