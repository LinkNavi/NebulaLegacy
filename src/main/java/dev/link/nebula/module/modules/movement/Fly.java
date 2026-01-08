package dev.link.nebula.module.modules.movement;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.ModeSetting;
import dev.link.nebula.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {

    private final ModeSetting mode;
    private final NumberSetting speed;

    public Fly() {
        super("Fly", "Allows you to fly", Category.MOVEMENT, Keyboard.KEY_F);

        mode = new ModeSetting("Mode", "Flight mode", "Vanilla", "Vanilla", "Creative");
        speed = new NumberSetting("Speed", "Flight speed", 1.0, 0.1, 5.0, 0.1);

        addSetting(mode);
        addSetting(speed);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.capabilities.isFlying = false;
            mc.thePlayer.capabilities.allowFlying = false;
        }
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

        if (mode.getMode().equals("Vanilla")) {
            mc.thePlayer.capabilities.isFlying = false;
            mc.thePlayer.motionY = 0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = speed.getValue();
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.thePlayer.motionY = -speed.getValue();
            }

            if (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0) {
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                double forward = mc.thePlayer.movementInput.moveForward;
                double strafe = mc.thePlayer.movementInput.moveStrafe;

                mc.thePlayer.motionX = -Math.sin(yaw) * speed.getValue() * forward + Math.cos(yaw) * speed.getValue() * strafe;
                mc.thePlayer.motionZ = Math.cos(yaw) * speed.getValue() * forward + Math.sin(yaw) * speed.getValue() * strafe;
            } else {
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
            }
        } else if (mode.getMode().equals("Creative")) {
            mc.thePlayer.capabilities.allowFlying = true;
            mc.thePlayer.capabilities.setFlySpeed((float) (speed.getValue() / 10.0));
        }
    }
}