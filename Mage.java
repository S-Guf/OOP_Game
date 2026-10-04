import java.awt.Color;

/** Glass cannon: grows MP and magic ATK. Fireball ignores enemy DEF. */
public class Mage extends Player {
    public Mage() {
        super("Mage", 80, 60, 15, 8);
        color = new Color(60, 70, 160);
    }
    @Override public String getSkillName() { return "Fireball"; }
    @Override public int getSkillCost() { return 15; }
    @Override public int skillRawDamage() { return atk * 5 / 2; }
    @Override public boolean skillIgnoresDef() { return true; }

    @Override protected String applyGrowth() {
        maxHp += 10; maxMp += 12; atk += 5; def += 1;
        return "MaxHP +10, MaxMP +12, Magic ATK +5, DEF +1";
    }
}
