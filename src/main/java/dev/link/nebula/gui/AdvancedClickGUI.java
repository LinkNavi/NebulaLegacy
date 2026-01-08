package dev.link.nebula.gui;

import dev.link.nebula.gui.font.CustomFontRenderer;
import dev.link.nebula.module.Module;
import dev.link.nebula.module.ModuleManager;
import dev.link.nebula.settings.Setting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedClickGUI extends GuiScreen {

    // Fonts
    private CustomFontRenderer titleFont;
    private CustomFontRenderer regularFont;
    private CustomFontRenderer smallFont;

    // State
    private Module.Category selectedCategory = Module.Category.COMBAT;
    private Module selectedModule = null;
    private int scrollOffset = 0;
    private float animationProgress = 0f;

    // Animation states
    private final Map<Module, Float> moduleAnimations = new HashMap<>();
    private final Map<Module.Category, Float> categoryAnimations = new HashMap<>();
    private float settingsPanelAnimation = 0f;

    // Colors - More sophisticated palette
    private final Color bgPrimary = new Color(15, 15, 20, 240);
    private final Color bgSecondary = new Color(20, 20, 28, 240);
    private final Color bgTertiary = new Color(25, 25, 35, 240);

    private final Color accentPrimary = new Color(138, 43, 226, 255); // Blue-violet
    private final Color accentSecondary = new Color(75, 0, 130, 255); // Indigo
    private final Color accentGlow = new Color(138, 43, 226, 100);

    private final Color textPrimary = new Color(255, 255, 255, 255);
    private final Color textSecondary = new Color(180, 180, 200, 255);
    private final Color textTertiary = new Color(120, 120, 140, 255);

    private final Color enabledColor = new Color(100, 200, 100, 255);
    private final Color hoverOverlay = new Color(255, 255, 255, 15);

    // Layout - More spacious and modern
    private final int categoryWidth = 140;
    private final int moduleWidth = 220;
    private final int settingsWidth = 280;
    private final int itemHeight = 36;
    private final int padding = 12;
    private final int gap = 10;
    private final int cornerRadius = 10;
    private final int shadowSize = 20;

    private int leftPanelX, leftPanelY, leftPanelHeight;
    private int middlePanelX, middlePanelY, middlePanelHeight;
    private int rightPanelX, rightPanelY, rightPanelHeight;

    @Override
    public void initGui() {
        // Initialize custom fonts
        titleFont = new CustomFontRenderer("Segoe UI", 20, true, true);
        regularFont = new CustomFontRenderer("Segoe UI", 16, true, true);
        smallFont = new CustomFontRenderer("Segoe UI", 14, true, true);

        // Initialize animations
        for (Module.Category category : Module.Category.values()) {
            categoryAnimations.put(category, 0f);
        }
        for (Module module : ModuleManager.getInstance().getModules()) {
            moduleAnimations.put(module, 0f);
        }

        // Calculate layout
        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        int totalWidth = categoryWidth + moduleWidth + gap * 2;
        int totalHeight = 450;

        leftPanelX = centerX - totalWidth / 2;
        leftPanelY = centerY - totalHeight / 2;
        leftPanelHeight = totalHeight;

        middlePanelX = leftPanelX + categoryWidth + gap;
        middlePanelY = leftPanelY;
        middlePanelHeight = totalHeight;

        rightPanelX = middlePanelX + moduleWidth + gap;
        rightPanelY = leftPanelY;
        rightPanelHeight = totalHeight;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Update animations
        updateAnimations();

        // Draw background overlay with blur effect
        drawGradientRect(0, 0, width, height,
                new Color(0, 0, 0, 100).getRGB(),
                new Color(0, 0, 0, 150).getRGB());

        // Draw main panels with shadows
        drawCategoryPanel(mouseX, mouseY);
        drawModulePanel(mouseX, mouseY);

        // Draw settings panel with animation
        if (selectedModule != null && !selectedModule.getSettings().isEmpty()) {
            settingsPanelAnimation = Math.min(1f, settingsPanelAnimation + 0.15f);
            drawSettingsPanel(mouseX, mouseY);
        } else {
            settingsPanelAnimation = Math.max(0f, settingsPanelAnimation - 0.15f);
            if (settingsPanelAnimation > 0) {
                drawSettingsPanel(mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateAnimations() {
        animationProgress += 0.05f;
        if (animationProgress > 1f) animationProgress = 0f;

        // Update category animations
        for (Module.Category category : Module.Category.values()) {
            float target = category == selectedCategory ? 1f : 0f;
            float current = categoryAnimations.get(category);
            categoryAnimations.put(category, smoothLerp(current, target, 0.2f));
        }

        // Update module animations
        for (Module module : ModuleManager.getInstance().getModules()) {
            float target = module.isEnabled() ? 1f : 0f;
            float current = moduleAnimations.get(module);
            moduleAnimations.put(module, smoothLerp(current, target, 0.15f));
        }
    }

    private float smoothLerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    private void drawCategoryPanel(int mouseX, int mouseY) {
        // Draw panel shadow
        drawShadow(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight, shadowSize);

        // Draw panel background with gradient
        drawGradientRoundedRect(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight,
                cornerRadius, bgPrimary.getRGB(), bgSecondary.getRGB());

        // Draw border with glow
        drawRoundedRectOutline(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight,
                cornerRadius, accentPrimary.getRGB(), 2);

        // Draw header
        titleFont.drawCenteredStringWithShadow("CATEGORIES",
                leftPanelX + categoryWidth / 2f, leftPanelY + 15, textPrimary.getRGB());

        // Draw separator line with gradient
        drawHorizontalGradientLine(leftPanelX + 15, leftPanelX + categoryWidth - 15,
                leftPanelY + 45, accentPrimary.getRGB(), accentSecondary.getRGB());

        // Draw categories
        int yOffset = leftPanelY + 60;
        for (Module.Category category : Module.Category.values()) {
            boolean hovered = isHovered(mouseX, mouseY, leftPanelX + padding, yOffset,
                    categoryWidth - padding * 2, itemHeight);
            float anim = categoryAnimations.get(category);

            // Draw category background
            int x = leftPanelX + padding;
            int width = categoryWidth - padding * 2;

            if (anim > 0.01f) {
                // Selected/animated state
                int glowColor = new Color(
                        accentGlow.getRed(),
                        accentGlow.getGreen(),
                        accentGlow.getBlue(),
                        (int)(accentGlow.getAlpha() * anim)
                ).getRGB();

                drawRoundedRect(x - 2, yOffset - 2, width + 4, itemHeight + 4,
                        cornerRadius, glowColor);

                drawGradientRoundedRect(x, yOffset, width, itemHeight, cornerRadius,
                        interpolateColor(bgTertiary.getRGB(), accentSecondary.getRGB(), anim * 0.3f),
                        interpolateColor(bgTertiary.getRGB(), accentPrimary.getRGB(), anim * 0.3f));
            } else if (hovered) {
                drawRoundedRect(x, yOffset, width, itemHeight, cornerRadius,
                        hoverOverlay.getRGB());
            }

            // Draw category icon (simple circle for now)
            int iconX = x + 12;
            int iconY = yOffset + itemHeight / 2;
            drawCircle(iconX, iconY, 4, interpolateColor(textTertiary.getRGB(),
                    accentPrimary.getRGB(), anim));

            // Draw category name
            regularFont.drawStringWithShadow(category.getName(),
                    iconX + 12, yOffset + (itemHeight - regularFont.getHeight()) / 2f,
                    interpolateColor(textSecondary.getRGB(), textPrimary.getRGB(), anim));

            yOffset += itemHeight + 5;
        }
    }

    private void drawModulePanel(int mouseX, int mouseY) {
        // Draw panel shadow
        drawShadow(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight, shadowSize);

        // Draw panel background
        drawGradientRoundedRect(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight,
                cornerRadius, bgPrimary.getRGB(), bgSecondary.getRGB());

        // Draw border
        drawRoundedRectOutline(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight,
                cornerRadius, accentPrimary.getRGB(), 2);

        // Draw header
        titleFont.drawCenteredStringWithShadow(selectedCategory.getName().toUpperCase(),
                middlePanelX + moduleWidth / 2f, middlePanelY + 15, textPrimary.getRGB());

        // Draw separator
        drawHorizontalGradientLine(middlePanelX + 15, middlePanelX + moduleWidth - 15,
                middlePanelY + 45, accentPrimary.getRGB(), accentSecondary.getRGB());

        // Enable scissor for scrolling
        enableScissor(middlePanelX, middlePanelY + 50, moduleWidth, middlePanelHeight - 50);

        // Draw modules
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        int yOffset = middlePanelY + 60 - scrollOffset;

        for (Module module : modules) {
            if (yOffset > middlePanelY + middlePanelHeight) break;
            if (yOffset + itemHeight < middlePanelY + 50) {
                yOffset += itemHeight + 6;
                continue;
            }

            boolean hovered = isHovered(mouseX, mouseY, middlePanelX + padding, yOffset,
                    moduleWidth - padding * 2, itemHeight);
            boolean selected = module == selectedModule;
            float anim = moduleAnimations.get(module);

            int x = middlePanelX + padding;
            int width = moduleWidth - padding * 2;

            // Draw module background with animation
            if (anim > 0.01f) {
                // Draw glow for enabled modules
                int glowColor = new Color(
                        enabledColor.getRed(),
                        enabledColor.getGreen(),
                        enabledColor.getBlue(),
                        (int)(50 * anim)
                ).getRGB();
                drawRoundedRect(x - 2, yOffset - 2, width + 4, itemHeight + 4,
                        cornerRadius, glowColor);
            }

            // Background color
            int bgColor = bgTertiary.getRGB();
            if (selected) {
                bgColor = interpolateColor(bgTertiary.getRGB(), accentSecondary.getRGB(), 0.2f);
            } else if (hovered) {
                bgColor = new Color(bgTertiary.getRed() + 10, bgTertiary.getGreen() + 10,
                        bgTertiary.getBlue() + 10, bgTertiary.getAlpha()).getRGB();
            }

            drawRoundedRect(x, yOffset, width, itemHeight, cornerRadius, bgColor);

            // Draw left accent bar for enabled modules
            if (anim > 0.01f) {
                drawRoundedRect(x + 3, yOffset + 8, 3, itemHeight - 16, 2,
                        interpolateColor(textTertiary.getRGB(), enabledColor.getRGB(), anim));
            }

            // Draw module name
            int textX = x + (anim > 0.01f ? 15 : 10);
            regularFont.drawStringWithShadow(module.getName(),
                    textX, yOffset + 6, textPrimary.getRGB());

            // Draw module description
            smallFont.drawString(module.getDescription(),
                    textX, yOffset + 22, textTertiary.getRGB());

            // Draw enabled indicator
            if (module.isEnabled()) {
                String status = "ON";
                int statusWidth = regularFont.getStringWidth(status);
                int statusX = x + width - statusWidth - 10;
                int statusY = yOffset + (itemHeight - regularFont.getHeight()) / 2;

                // Draw status background
                drawRoundedRect(statusX - 4, statusY - 2, statusWidth + 8,
                        regularFont.getHeight() + 4, 4,
                        new Color(enabledColor.getRed(), enabledColor.getGreen(),
                                enabledColor.getBlue(), 40).getRGB());

                regularFont.drawString(status, statusX, statusY, enabledColor.getRGB());
            }

            // Draw settings indicator
            if (!module.getSettings().isEmpty()) {
                int dotX = x + width - 8;
                int dotY = yOffset + itemHeight - 8;
                drawCircle(dotX, dotY, 2, selected ? accentPrimary.getRGB() : textTertiary.getRGB());
            }

            yOffset += itemHeight + 6;
        }

        disableScissor();

        // Draw scroll indicator
        if (modules.size() * (itemHeight + 6) > middlePanelHeight - 60) {
            drawScrollbar(middlePanelX + moduleWidth - 8, middlePanelY + 50,
                    4, middlePanelHeight - 50, scrollOffset,
                    modules.size() * (itemHeight + 6) - (middlePanelHeight - 60));
        }
    }

    private void drawSettingsPanel(int mouseX, int mouseY) {
        if (selectedModule == null) return;

        int x = (int)(rightPanelX - (1f - settingsPanelAnimation) * 30);
        int alpha = (int)(240 * settingsPanelAnimation);

        // Draw panel shadow
        if (settingsPanelAnimation > 0.5f) {
            drawShadow(x, rightPanelY, settingsWidth, rightPanelHeight, shadowSize);
        }

        // Draw panel background
        Color bg1 = new Color(bgPrimary.getRed(), bgPrimary.getGreen(),
                bgPrimary.getBlue(), alpha);
        Color bg2 = new Color(bgSecondary.getRed(), bgSecondary.getGreen(),
                bgSecondary.getBlue(), alpha);
        drawGradientRoundedRect(x, rightPanelY, settingsWidth, rightPanelHeight,
                cornerRadius, bg1.getRGB(), bg2.getRGB());

        // Draw border
        Color border = new Color(accentPrimary.getRed(), accentPrimary.getGreen(),
                accentPrimary.getBlue(), (int)(255 * settingsPanelAnimation));
        drawRoundedRectOutline(x, rightPanelY, settingsWidth, rightPanelHeight,
                cornerRadius, border.getRGB(), 2);

        // Draw header
        titleFont.drawCenteredStringWithShadow(selectedModule.getName().toUpperCase(),
                x + settingsWidth / 2f, rightPanelY + 15,
                new Color(textPrimary.getRed(), textPrimary.getGreen(),
                        textPrimary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

        smallFont.drawCenteredString("SETTINGS",
                x + settingsWidth / 2f, rightPanelY + 35,
                new Color(textTertiary.getRed(), textTertiary.getGreen(),
                        textTertiary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

        // Draw separator
        Color sep1 = new Color(accentPrimary.getRed(), accentPrimary.getGreen(),
                accentPrimary.getBlue(), (int)(255 * settingsPanelAnimation));
        Color sep2 = new Color(accentSecondary.getRed(), accentSecondary.getGreen(),
                accentSecondary.getBlue(), (int)(255 * settingsPanelAnimation));
        drawHorizontalGradientLine(x + 15, x + settingsWidth - 15, rightPanelY + 52,
                sep1.getRGB(), sep2.getRGB());

        // Draw settings
        int yOffset = rightPanelY + 65;
        for (Setting setting : selectedModule.getSettings()) {
            if (yOffset > rightPanelY + rightPanelHeight - 40) break;

            boolean hovered = isHovered(mouseX, mouseY, x + padding, yOffset,
                    settingsWidth - padding * 2, itemHeight + 5);

            int settingX = x + padding;
            int settingWidth = settingsWidth - padding * 2;

            // Draw setting background
            Color settingBg = new Color(bgTertiary.getRed(), bgTertiary.getGreen(),
                    bgTertiary.getBlue(), (int)(alpha * 0.8f));
            if (hovered) {
                settingBg = new Color(bgTertiary.getRed() + 15, bgTertiary.getGreen() + 15,
                        bgTertiary.getBlue() + 15, (int)(alpha * 0.8f));
            }
            drawRoundedRect(settingX, yOffset, settingWidth, itemHeight + 5, cornerRadius,
                    settingBg.getRGB());

            // Draw setting name
            regularFont.drawStringWithShadow(setting.getName(),
                    settingX + 10, yOffset + 5,
                    new Color(textPrimary.getRed(), textPrimary.getGreen(),
                            textPrimary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

            // Draw setting value with background
            String value = setting.getValueAsString();
            int valueWidth = regularFont.getStringWidth(value);
            int valueX = settingX + settingWidth - valueWidth - 15;

            drawRoundedRect(valueX - 5, yOffset + 19, valueWidth + 10,
                    regularFont.getHeight() + 4, 4,
                    new Color(accentSecondary.getRed(), accentSecondary.getGreen(),
                            accentSecondary.getBlue(), (int)(60 * settingsPanelAnimation)).getRGB());

            regularFont.drawString(value, valueX, yOffset + 21,
                    new Color(accentPrimary.getRed(), accentPrimary.getGreen(),
                            accentPrimary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

            yOffset += itemHeight + 10;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Category selection
        int yOffset = leftPanelY + 60;
        for (Module.Category category : Module.Category.values()) {
            if (isHovered(mouseX, mouseY, leftPanelX + padding, yOffset,
                    categoryWidth - padding * 2, itemHeight)) {
                selectedCategory = category;
                scrollOffset = 0;
                return;
            }
            yOffset += itemHeight + 5;
        }

        // Module interaction
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        yOffset = middlePanelY + 60 - scrollOffset;

        for (Module module : modules) {
            if (yOffset > middlePanelY + middlePanelHeight) break;
            if (yOffset + itemHeight < middlePanelY + 50) {
                yOffset += itemHeight + 6;
                continue;
            }

            if (isHovered(mouseX, mouseY, middlePanelX + padding, yOffset,
                    moduleWidth - padding * 2, itemHeight)) {
                if (mouseButton == 0) {
                    module.toggle();
                } else if (mouseButton == 1) {
                    selectedModule = (selectedModule == module) ? null : module;
                }
                return;
            }
            yOffset += itemHeight + 6;
        }

        // Settings interaction
        if (selectedModule != null && settingsPanelAnimation > 0.5f) {
            yOffset = rightPanelY + 65;
            int x = (int)(rightPanelX - (1f - settingsPanelAnimation) * 30);

            for (Setting setting : selectedModule.getSettings()) {
                if (yOffset > rightPanelY + rightPanelHeight - 40) break;

                if (isHovered(mouseX, mouseY, x + padding, yOffset,
                        settingsWidth - padding * 2, itemHeight + 5)) {
                    if (mouseButton == 0) {
                        setting.increment();
                    } else if (mouseButton == 1) {
                        setting.decrement();
                    }
                    return;
                }
                yOffset += itemHeight + 10;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset -= wheel > 0 ? 40 : -40;
            List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
            int maxScroll = Math.max(0, modules.size() * (itemHeight + 6) - (middlePanelHeight - 60));
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ===== RENDERING UTILITIES =====

    private void enableScissor(int x, int y, int width, int height) {
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (y + height) * scale,
                width * scale, height * scale);
    }

    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawScrollbar(int x, int y, int width, int height, int scroll, int maxScroll) {
        if (maxScroll <= 0) return;

        // Draw track
        drawRoundedRect(x, y, width, height, width / 2,
                new Color(40, 40, 50, 100).getRGB());

        // Calculate thumb size and position
        int thumbHeight = Math.max(20, (int)((float)height * ((float)height / (height + maxScroll))));
        int thumbY = y + (int)((float)(height - thumbHeight) * ((float)scroll / maxScroll));

        // Draw thumb
        drawRoundedRect(x, thumbY, width, thumbHeight, width / 2, accentPrimary.getRGB());
    }

    private void drawShadow(int x, int y, int width, int height, int size) {
        // Simple shadow using gradient rectangles
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Top shadow
        drawGradientRect(x - size, y - size, x + width + size, y,
                0, new Color(0, 0, 0, 40).getRGB());

        // Bottom shadow
        drawGradientRect(x - size, y + height, x + width + size, y + height + size,
                new Color(0, 0, 0, 40).getRGB(), 0);

        // Left shadow
        drawGradientRect(x - size, y, x, y + height,
                0, new Color(0, 0, 0, 40).getRGB());

        // Right shadow
        drawGradientRect(x + width, y, x + width + size, y + height,
                new Color(0, 0, 0, 40).getRGB(), 0);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawGradientRoundedRect(int x, int y, int width, int height, int radius,
                                         int color1, int color2) {
        drawRoundedRect(x, y, width, height, radius, color1);

        // Overlay gradient
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        drawGradientRect(x + radius, y, x + width - radius, y + height, color1, color2);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
        drawRect(x + radius, y, x + width - radius, y + height, color);
        drawRect(x, y + radius, x + width, y + height - radius, color);

        drawCircle(x + radius, y + radius, radius, color);
        drawCircle(x + width - radius, y + radius, radius, color);
        drawCircle(x + radius, y + height - radius, radius, color);
        drawCircle(x + width - radius, y + height - radius, radius, color);
    }

    private void drawRoundedRectOutline(int x, int y, int width, int height, int radius,
                                        int color, int lineWidth) {
        for (int i = 0; i < lineWidth; i++) {
            drawHorizontalLine(x + radius, x + width - radius, y + i, color);
            drawHorizontalLine(x + radius, x + width - radius, y + height - i - 1, color);
            drawVerticalLine(x + i, y + radius, y + height - radius, color);
            drawVerticalLine(x + width - i - 1, y + radius, y + height - radius, color);
        }
    }

    private void drawHorizontalGradientLine(int x1, int x2, int y, int color1, int color2) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        drawGradientRect(x1, y, x2, y + 1, color1, color2);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawCircle(int x, int y, int radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255f;
        float red = (color >> 16 & 0xFF) / 255f;
        float green = (color >> 8 & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);

        for (int i = 0; i <= 360; i += 10) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }

        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private int interpolateColor(int color1, int color2, float t) {
        t = Math.max(0, Math.min(1, t));

        int a1 = (color1 >> 24 & 0xFF);
        int r1 = (color1 >> 16 & 0xFF);
        int g1 = (color1 >> 8 & 0xFF);
        int b1 = (color1 & 0xFF);

        int a2 = (color2 >> 24 & 0xFF);
        int r2 = (color2 >> 16 & 0xFF);
        int g2 = (color2 >> 8 & 0xFF);
        int b2 = (color2 & 0xFF);

        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}