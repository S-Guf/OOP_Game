import java.util.concurrent.ThreadLocalRandom;

/** Base class of enemies. */
public abstract class Monster extends Character {
    protected final int expReward, goldReward;

    protected Monster(String name, int hp, int atk, int def, int expReward, int goldReward) {
        super(name, hp, 0, atk, def);
        this.expReward = expReward;
        this.goldReward = goldReward;
    }

    public int rollAttack() { return atk + ThreadLocalRandom.current().nextInt(5) - 2; }
    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }

    @Override protected String applyGrowth() { return ""; }
}
