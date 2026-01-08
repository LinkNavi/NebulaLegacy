package dev.link.nebula.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

/**
 * Utility class for managing packet operations
 */
public class PacketUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Sends a packet to the server
     * @param packet The packet to send
     */
    public static void sendPacket(Packet<?> packet) {
        if (mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
            mc.thePlayer.sendQueue.addToSendQueue(packet);
        }
    }

    /**
     * Sends a packet without triggering event handlers
     * @param packet The packet to send
     */
    public static void sendPacketNoEvent(Packet<?> packet) {
        if (mc.thePlayer != null && mc.getNetHandler() != null) {
            mc.getNetHandler().addToSendQueue(packet);
        }
    }

    /**
     * Sends multiple packets at once
     * @param packets Array of packets to send
     */
    public static void sendPackets(Packet<?>... packets) {
        for (Packet<?> packet : packets) {
            sendPacket(packet);
        }
    }

    // Movement Packets

    /**
     * Sends a position packet
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param onGround Whether the player is on ground
     */
    public static void sendPosition(double x, double y, double z, boolean onGround) {
        sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, onGround));
    }

    /**
     * Sends a rotation packet
     * @param yaw Yaw rotation
     * @param pitch Pitch rotation
     * @param onGround Whether the player is on ground
     */
    public static void sendRotation(float yaw, float pitch, boolean onGround) {
        sendPacket(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, onGround));
    }

    /**
     * Sends a full position and rotation packet
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param yaw Yaw rotation
     * @param pitch Pitch rotation
     * @param onGround Whether the player is on ground
     */
    public static void sendPositionAndRotation(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, yaw, pitch, onGround));
    }

    /**
     * Sends an onGround state packet
     * @param onGround Whether the player is on ground
     */
    public static void sendOnGround(boolean onGround) {
        sendPacket(new C03PacketPlayer(onGround));
    }

    /**
     * Sends the player's current position
     * @param onGround Whether the player is on ground
     */
    public static void sendPlayerPosition(boolean onGround) {
        if (mc.thePlayer != null) {
            sendPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, onGround);
        }
    }

    /**
     * Sends the player's current rotation
     * @param onGround Whether the player is on ground
     */
    public static void sendPlayerRotation(boolean onGround) {
        if (mc.thePlayer != null) {
            sendRotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, onGround);
        }
    }

    // Action Packets

    /**
     * Sends an entity action packet
     * @param action The action to perform
     */
    public static void sendEntityAction(C0BPacketEntityAction.Action action) {
        if (mc.thePlayer != null) {
            sendPacket(new C0BPacketEntityAction(mc.thePlayer, action));
        }
    }

    /**
     * Sends a sprint start packet
     */
    public static void sendStartSprinting() {
        sendEntityAction(C0BPacketEntityAction.Action.START_SPRINTING);
    }

    /**
     * Sends a sprint stop packet
     */
    public static void sendStopSprinting() {
        sendEntityAction(C0BPacketEntityAction.Action.STOP_SPRINTING);
    }

    /**
     * Sends a sneak start packet
     */
    public static void sendStartSneaking() {
        sendEntityAction(C0BPacketEntityAction.Action.START_SNEAKING);
    }

    /**
     * Sends a sneak stop packet
     */
    public static void sendStopSneaking() {
        sendEntityAction(C0BPacketEntityAction.Action.STOP_SNEAKING);
    }

    // Block Interaction Packets

    /**
     * Sends a block placement packet
     * @param pos Block position
     * @param facing Face being clicked
     * @param hitVec Hit vector on the block
     */
    public static void sendBlockPlacement(BlockPos pos, EnumFacing facing, Vec3 hitVec) {
        if (mc.thePlayer != null) {
            sendPacket(new C08PacketPlayerBlockPlacement(
                    pos,
                    facing.getIndex(),
                    mc.thePlayer.getHeldItem(),
                    (float) hitVec.xCoord,
                    (float) hitVec.yCoord,
                    (float) hitVec.zCoord
            ));
        }
    }

    /**
     * Sends a block dig packet
     * @param pos Block position
     * @param facing Face being dug
     * @param status Dig status
     */
    public static void sendBlockDig(BlockPos pos, EnumFacing facing, C07PacketPlayerDigging.Action status) {
        sendPacket(new C07PacketPlayerDigging(status, pos, facing));
    }

    /**
     * Sends a start digging packet
     * @param pos Block position
     * @param facing Face being dug
     */
    public static void sendStartDigging(BlockPos pos, EnumFacing facing) {
        sendBlockDig(pos, facing, C07PacketPlayerDigging.Action.START_DESTROY_BLOCK);
    }

    /**
     * Sends a stop digging packet
     * @param pos Block position
     * @param facing Face being dug
     */
    public static void sendStopDigging(BlockPos pos, EnumFacing facing) {
        sendBlockDig(pos, facing, C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK);
    }

    // Item Interaction Packets

    /**
     * Sends an animation packet (swing arm)
     */
    public static void sendAnimation() {
        sendPacket(new C0APacketAnimation());
    }

    /**
     * Sends a held item change packet
     * @param slot The slot to change to (0-8)
     */
    public static void sendHeldItemChange(int slot) {
        sendPacket(new C09PacketHeldItemChange(slot));
    }

    /**
     * Sends a use item packet
     */
    public static void sendUseItem() {
        if (mc.thePlayer != null) {
            sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
    }

    // Window/Inventory Packets

    /**
     * Sends a window click packet
     * @param windowId Window ID
     * @param slotId Slot ID
     * @param mouseButton Mouse button used
     * @param mode Click mode
     */
    public static void sendWindowClick(int windowId, int slotId, int mouseButton, int mode) {
        if (mc.thePlayer != null && mc.thePlayer.openContainer != null) {
            sendPacket(new C0EPacketClickWindow(
                    windowId,
                    slotId,
                    mouseButton,
                    mode,
                    mc.thePlayer.openContainer.getSlot(slotId).getStack(),
                    mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)
            ));
        }
    }

    /**
     * Sends a close window packet
     * @param windowId Window ID to close
     */
    public static void sendCloseWindow(int windowId) {
        sendPacket(new C0DPacketCloseWindow(windowId));
    }

    // Chat Packets

    /**
     * Sends a chat message
     * @param message The message to send
     */
    public static void sendChatMessage(String message) {
        sendPacket(new C01PacketChatMessage(message));
    }

    // Client Status Packets

    /**
     * Sends a client status packet
     * @param status The status to send
     */
    public static void sendClientStatus(C16PacketClientStatus.EnumState status) {
        sendPacket(new C16PacketClientStatus(status));
    }

    /**
     * Sends a respawn packet
     */
    public static void sendRespawn() {
        sendClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN);
    }

    /**
     * Sends an open inventory achievement packet
     */
    public static void sendOpenInventoryAchievement() {
        sendClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT);
    }

    // Utility Methods

    /**
     * Checks if the player can send packets
     * @return true if packets can be sent
     */
    public static boolean canSendPackets() {
        return mc.thePlayer != null && mc.thePlayer.sendQueue != null && mc.theWorld != null;
    }

    /**
     * Sends a packet with a delay (in ticks)
     * @param packet The packet to send
     * @param delayTicks Delay in ticks
     */
    public static void sendPacketDelayed(Packet<?> packet, int delayTicks) {
        new Thread(() -> {
            try {
                Thread.sleep(delayTicks * 50L); // 50ms per tick
                sendPacket(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Blinking utility - queues packets to send later
     */
    public static class Blink {
        private static java.util.Queue<Packet<?>> packetQueue = new java.util.LinkedList<>();
        private static boolean enabled = false;

        /**
         * Enables packet blinking (queuing)
         */
        public static void enable() {
            enabled = true;
            packetQueue.clear();
        }

        /**
         * Disables packet blinking and sends all queued packets
         */
        public static void disable() {
            enabled = false;
            while (!packetQueue.isEmpty()) {
                sendPacket(packetQueue.poll());
            }
        }

        /**
         * Adds a packet to the blink queue
         * @param packet Packet to queue
         */
        public static void addPacket(Packet<?> packet) {
            if (enabled) {
                packetQueue.add(packet);
            } else {
                sendPacket(packet);
            }
        }

        /**
         * Clears the packet queue without sending
         */
        public static void clear() {
            packetQueue.clear();
        }

        /**
         * Gets the number of queued packets
         * @return Queue size
         */
        public static int getQueueSize() {
            return packetQueue.size();
        }

        /**
         * Checks if blinking is enabled
         * @return true if enabled
         */
        public static boolean isEnabled() {
            return enabled;
        }
    }
}