package dev.link.nebula.module.modules.player;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

public class Reach extends Module {

    private final NumberSetting combatReach;
    private final NumberSetting buildReach;

    public Reach() {
        super("Reach", "Extends your reach distance", Category.PLAYER, Keyboard.KEY_NONE);

        combatReach = new NumberSetting("CombatReach", "Reach for attacking entities", 3.5, 3.0, 7.0, 0.1);
        buildReach = new NumberSetting("BuildReach", "Reach for placing blocks", 5.0, 4.5, 7.0, 0.1);

        addSetting(combatReach);
        addSetting(buildReach);
    }

    public double getCombatReach() {
        return combatReach.getValue();
    }

    public double getBuildReach() {
        return buildReach.getValue();
    }

    public double getMaxRange() {
        return Math.max(combatReach.getValue(), buildReach.getValue());
    }
}