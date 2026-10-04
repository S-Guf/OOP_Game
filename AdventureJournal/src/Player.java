import java.util.LinkedHashMap;
import java.util.Map;

/** Base class of playable heroes: adds gold, inventory and a class skill. */
public abstract class Player extends Character {
    protected int gold = 100;
    private final Map<Item, Integer> inventory = new LinkedHashMap<>();

    protected Player(String name, int hp, int mp, int atk, int def) {
        super(name, hp, mp, atk, def);
        for (Item it : Item.ALL) inventory.put(it, 0);
        inventory.put(Item.POTION, 3);
        inventory.put(Item.ETHER, 1);
    }

    public abstract String getSkillName();
    public abstract int getSkillCost();
    public abstract int skillRawDamage();
    public boolean skillIgnoresDef() { return false; }
    public boolean canUseSkill() { return mp >= getSkillCost(); }

    public int getGold() { return gold; }
    public void addGold(int g) { gold += g; }
    public boolean spendGold(int g) {
        if (gold < g) return false;
        gold -= g;
        return true;
    }

    public int getItemCount(Item it) { return inventory.getOrDefault(it, 0); }
    public void addItem(Item it, int n) { inventory.put(it, getItemCount(it) + n); }

    public boolean useItem(Item it) {
        int n = getItemCount(it);
        if (n <= 0) return false;
        inventory.put(it, n - 1);
        heal(it.getHp(), it.getMp());
        return true;
    }
}
