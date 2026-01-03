package dev.link.nebula.gui;

import dev.link.nebula.module.Module;
import dev.link.nebula.module.ModuleManager;
import dev.link.nebula.settings.BooleanSetting;
import dev.link.nebula.settings.ModeSetting;
import dev.link.nebula.settings.NumberSetting;
import dev.link.nebula.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {

    private Module.Category selectedCategory = Module.Category.COMBAT;
    private Module selectedModule = null;
    private int scrollOffset = 0;

    // Colors
    private final int backgroundColor = new Color(20, 20, 20, 200).getRGB();
    private final int outlineColor = new Color(80, 40, 120, 255).getRGB();
    private final int hoverColor = new Color(40, 40, 40, 200).getRGB();
    private final int enabledColor = new Color(100, 50, 150, 200).getRGB();
    private final int textColor = new Color(255, 255, 255, 255).getRGB();

    // Layout
    private final int categoryWidth = 120;
    private final int moduleWidth = 200;
    private final int settingsWidth = 250;
    private final int itemHeight = 30;
    private final int padding = 10;
    private final int cornerRadius = 8;

    private int leftPanelX;
    private int leftPanelY;
    private int leftPanelHeight;

    private int middlePanelX;
    private int middlePanelY;
    private int middlePanelHeight;

    private int rightPanelX;
    private int rightPanelY;
    private int rightPanelHeight;

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        int totalWidth = categoryWidth + moduleWidth + (selectedModule != null ? settingsWidth : 0) + padding * 4;
        int totalHeight = 400;

        leftPanelX = centerX - totalWidth / 2;
        leftPanelY = centerY - totalHeight / 2;
        leftPanelHeight = totalHeight;

        middlePanelX = leftPanelX + categoryWidth + padding;
        middlePanelY = leftPanelY;
        middlePanelHeight = totalHeight;

        rightPanelX = middlePanelX + moduleWidth + padding;
        rightPanelY = leftPanelY;
        rightPanelHeight = totalHeight;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // Draw category panel
        drawRoundedRect(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight, cornerRadius, backgroundColor);
        drawRoundedRectOutline(leftPanelX, leftPanelY, categoryWidth, leftPanelHeight, cornerRadius, outlineColor, 2);

        // Draw categories
        int yOffset = leftPanelY + padding;
        for (Module.Category category : Module.Category.values()) {
            boolean hovered = isHovered(mouseX, mouseY, leftPanelX + 5, yOffset, categoryWidth - 10, itemHeight);
            boolean selected = category == selectedCategory;

            int bgColor = selected ? enabledColor : (hovered ? hoverColor : backgroundColor);
            drawRoundedRect(leftPanelX + 5, yOffset, categoryWidth - 10, itemHeight, cornerRadius, bgColor);

            drawCenteredString(fontRendererObj, category.getName(),
                    leftPanelX + categoryWidth / 2, yOffset + (itemHeight - 8) / 2, textColor);

            yOffset += itemHeight + 5;
        }

        // Draw module panel
        drawRoundedRect(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight, cornerRadius, backgroundColor);
        drawRoundedRectOutline(middlePanelX, middlePanelY, moduleWidth, middlePanelHeight, cornerRadius, outlineColor, 2);

        // Draw modules for selected category
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        yOffset = middlePanelY + padding - scrollOffset;

        for (Module module : modules) {
            if (yOffset > middlePanelY + middlePanelHeight) break;
            if (yOffset + itemHeight < middlePanelY) {
                yOffset += itemHeight + 5;
                continue;
            }

            boolean hovered = isHovered(mouseX, mouseY, middlePanelX + 5, yOffset, moduleWidth - 10, itemHeight);
            boolean selected = module == selectedModule;

            int bgColor = module.isEnabled() ? enabledColor : (selected ? hoverColor : (hovered ? hoverColor : backgroundColor));
            drawRoundedRect(middlePanelX + 5, yOffset, moduleWidth - 10, itemHeight, cornerRadius, bgColor);

            drawString(fontRendererObj, module.getName(),
                    middlePanelX + 10, yOffset + (itemHeight - 8) / 2, textColor);

            // Draw enabled indicator
            if (module.isEnabled()) {
                drawString(fontRendererObj, "[ON]",
                        middlePanelX + moduleWidth - 35, yOffset + (itemHeight - 8) / 2,
                        new Color(100, 255, 100).getRGB());
            }

            yOffset += itemHeight + 5;
        }

        // Draw settings panel if module is selected
        if (selectedModule != null && !selectedModule.getSettings().isEmpty()) {
            drawRoundedRect(rightPanelX, rightPanelY, settingsWidth, rightPanelHeight, cornerRadius, backgroundColor);
            drawRoundedRectOutline(rightPanelX, rightPanelY, settingsWidth, rightPanelHeight, cornerRadius, outlineColor, 2);

            // Draw module name header
            drawCenteredString(fontRendererObj, selectedModule.getName() + " Settings",
                    rightPanelX + settingsWidth / 2, rightPanelY + 15, textColor);

            // Draw settings
            yOffset = rightPanelY + 40;
            for (Setting setting : selectedModule.getSettings()) {
                if (yOffset > rightPanelY + rightPanelHeight - 50) break;

                boolean hovered = isHovered(mouseX, mouseY, rightPanelX + 5, yOffset, settingsWidth - 10, itemHeight);
                drawRoundedRect(rightPanelX + 5, yOffset, settingsWidth - 10, itemHeight, cornerRadius,
                        hovered ? hoverColor : backgroundColor);

                drawString(fontRendererObj, setting.getName(),
                        rightPanelX + 10, yOffset + 5, textColor);

                drawString(fontRendererObj, setting.getValueAsString(),
                        rightPanelX + 10, yOffset + 17, new Color(150, 150, 150).getRGB());

                yOffset += itemHeight + 5;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Category selection
        int yOffset = leftPanelY + padding;
        for (Module.Category category : Module.Category.values()) {
            if (isHovered(mouseX, mouseY, leftPanelX + 5, yOffset, categoryWidth - 10, itemHeight)) {
                selectedCategory = category;
                scrollOffset = 0;
                return;
            }
            yOffset += itemHeight + 5;
        }

        // Module selection and toggle
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        yOffset = middlePanelY + padding - scrollOffset;

        for (Module module : modules) {
            if (yOffset > middlePanelY + middlePanelHeight) break;
            if (yOffset + itemHeight < middlePanelY) {
                yOffset += itemHeight + 5;
                continue;
            }

            if (isHovered(mouseX, mouseY, middlePanelX + 5, yOffset, moduleWidth - 10, itemHeight)) {
                if (mouseButton == 0) {
                    module.toggle();
                } else if (mouseButton == 1) {
                    selectedModule = (selectedModule == module) ? null : module;
                }
                return;
            }
            yOffset += itemHeight + 5;
        }

        // Settings interaction
        if (selectedModule != null && !selectedModule.getSettings().isEmpty()) {
            yOffset = rightPanelY + 40;
            for (Setting setting : selectedModule.getSettings()) {
                if (yOffset > rightPanelY + rightPanelHeight - 50) break;

                if (isHovered(mouseX, mouseY, rightPanelX + 5, yOffset, settingsWidth - 10, itemHeight)) {
                    if (mouseButton == 0) {
                        setting.increment();
                    } else if (mouseButton == 1) {
                        setting.decrement();
                    }
                    return;
                }
                yOffset += itemHeight + 5;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset -= wheel > 0 ? 30 : -30;
            if (scrollOffset < 0) scrollOffset = 0;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
        drawRect(x + radius, y, x + width - radius, y + height, color);
        drawRect(x, y + radius, x + width, y + height - radius, color);

        drawCircle(x + radius, y + radius, radius, color);
        drawCircle(x + width - radius, y + radius, radius, color);
        drawCircle(x + radius, y + height - radius, radius, color);
        drawCircle(x + width - radius, y + height - radius, radius, color);
    }

    private void drawRoundedRectOutline(int x, int y, int width, int height, int radius, int color, int lineWidth) {
        for (int i = 0; i < lineWidth; i++) {
            drawHorizontalLine(x + radius, x + width - radius, y + i, color);
            drawHorizontalLine(x + radius, x + width - radius, y + height - i, color);
            drawVerticalLine(x + i, y + radius, y + height - radius, color);
            drawVerticalLine(x + width - i, y + radius, y + height - radius, color);
        }
    }

    private void drawCircle(int x, int y, int radius, int color) {
        for (int i = 0; i < radius * 2; i++) {
            for (int j = 0; j < radius * 2; j++) {
                double distance = Math.sqrt(Math.pow(i - radius, 2) + Math.pow(j - radius, 2));
                if (distance <= radius) {
                    drawRect(x + i - radius, y + j - radius, x + i - radius + 1, y + j - radius + 1, color);
                }
            }
        }
    }
}