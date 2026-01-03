package dev.link.nebula.settings;

public class BooleanSetting extends Setting {

    private boolean value;

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description);
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void toggle() {
        this.value = !this.value;
    }

    @Override
    public void increment() {
        toggle();
    }

    @Override
    public void decrement() {
        toggle();
    }

    @Override
    public String getValueAsString() {
        return value ? "Enabled" : "Disabled";
    }
}