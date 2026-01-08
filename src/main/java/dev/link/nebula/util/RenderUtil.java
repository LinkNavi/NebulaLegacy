package dev.link.nebula.util;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Utility class for advanced rendering operations
 */
public class RenderUtil {

    /**
     * Draws a rounded rectangle with proper corners
     */
    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_POLYGON);

        // Top right corner
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + width - radius + Math.sin(angle) * radius,
                    y + radius - Math.cos(angle) * radius);
        }

        // Bottom right corner
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + width - radius + Math.cos(angle) * radius,
                    y + height - radius + Math.sin(angle) * radius);
        }

        // Bottom left corner
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + radius - Math.sin(angle) * radius,
                    y + height - radius + Math.cos(angle) * radius);
        }

        // Top left corner
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + radius - Math.cos(angle) * radius,
                    y + radius - Math.sin(angle) * radius);
        }

        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Draws a filled circle
     */
    public static void drawFilledCircle(float x, float y, float radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);

        for (int i = 0; i <= 360; i += 6) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }

        GL11.glEnd();
    }

    /**
     * Draws a rounded rectangle outline
     */
    public static void drawRoundedRectOutline(float x, float y, float width, float height, float radius, int color, float lineWidth) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        // Top line
        GL11.glVertex2f(x + radius, y);
        GL11.glVertex2f(x + width - radius, y);

        // Top-right corner
        for (int i = 270; i <= 360; i += 6) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + width - radius + Math.cos(angle) * radius,
                    y + radius + Math.sin(angle) * radius);
        }

        // Right line
        GL11.glVertex2f(x + width, y + radius);
        GL11.glVertex2f(x + width, y + height - radius);

        // Bottom-right corner
        for (int i = 0; i <= 90; i += 6) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + width - radius + Math.cos(angle) * radius,
                    y + height - radius + Math.sin(angle) * radius);
        }

        // Bottom line
        GL11.glVertex2f(x + width - radius, y + height);
        GL11.glVertex2f(x + radius, y + height);

        // Bottom-left corner
        for (int i = 90; i <= 180; i += 6) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + radius + Math.cos(angle) * radius,
                    y + height - radius + Math.sin(angle) * radius);
        }

        // Left line
        GL11.glVertex2f(x, y + height - radius);
        GL11.glVertex2f(x, y + radius);

        // Top-left corner
        for (int i = 180; i <= 270; i += 6) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + radius + Math.cos(angle) * radius,
                    y + radius + Math.sin(angle) * radius);
        }

        GL11.glVertex2f(x + radius, y);

        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Draws a gradient rounded rectangle
     */
    public static void drawGradientRoundedRect(float x, float y, float width, float height, float radius, int color1, int color2) {
        // Draw base with gradient using polygon
        float alpha1 = (color1 >> 24 & 0xFF) / 255.0F;
        float red1 = (color1 >> 16 & 0xFF) / 255.0F;
        float green1 = (color1 >> 8 & 0xFF) / 255.0F;
        float blue1 = (color1 & 0xFF) / 255.0F;

        float alpha2 = (color2 >> 24 & 0xFF) / 255.0F;
        float red2 = (color2 >> 16 & 0xFF) / 255.0F;
        float green2 = (color2 >> 8 & 0xFF) / 255.0F;
        float blue2 = (color2 & 0xFF) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);

        // Top right corner (use color1)
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GlStateManager.color(red1, green1, blue1, alpha1);
            GL11.glVertex2d(x + width - radius + Math.sin(angle) * radius,
                    y + radius - Math.cos(angle) * radius);
        }

        // Bottom right corner (use color2)
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GlStateManager.color(red2, green2, blue2, alpha2);
            GL11.glVertex2d(x + width - radius + Math.cos(angle) * radius,
                    y + height - radius + Math.sin(angle) * radius);
        }

        // Bottom left corner (use color2)
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GlStateManager.color(red2, green2, blue2, alpha2);
            GL11.glVertex2d(x + radius - Math.sin(angle) * radius,
                    y + height - radius + Math.cos(angle) * radius);
        }

        // Top left corner (use color1)
        for (int i = 0; i <= 90; i += 5) {
            double angle = Math.toRadians(i);
            GlStateManager.color(red1, green1, blue1, alpha1);
            GL11.glVertex2d(x + radius - Math.cos(angle) * radius,
                    y + radius - Math.sin(angle) * radius);
        }

        GL11.glEnd();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}