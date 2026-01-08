package dev.link.nebula.module.modules.render;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.BooleanSetting;
import dev.link.nebula.settings.NumberSetting;
import org.lwjgl.input.Keyboard;

public class AntiBlind extends Module {

    private final BooleanSetting confusionEffect;
    private final BooleanSetting pumpkinEffect;
    private final NumberSetting fireAlpha;
    private final BooleanSetting bossHealth;

    public AntiBlind() {
        super("AntiBlind", "Removes blinding effects", Category.RENDER, Keyboard.KEY_NONE);

        confusionEffect = new BooleanSetting("Confusion", "Remove nausea effect", true);
        pumpkinEffect = new BooleanSetting("Pumpkin", "Remove pumpkin overlay", true);
        fireAlpha = new NumberSetting("FireAlpha", "Fire overlay transparency", 0.3, 0.0, 1.0, 0.1);
        bossHealth = new BooleanSetting("BossHealth", "Remove boss health bar", true);

        addSetting(confusionEffect);
        addSetting(pumpkinEffect);
        addSetting(fireAlpha);
        addSetting(bossHealth);
    }

    public boolean shouldRemoveConfusion() {
        return confusionEffect.getValue();
    }

    public boolean shouldRemovePumpkin() {
        return pumpkinEffect.getValue();
    }

    public float getFireAlpha() {
        return (float) fireAlpha.getValue();
    }

    public boolean shouldRemoveBossHealth() {
        return bossHealth.getValue();
    }
}