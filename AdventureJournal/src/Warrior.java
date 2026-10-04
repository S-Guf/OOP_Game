import java.awt.Color;

/** Tanky fighter: grows HP / ATK / DEF. */
public class Warrior extends Player {
    public Warrior() {
        super("Warrior", 150, 20, 25, 20);
        color = new Color(90, 100, 125);
    }
    @Override public String getSkillName() { return "Power Strike"; }
    @Override public int getSkillCost() { return 10; }
    @Override public int skillRawDamage() { return atk * 2; }

    @Override protected String applyGrowth() {
        maxHp += 25; maxMp += 3; atk += 4; def += 3;
        return "MaxHP +25, MaxMP +3, ATK +4, DEF +3";
    }
}
