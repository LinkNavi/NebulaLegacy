package dev.link.nebula.gui.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class CustomFontRenderer {

    private final Font font;
    private final boolean antiAlias;
    private final boolean fractionalMetrics;
    private final CharData[] charData = new CharData[256];
    private int fontHeight = -1;
    private int textureID;

    public CustomFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        setupTexture();
    }

    public CustomFontRenderer(String fontName, int size, boolean antiAlias, boolean fractionalMetrics) {
        this(new Font(fontName, Font.PLAIN, size), antiAlias, fractionalMetrics);
    }

    public static CustomFontRenderer createFromTTF(ResourceLocation location, float size) {
        try {
            IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
            InputStream inputStream = resourceManager.getResource(location).getInputStream();
            Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            font = font.deriveFont(size);
            inputStream.close();
            return new CustomFontRenderer(font, true, true);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomFontRenderer("Arial", (int) size, true, true);
        }
    }

    private void setupTexture() {
        // Create buffered image for font rendering
        BufferedImage bufferedImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

        // Set rendering hints for quality
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setFont(font);

        FontRenderContext fontRenderContext = graphics.getFontRenderContext();

        int rowHeight = 0;
        int posX = 0;
        int posY = 0;

        // Render each character
        for (int i = 0; i < charData.length; i++) {
            char c = (char) i;
            Rectangle2D bounds = font.getStringBounds(String.valueOf(c), fontRenderContext);

            int width = (int) bounds.getWidth() + 8;
            int height = (int) bounds.getHeight();

            if (posX + width >= 1024) {
                posX = 0;
                posY += rowHeight;
                rowHeight = 0;
            }

            CharData charData = new CharData();
            charData.x = posX;
            charData.y = posY;
            charData.width = width;
            charData.height = height;
            this.charData[i] = charData;

            graphics.setColor(Color.WHITE);
            graphics.drawString(String.valueOf(c), posX + 2, posY + (int) -bounds.getY());

            if (height > rowHeight) {
                rowHeight = height;
            }

            posX += width;
        }

        fontHeight = rowHeight;

        // Create OpenGL texture
        textureID = GlStateManager.generateTexture();
        uploadTexture(bufferedImage);
    }

    private void uploadTexture(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        GlStateManager.bindTexture(textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(),
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, convertToByteBuffer(pixels));
    }

    private java.nio.ByteBuffer convertToByteBuffer(int[] pixels) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(pixels.length * 4);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();
        return buffer;
    }

    public void drawString(String text, float x, float y, int color) {
        drawString(text, x, y, color, false);
    }

    public void drawStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x + 1, y + 1, 0xFF000000, false);
        drawString(text, x, y, color, false);
    }

    public void drawCenteredString(String text, float x, float y, int color) {
        drawString(text, x - getStringWidth(text) / 2f, y, color, false);
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, int color) {
        drawStringWithShadow(text, x - getStringWidth(text) / 2f, y, color);
    }

    private void drawString(String text, float x, float y, int color, boolean ignoreColor) {
        if (text == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);

        x *= 2;
        y *= 2;

        GlStateManager.bindTexture(textureID);
        GL11.glBegin(GL11.GL_QUADS);

        for (char c : text.toCharArray()) {
            if (c >= charData.length) continue;

            CharData charData = this.charData[c];
            if (charData == null) continue;

            float texX = (float) charData.x / 1024f;
            float texY = (float) charData.y / 1024f;
            float texWidth = (float) charData.width / 1024f;
            float texHeight = (float) charData.height / 1024f;

            GL11.glTexCoord2f(texX, texY);
            GL11.glVertex2f(x, y);

            GL11.glTexCoord2f(texX, texY + texHeight);
            GL11.glVertex2f(x, y + charData.height);

            GL11.glTexCoord2f(texX + texWidth, texY + texHeight);
            GL11.glVertex2f(x + charData.width, y + charData.height);

            GL11.glTexCoord2f(texX + texWidth, texY);
            GL11.glVertex2f(x + charData.width, y);

            x += charData.width - 8;
        }

        GL11.glEnd();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    public int getStringWidth(String text) {
        if (text == null) return 0;

        int width = 0;
        for (char c : text.toCharArray()) {
            if (c >= charData.length) continue;
            CharData charData = this.charData[c];
            if (charData != null) {
                width += (charData.width - 8) / 2;
            }
        }
        return width;
    }

    public int getHeight() {
        return fontHeight / 2;
    }

    private static class CharData {
        public int x;
        public int y;
        public int width;
        public int height;
    }
}