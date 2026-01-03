package dev.link.nebula.handler;

import dev.link.nebula.gui.ClickGUI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyBindHandler {

    private static final int GUI_KEY = Keyboard.KEY_RSHIFT;

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(GUI_KEY)) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGUI());
        }
    }
}