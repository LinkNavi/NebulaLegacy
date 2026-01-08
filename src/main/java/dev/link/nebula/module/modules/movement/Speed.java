package dev.link.nebula.module.modules.movement;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Speed extends Module {

    private final NumberSetting multiplier;

    public Speed() {
        super("Speed", "Move faster", Category.MOVEMENT, Keyboard.KEY_NONE);

        multiplier = new NumberSetting("Multiplier", "Speed multiplier", 2.0, 1.0, 5.0, 0.1);
        addSetting(multiplier);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (mc.thePlayer.onGround && (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0)) {
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            double forward = mc.thePlayer.movementInput.moveForward;
            double strafe = mc.thePlayer.movementInput.moveStrafe;

            double speed = 0.2873 * multiplier.getValue();

            mc.thePlayer.motionX = -Math.sin(yaw) * speed * forward + Math.cos(yaw) * speed * strafe;
            mc.thePlayer.motionZ = Math.cos(yaw) * speed * forward + Math.sin(yaw) * speed * strafe;
        }
    }
}