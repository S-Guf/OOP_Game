/** Consumable item that restores HP and/or MP. */
public class Item {
    public static final Item POTION    = new Item("Potion", 50, 0, 40);
    public static final Item ETHER     = new Item("Ether", 0, 30, 50);
    public static final Item HI_POTION = new Item("Hi-Potion", 120, 0, 90);
    public static final Item[] ALL = { POTION, ETHER, HI_POTION };

    private final String name;
    private final int hp, mp, price;

    public Item(String name, int hp, int mp, int price) {
        this.name = name; this.hp = hp; this.mp = mp; this.price = price;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMp() { return mp; }
    public int getPrice() { return price; }

    public String describe() {
        String s = "";
        if (hp > 0) s += "+" + hp + " HP";
        if (mp > 0) s += (s.isEmpty() ? "" : " ") + "+" + mp + " MP";
        return s;
    }
}
