package dev.link.nebula.module.modules.movement;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    private final BooleanSetting omnidirectional;

    public Sprint() {
        super("Sprint", "Automatically sprint", Category.MOVEMENT, Keyboard.KEY_V);

        omnidirectional = new BooleanSetting("Omnidirectional", "Sprint in all directions", false);
        addSetting(omnidirectional);
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

        if (omnidirectional.getValue()) {
            // Sprint in all directions
            if (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0) {
                if (!mc.thePlayer.isSprinting() && !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isSneaking()) {
                    mc.thePlayer.setSprinting(true);
                }
            }
        } else {
            // Only sprint when moving forward
            if (mc.thePlayer.movementInput.moveForward > 0) {
                if (!mc.thePlayer.isSprinting() && !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isSneaking()) {
                    mc.thePlayer.setSprinting(true);
                }
            }
        }
    }
}