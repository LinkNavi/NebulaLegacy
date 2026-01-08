package dev.link.nebula.module.modules.player;

import dev.link.nebula.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.PLAYER, Keyboard.KEY_NONE);
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

        // Send onGround packet when falling more than 3 blocks
        if (mc.thePlayer.fallDistance > 3.0F) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            mc.thePlayer.fallDistance = 0.0F;
        }
    }
}