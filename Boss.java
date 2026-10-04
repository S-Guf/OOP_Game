import java.awt.Color;

public class Boss extends Monster {
    public Boss() {
        super("Boss", 300, 34, 12, 200, 400);
        color = new Color(140, 30, 40);
        spriteW = 210; spriteH = 260;
    }
}
