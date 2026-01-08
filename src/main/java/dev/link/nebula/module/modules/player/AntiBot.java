package dev.link.nebula.module.modules.player;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.BooleanSetting;
import dev.link.nebula.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AntiBot extends Module {

    private final BooleanSetting tab;
    private final BooleanSetting entityID;
    private final BooleanSetting invalidUUID;
    private final BooleanSetting livingTime;
    private final NumberSetting livingTimeTicks;
    private final BooleanSetting ground;
    private final BooleanSetting derp;
    private final BooleanSetting ping;

    private final Set<Integer> groundList = new HashSet<>();
    private final Set<UUID> botList = new HashSet<>();

    public AntiBot() {
        super("AntiBot", "Detects and ignores bots", Category.PLAYER, Keyboard.KEY_NONE);

        tab = new BooleanSetting("Tab", "Check if player is in tab list", true);
        entityID = new BooleanSetting("EntityID", "Check for invalid entity IDs", true);
        invalidUUID = new BooleanSetting("InvalidUUID", "Check for invalid UUIDs", true);
        livingTime = new BooleanSetting("LivingTime", "Check living time", false);
        livingTimeTicks = new NumberSetting("LivingTimeTicks", "Minimum ticks alive", 40.0, 1.0, 200.0, 10.0);
        ground = new BooleanSetting("Ground", "Check if touched ground", true);
        derp = new BooleanSetting("Derp", "Check for derped rotation", true);
        ping = new BooleanSetting("Ping", "Check for zero ping", false);

        addSetting(tab);
        addSetting(entityID);
        addSetting(invalidUUID);
        addSetting(livingTime);
        addSetting(livingTimeTicks);
        addSetting(ground);
        addSetting(derp);
        addSetting(ping);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        // Update ground list
        for (Object obj : mc.theWorld.playerEntities) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) obj;
                if (player.onGround && !groundList.contains(player.getEntityId())) {
                    groundList.add(player.getEntityId());
                }
            }
        }

        // Update bot list
        for (Object obj : mc.theWorld.loadedEntityList) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) obj;
                UUID uuid = player.getUniqueID();

                if (isBot(player)) {
                    if (!botList.contains(uuid)) {
                        botList.add(uuid);
                    }
                } else {
                    botList.remove(uuid);
                }
            }
        }
    }

    public boolean isBot(EntityPlayer entity) {
        Minecraft mc = Minecraft.getMinecraft();

        if (entity == null || entity == mc.thePlayer) {
            return false;
        }

        // Living time check
        if (livingTime.getValue() && entity.ticksExisted < livingTimeTicks.getValue()) {
            return true;
        }

        // Ground check
        if (ground.getValue() && !groundList.contains(entity.getEntityId())) {
            return true;
        }

        // Entity ID check
        if (entityID.getValue() && (entity.getEntityId() >= 1000000000 || entity.getEntityId() <= 0)) {
            return true;
        }

        // Derp check (invalid pitch)
        if (derp.getValue() && (entity.rotationPitch > 90F || entity.rotationPitch < -90F)) {
            return true;
        }

        // Ping check
        if (ping.getValue()) {
            try {
                if (mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfo(entity.getUniqueID()) != null) {
                    if (mc.getNetHandler().getPlayerInfo(entity.getUniqueID()).getResponseTime() == 0) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        // Invalid UUID check
        if (invalidUUID.getValue() && mc.getNetHandler() != null) {
            if (mc.getNetHandler().getPlayerInfo(entity.getUniqueID()) == null) {
                return true;
            }
        }

        // Tab check
        if (tab.getValue() && mc.getNetHandler() != null) {
            boolean inTab = false;
            try {
                if (mc.getNetHandler().getPlayerInfo(entity.getUniqueID()) != null) {
                    inTab = true;
                }
            } catch (Exception e) {
                // Not in tab
            }

            if (!inTab) {
                return true;
            }
        }

        return false;
    }

    public Set<UUID> getBotList() {
        return botList;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        groundList.clear();
        botList.clear();
    }
}