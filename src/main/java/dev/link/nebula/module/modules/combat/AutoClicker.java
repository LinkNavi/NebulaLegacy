package dev.link.nebula.module.modules.combat;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.BooleanSetting;
import dev.link.nebula.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Random;

public class AutoClicker extends Module {

    private final NumberSetting minCPS;
    private final NumberSetting maxCPS;
    private final BooleanSetting left;
    private final BooleanSetting right;
    private final BooleanSetting jitter;
    private final BooleanSetting onlyBlocks;

    private long rightDelay;
    private long rightLastSwing = 0L;
    private long leftDelay;
    private long leftLastSwing = 0L;

    private final Random random = new Random();

    public AutoClicker() {
        super("AutoClicker", "Automatically clicks for you", Category.COMBAT, Keyboard.KEY_NONE);

        minCPS = new NumberSetting("MinCPS", "Minimum clicks per second", 5.0, 1.0, 20.0, 1.0);
        maxCPS = new NumberSetting("MaxCPS", "Maximum clicks per second", 8.0, 1.0, 20.0, 1.0);
        left = new BooleanSetting("Left", "Auto click left mouse button", true);
        right = new BooleanSetting("Right", "Auto click right mouse button", true);
        jitter = new BooleanSetting("Jitter", "Add jitter effect", false);
        onlyBlocks = new BooleanSetting("OnlyBlocks", "Only right click with blocks", true);

        addSetting(minCPS);
        addSetting(maxCPS);
        addSetting(left);
        addSetting(right);
        addSetting(jitter);
        addSetting(onlyBlocks);

        rightDelay = generateClickDelay();
        leftDelay = generateClickDelay();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        rightLastSwing = 0L;
        leftLastSwing = 0L;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            return;
        }

        long time = System.currentTimeMillis();

        // Right click
        if (right.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown() && time - rightLastSwing >= rightDelay) {
            if (!onlyBlocks.getValue() || (mc.thePlayer.getHeldItem() != null &&
                    mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemBlock)) {

                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                rightLastSwing = time;
                rightDelay = generateClickDelay();
            }
        }

        // Left click
        if (left.getValue() && mc.gameSettings.keyBindAttack.isKeyDown() &&
                !mc.gameSettings.keyBindUseItem.isKeyDown() && time - leftLastSwing >= leftDelay) {

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit !=
                    net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {

                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                leftLastSwing = time;
                leftDelay = generateClickDelay();
            }
        }

        // Jitter effect
        if (jitter.getValue() && (left.getValue() && mc.gameSettings.keyBindAttack.isKeyDown() ||
                right.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown())) {

            if (random.nextBoolean()) {
                mc.thePlayer.rotationYaw += (random.nextFloat() - 0.5F) * 2F;
            }
            if (random.nextBoolean()) {
                mc.thePlayer.rotationPitch += (random.nextFloat() - 0.5F) * 2F;
            }
        }
    }

    private long generateClickDelay() {
        double minCPSValue = minCPS.getValue();
        double maxCPSValue = maxCPS.getValue();

        // Ensure min is not greater than max
        if (minCPSValue > maxCPSValue) {
            minCPSValue = maxCPSValue;
        }

        double cps = minCPSValue + (maxCPSValue - minCPSValue) * random.nextDouble();
        return (long) (1000.0 / cps);
    }
}