package dev.link.nebula.settings;

public class NumberSetting extends Setting {

    private double value;
    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (value < min) {
            this.value = min;
        } else if (value > max) {
            this.value = max;
        } else {
            this.value = value;
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    @Override
    public void increment() {
        setValue(value + increment);
    }

    @Override
    public void decrement() {
        setValue(value - increment);
    }

    @Override
    public String getValueAsString() {
        return String.format("%.2f", value);
    }
}