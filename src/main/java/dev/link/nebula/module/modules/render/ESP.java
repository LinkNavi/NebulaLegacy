package dev.link.nebula.module.modules.render;

import dev.link.nebula.module.Module;
import dev.link.nebula.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;

public class ESP extends Module {

    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;

    public ESP() {
        super("ESP", "Highlights entities", Category.RENDER, Keyboard.KEY_NONE);

        players = new BooleanSetting("Players", "Highlight players", true);
        mobs = new BooleanSetting("Mobs", "Highlight mobs", true);
        animals = new BooleanSetting("Animals", "Highlight animals", false);

        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;

            boolean shouldRender = false;
            float red = 1.0F, green = 1.0F, blue = 1.0F;

            if (entity instanceof EntityPlayer && players.getValue()) {
                shouldRender = true;
                red = 0.0F;
                green = 1.0F;
                blue = 0.0F;
            } else if (entity instanceof EntityMob && mobs.getValue()) {
                shouldRender = true;
                red = 1.0F;
                green = 0.0F;
                blue = 0.0F;
            } else if (entity instanceof EntityAnimal && animals.getValue()) {
                shouldRender = true;
                red = 1.0F;
                green = 1.0F;
                blue = 0.0F;
            }

            if (shouldRender) {
                drawESP(entity, red, green, blue, event.partialTicks);
            }
        }
    }

    private void drawESP(Entity entity, float red, float green, float blue, float partialTicks) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        GL11.glColor4f(red, green, blue, 0.5F);
        GL11.glLineWidth(2.0F);

        double width = entity.width / 2.0;
        double height = entity.height;

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(-width, 0, -width);
        GL11.glVertex3d(width, 0, -width);
        GL11.glVertex3d(width, 0, width);
        GL11.glVertex3d(-width, 0, width);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(-width, height, -width);
        GL11.glVertex3d(width, height, -width);
        GL11.glVertex3d(width, height, width);
        GL11.glVertex3d(-width, height, width);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(-width, 0, -width);
        GL11.glVertex3d(-width, height, -width);
        GL11.glVertex3d(width, 0, -width);
        GL11.glVertex3d(width, height, -width);
        GL11.glVertex3d(width, 0, width);
        GL11.glVertex3d(width, height, width);
        GL11.glVertex3d(-width, 0, width);
        GL11.glVertex3d(-width, height, width);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}