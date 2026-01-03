package dev.link.nebula.settings;

public abstract class Setting {

    private final String name;
    private final String description;

    public Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract void increment();
    public abstract void decrement();
    public abstract String getValueAsString();
}