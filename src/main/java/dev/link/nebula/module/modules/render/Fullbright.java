package dev.link.nebula.module.modules.render;

import dev.link.nebula.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Fullbright extends Module {

    private float previousGamma;

    public Fullbright() {
        super("Fullbright", "Makes everything bright", Category.RENDER, Keyboard.KEY_NONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Minecraft mc = Minecraft.getMinecraft();
        previousGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 100.0F;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.gammaSetting = previousGamma;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.gammaSetting != 100.0F) {
            mc.gameSettings.gammaSetting = 100.0F;
        }
    }
}