package dev.link.nebula.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {

    private final List<String> modes;
    private int currentIndex;

    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description);
        this.modes = Arrays.asList(modes);
        this.currentIndex = this.modes.indexOf(defaultMode);
        if (this.currentIndex == -1) {
            this.currentIndex = 0;
        }
    }

    public String getMode() {
        return modes.get(currentIndex);
    }

    public void setMode(String mode) {
        int index = modes.indexOf(mode);
        if (index != -1) {
            this.currentIndex = index;
        }
    }

    public List<String> getModes() {
        return modes;
    }

    @Override
    public void increment() {
        currentIndex = (currentIndex + 1) % modes.size();
    }

    @Override
    public void decrement() {
        currentIndex = (currentIndex - 1 + modes.size()) % modes.size();
    }

    @Override
    public String getValueAsString() {
        return getMode();
    }
}