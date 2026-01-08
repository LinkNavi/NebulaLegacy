package dev.link.nebula.gui;

import dev.link.nebula.gui.font.CustomFontRenderer;
import dev.link.nebula.module.Module;
import dev.link.nebula.module.ModuleManager;
import dev.link.nebula.settings.Setting;
import dev.link.nebula.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

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
    private final Color bgPrimary = new Color(15, 15, 20, 255);
    private final Color bgSecondary = new Color(20, 20, 28, 255);
    private final Color bgTertiary = new Color(25, 25, 35, 255);

    private final Color accentPrimary = new Color(138, 43, 226, 255);
    private final Color accentSecondary = new Color(75, 0, 130, 255);
    private final Color accentGlow = new Color(138, 43, 226, 100);

    private final Color textPrimary = new Color(255, 255, 255, 255);
    private final Color textSecondary = new Color(180, 180, 200, 255);
    private final Color textTertiary = new Color(120, 120, 140, 255);

    private final Color enabledColor = new Color(100, 200, 100, 255);
    private final Color hoverOverlay = new Color(255, 255, 255, 15);

    // Layout
    private final int categoryWidth = 140;
    private final int moduleWidth = 220;
    private final int settingsWidth = 280;
    private final int itemHeight = 36;
    private final int padding = 12;
    private final int gap = 10;
    private final int cornerRadius = 10;

    private int leftPanelX, leftPanelY, leftPanelHeight;
    private int middlePanelX, middlePanelY, middlePanelHeight;
    private int rightPanelX, rightPanelY, rightPanelHeight;

    @Override
    public void initGui() {
        titleFont = new CustomFontRenderer("Segoe UI", 20, true, true);
        regularFont = new CustomFontRenderer("Segoe UI", 16, true, true);
        smallFont = new CustomFontRenderer("Segoe UI", 14, true, true);

        for (Module.Category category : Module.Category.values()) {
            categoryAnimations.put(category, 0f);
        }
        for (Module module : ModuleManager.getInstance().getModules()) {
            moduleAnimations.put(module, 0f);
        }

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
        updateAnimations();

        // Draw darker background overlay
        drawGradientRect(0, 0, width, height,
                new Color(0, 0, 0, 180).getRGB(),
                new Color(0, 0, 0, 200).getRGB());

        drawCategoryPanel(mouseX, mouseY);
        drawModulePanel(mouseX, mouseY);

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

        for (Module.Category category : Module.Category.values()) {
            float target = category == selectedCategory ? 1f : 0f;
            float current = categoryAnimations.get(category);
            categoryAnimations.put(category, smoothLerp(current, target, 0.2f));
        }

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
        RenderUtil.drawGradientRoundedRect(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight,
                cornerRadius, bgPrimary.getRGB(), bgSecondary.getRGB());
        RenderUtil.drawRoundedRectOutline(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight,
                cornerRadius, accentPrimary.getRGB(), 2);

        // Enable proper text rendering
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        titleFont.drawCenteredStringWithShadow("CATEGORIES",
                leftPanelX + categoryWidth / 2f, leftPanelY + 15, textPrimary.getRGB());

        drawHorizontalLine(leftPanelX + 15, leftPanelX + categoryWidth - 15, leftPanelY + 45, accentPrimary.getRGB());

        int yOffset = leftPanelY + 60;
        for (Module.Category category : Module.Category.values()) {
            boolean hovered = isHovered(mouseX, mouseY, leftPanelX + padding, yOffset,
                    categoryWidth - padding * 2, itemHeight);
            float anim = categoryAnimations.get(category);

            int x = leftPanelX + padding;
            int width = categoryWidth - padding * 2;

            if (anim > 0.01f) {
                int glowColor = new Color(
                        accentGlow.getRed(),
                        accentGlow.getGreen(),
                        accentGlow.getBlue(),
                        (int)(accentGlow.getAlpha() * anim)
                ).getRGB();

                RenderUtil.drawRoundedRect(x - 2, yOffset - 2, width + 4, itemHeight + 4,
                        cornerRadius, glowColor);

                RenderUtil.drawGradientRoundedRect(x, yOffset, width, itemHeight, cornerRadius,
                        interpolateColor(bgTertiary.getRGB(), accentSecondary.getRGB(), anim * 0.3f),
                        interpolateColor(bgTertiary.getRGB(), accentPrimary.getRGB(), anim * 0.3f));
            } else if (hovered) {
                RenderUtil.drawRoundedRect(x, yOffset, width, itemHeight, cornerRadius,
                        hoverOverlay.getRGB());
            }

            // Re-enable textures for icon
            GlStateManager.enableTexture2D();

            int iconX = x + 12;
            int iconY = yOffset + itemHeight / 2;
            RenderUtil.drawFilledCircle(iconX, iconY, 4, interpolateColor(textTertiary.getRGB(),
                    accentPrimary.getRGB(), anim));

            // Re-enable textures for text
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            regularFont.drawStringWithShadow(category.getName(),
                    iconX + 12, yOffset + (itemHeight - regularFont.getHeight()) / 2f,
                    interpolateColor(textSecondary.getRGB(), textPrimary.getRGB(), anim));

            yOffset += itemHeight + 5;
        }
    }

    private void drawModulePanel(int mouseX, int mouseY) {
        RenderUtil.drawGradientRoundedRect(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight,
                cornerRadius, bgPrimary.getRGB(), bgSecondary.getRGB());
        RenderUtil.drawRoundedRectOutline(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight,
                cornerRadius, accentPrimary.getRGB(), 2);

        // Enable proper text rendering
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        titleFont.drawCenteredStringWithShadow(selectedCategory.getName().toUpperCase(),
                middlePanelX + moduleWidth / 2f, middlePanelY + 15, textPrimary.getRGB());

        drawHorizontalLine(middlePanelX + 15, middlePanelX + moduleWidth - 15,
                middlePanelY + 45, accentPrimary.getRGB());

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

            if (anim > 0.01f) {
                int glowColor = new Color(
                        enabledColor.getRed(),
                        enabledColor.getGreen(),
                        enabledColor.getBlue(),
                        (int)(50 * anim)
                ).getRGB();
                RenderUtil.drawRoundedRect(x - 2, yOffset - 2, width + 4, itemHeight + 4,
                        cornerRadius, glowColor);
            }

            int bgColor = bgTertiary.getRGB();
            if (selected) {
                bgColor = interpolateColor(bgTertiary.getRGB(), accentSecondary.getRGB(), 0.2f);
            } else if (hovered) {
                bgColor = new Color(bgTertiary.getRed() + 10, bgTertiary.getGreen() + 10,
                        bgTertiary.getBlue() + 10, bgTertiary.getAlpha()).getRGB();
            }

            RenderUtil.drawRoundedRect(x, yOffset, width, itemHeight, cornerRadius, bgColor);

            if (anim > 0.01f) {
                RenderUtil.drawRoundedRect(x + 3, yOffset + 8, 3, itemHeight - 16, 2,
                        interpolateColor(textTertiary.getRGB(), enabledColor.getRGB(), anim));
            }

            // Re-enable textures for text rendering
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            int textX = x + (anim > 0.01f ? 15 : 10);
            regularFont.drawStringWithShadow(module.getName(),
                    textX, yOffset + 6, textPrimary.getRGB());

            smallFont.drawString(module.getDescription(),
                    textX, yOffset + 22, textTertiary.getRGB());

            if (module.isEnabled()) {
                String status = "ON";
                int statusWidth = regularFont.getStringWidth(status);
                int statusX = x + width - statusWidth - 10;
                int statusY = yOffset + (itemHeight - regularFont.getHeight()) / 2;

                RenderUtil.drawRoundedRect(statusX - 4, statusY - 2, statusWidth + 8,
                        regularFont.getHeight() + 4, 4,
                        new Color(enabledColor.getRed(), enabledColor.getGreen(),
                                enabledColor.getBlue(), 40).getRGB());

                // Re-enable textures for status text
                GlStateManager.enableTexture2D();
                GlStateManager.enableBlend();

                regularFont.drawString(status, statusX, statusY, enabledColor.getRGB());
            }

            if (!module.getSettings().isEmpty()) {
                // Re-enable textures before drawing circle
                GlStateManager.enableTexture2D();

                int dotX = x + width - 8;
                int dotY = yOffset + itemHeight - 8;
                RenderUtil.drawFilledCircle(dotX, dotY, 2, selected ? accentPrimary.getRGB() : textTertiary.getRGB());
            }

            yOffset += itemHeight + 6;
        }
    }

    private void drawSettingsPanel(int mouseX, int mouseY) {
        if (selectedModule == null) return;

        int x = (int)(rightPanelX - (1f - settingsPanelAnimation) * 30);
        int alpha = (int)(240 * settingsPanelAnimation);

        Color bg1 = new Color(bgPrimary.getRed(), bgPrimary.getGreen(),
                bgPrimary.getBlue(), alpha);
        Color bg2 = new Color(bgSecondary.getRed(), bgSecondary.getGreen(),
                bgSecondary.getBlue(), alpha);
        RenderUtil.drawGradientRoundedRect(x, rightPanelY, settingsWidth, rightPanelHeight,
                cornerRadius, bg1.getRGB(), bg2.getRGB());

        Color border = new Color(accentPrimary.getRed(), accentPrimary.getGreen(),
                accentPrimary.getBlue(), (int)(255 * settingsPanelAnimation));
        RenderUtil.drawRoundedRectOutline(x, rightPanelY, settingsWidth, rightPanelHeight,
                cornerRadius, border.getRGB(), 2);

        // Enable proper text rendering
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        titleFont.drawCenteredStringWithShadow(selectedModule.getName().toUpperCase(),
                x + settingsWidth / 2f, rightPanelY + 15,
                new Color(textPrimary.getRed(), textPrimary.getGreen(),
                        textPrimary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

        smallFont.drawCenteredString("SETTINGS",
                x + settingsWidth / 2f, rightPanelY + 35,
                new Color(textTertiary.getRed(), textTertiary.getGreen(),
                        textTertiary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

        int yOffset = rightPanelY + 65;
        for (Setting setting : selectedModule.getSettings()) {
            if (yOffset > rightPanelY + rightPanelHeight - 40) break;

            boolean hovered = isHovered(mouseX, mouseY, x + padding, yOffset,
                    settingsWidth - padding * 2, itemHeight + 5);

            int settingX = x + padding;
            int settingWidth = settingsWidth - padding * 2;

            Color settingBg = new Color(bgTertiary.getRed(), bgTertiary.getGreen(),
                    bgTertiary.getBlue(), (int)(alpha * 0.8f));
            if (hovered) {
                settingBg = new Color(bgTertiary.getRed() + 15, bgTertiary.getGreen() + 15,
                        bgTertiary.getBlue() + 15, (int)(alpha * 0.8f));
            }
            RenderUtil.drawRoundedRect(settingX, yOffset, settingWidth, itemHeight + 5, cornerRadius,
                    settingBg.getRGB());

            // Re-enable textures for text
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            regularFont.drawStringWithShadow(setting.getName(),
                    settingX + 10, yOffset + 5,
                    new Color(textPrimary.getRed(), textPrimary.getGreen(),
                            textPrimary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

            String value = setting.getValueAsString();
            int valueWidth = regularFont.getStringWidth(value);
            int valueX = settingX + settingWidth - valueWidth - 15;

            RenderUtil.drawRoundedRect(valueX - 5, yOffset + 19, valueWidth + 10,
                    regularFont.getHeight() + 4, 4,
                    new Color(accentSecondary.getRed(), accentSecondary.getGreen(),
                            accentSecondary.getBlue(), (int)(60 * settingsPanelAnimation)).getRGB());

            // Re-enable textures for value text
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            regularFont.drawString(value, valueX, yOffset + 21,
                    new Color(accentPrimary.getRed(), accentPrimary.getGreen(),
                            accentPrimary.getBlue(), (int)(255 * settingsPanelAnimation)).getRGB());

            yOffset += itemHeight + 10;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
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