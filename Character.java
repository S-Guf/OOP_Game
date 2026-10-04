import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Base class of every fighter (Player and Monster). */
public abstract class Character {
    private static final Random RND = new Random();

    protected String name;
    protected int level = 1, exp = 0, maxExp = 100;
    protected int hp, maxHp, mp, maxMp, atk, def;

    // sprite / animation state (used by the UI)
    protected int spriteW = 150, spriteH = 190;
    protected Color color = Color.GRAY;      // placeholder colour
    public int offsetX = 0;                  // dash animation offset
    public boolean isHurtFlash = false;      // red flash when hurt
    public boolean defending = false;        // Defend command active

    protected Character(String name, int hp, int mp, int atk, int def) {
        this.name = name;
        this.hp = this.maxHp = hp;
        this.mp = this.maxMp = mp;
        this.atk = atk;
        this.def = def;
    }

    /** Stat growth for this class. Returns a text describing the gains. */
    protected abstract String applyGrowth();

    public int takeDamage(int rawAttack) { return takeDamage(rawAttack, false); }

    /** Damage = raw - DEF/2 (+-2 random). Halved while defending. Min 1. */
    public int takeDamage(int rawAttack, boolean ignoreDef) {
        int dmg = rawAttack - (ignoreDef ? 0 : def / 2) + RND.nextInt(5) - 2;
        dmg = Math.max(1, dmg);
        if (defending) dmg = Math.max(1, dmg / 2);
        hp = Math.max(0, hp - dmg);
        return dmg;
    }

    /** Adds EXP, levels up as many times as needed, returns one message per level-up. */
    public List<String> gainExp(int amount) {
        List<String> msgs = new ArrayList<>();
        exp += amount;
        while (exp >= maxExp) msgs.add(levelUp());
        return msgs;
    }

    public String levelUp() {
        exp -= maxExp;
        maxExp = (int) Math.round(maxExp * 1.5);   // EXP curve: 100, 150, 225, ...
        int old = level++;
        String gains = applyGrowth();
        hp = maxHp;                                  // full heal on level up
        mp = maxMp;
        return "LEVEL UP! Lv." + old + " -> Lv." + level + " : " + gains + " (HP/MP fully restored)";
    }

    public void heal(int h, int m) {
        hp = Math.min(maxHp, hp + h);
        mp = Math.min(maxMp, mp + m);
    }

    public void spendMp(int cost) { mp = Math.max(0, mp - cost); }
    public boolean isAlive() { return hp > 0; }

    public String getName() { return name; }
    public String getSpriteName() { return getClass().getSimpleName(); }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getMaxExp() { return maxExp; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getSpriteW() { return spriteW; }
    public int getSpriteH() { return spriteH; }
    public Color getPlaceholderColor() { return color; }
}
