package dev.link.nebula.module.modules.player;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.BooleanSetting;
import dev.link.nebula.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Eagle extends Module {

    private final BooleanSetting onlyWhenLookingDown;
    private final NumberSetting lookDownThreshold;
    private final NumberSetting maxSneakTime;

    private int sneakTimer = 0;
    private int maxSneakTicks = 0;
    private boolean sneakingByModule = false;

    public Eagle() {
        super("Eagle", "Auto sneak at edges", Category.PLAYER, Keyboard.KEY_NONE);

        onlyWhenLookingDown = new BooleanSetting("OnlyWhenLookingDown", "Only sneak when looking down", false);
        lookDownThreshold = new NumberSetting("LookDownThreshold", "Pitch threshold for looking down", 45.0, 0.0, 90.0, 5.0);
        maxSneakTime = new NumberSetting("MaxSneakTime", "Maximum sneak time in ticks", 5.0, 0.0, 20.0, 1.0);

        addSetting(onlyWhenLookingDown);
        addSetting(lookDownThreshold);
        addSetting(maxSneakTime);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        sneakTimer = 0;
        maxSneakTicks = (int) maxSneakTime.getValue();
        sneakingByModule = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft mc = Minecraft.getMinecraft();
        sneakTimer = 0;

        // If we pressed sneak for the user, release it
        if (sneakingByModule && mc != null && mc.gameSettings != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
        sneakingByModule = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // Run at END of tick (motion & collisions should be resolved)
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Don't override manual sneaking
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            if (sneakingByModule) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                sneakingByModule = false;
            }
            return;
        }

        // --- multi-point edge detection ---
        // We check three positions:
        //  1) center under player's feet
        //  2) front-left under player's hitbox
        //  3) front-right under player's hitbox
        //
        // If center is solid -> SAFE (stop sneaking).
        // Otherwise, if either front-left or front-right is air -> EDGE (start sneaking).
        // This allows placing a block under yourself (center) to immediately cancel sneak.

        // compute stable integer block coordinates for checks
        double px = mc.thePlayer.posX;
        double py = mc.thePlayer.posY;
        double pz = mc.thePlayer.posZ;

        // center block under feet (floor the coords)
        int cx = (int) Math.floor(px);
        int cy = (int) Math.floor(py - 0.2) - 1; // slightly below feet to hit the block under you
        int cz = (int) Math.floor(pz);
        BlockPos centerPos = new BlockPos(cx, cy, cz);

        // front corner checks
        double forward = 0.4;
        double side = 0.3;
        double yawRad = Math.toRadians(mc.thePlayer.rotationYaw);

        double fx = -Math.sin(yawRad) * forward;
        double fz =  Math.cos(yawRad) * forward;

        double sx =  Math.cos(yawRad) * side;
        double sz =  Math.sin(yawRad) * side;

        double leftXf = px + fx + sx;
        double leftZf = pz + fz + sz;
        double rightXf = px + fx - sx;
        double rightZf = pz + fz - sz;

        BlockPos leftPos = new BlockPos((int)Math.floor(leftXf), (int)Math.floor(py - 0.2) - 1, (int)Math.floor(leftZf));
        BlockPos rightPos = new BlockPos((int)Math.floor(rightXf), (int)Math.floor(py - 0.2) - 1, (int)Math.floor(rightZf));

        Block centerBlock = mc.theWorld.getBlockState(centerPos).getBlock();
        Block leftBlock = mc.theWorld.getBlockState(leftPos).getBlock();
        Block rightBlock = mc.theWorld.getBlockState(rightPos).getBlock();

        boolean centerSolid = centerBlock != Blocks.air;
        boolean leftAir = leftBlock == Blocks.air;
        boolean rightAir = rightBlock == Blocks.air;

        boolean atEdge = false;

        // If user wants "only when looking down", check pitch
        boolean lookOk = true;
        if (onlyWhenLookingDown.getValue()) {
            lookOk = mc.thePlayer.rotationPitch >= lookDownThreshold.getValue();
        }

        // center takes priority: if block under feet exists, consider safe
        if (!centerSolid && lookOk && (leftAir || rightAir)) {
            atEdge = true;
        }

        if (atEdge) {
            // Start sneaking immediately if we haven't already
            if (!sneakingByModule) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                sneakingByModule = true;
            }
            sneakTimer = 0;
        } else {
            if (sneakingByModule) {
                if (sneakTimer < maxSneakTicks) {
                    sneakTimer++;
                } else {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    sneakingByModule = false;
                    sneakTimer = 0;
                }
            }
        }
    }
}
