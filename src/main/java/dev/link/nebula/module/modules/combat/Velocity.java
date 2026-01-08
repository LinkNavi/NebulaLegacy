package dev.link.nebula.module.modules.combat;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.NumberSetting;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {

    private final NumberSetting horizontal;
    private final NumberSetting vertical;

    public Velocity() {
        super("Velocity", "Modify knockback taken", Category.COMBAT, Keyboard.KEY_NONE);

        horizontal = new NumberSetting("Horizontal", "Horizontal knockback %", 0, 0, 100, 10);
        vertical = new NumberSetting("Vertical", "Vertical knockback %", 0, 0, 100, 10);

        addSetting(horizontal);
        addSetting(vertical);
    }
}