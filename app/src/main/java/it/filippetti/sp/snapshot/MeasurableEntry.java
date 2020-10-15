package it.filippetti.sp.snapshot;

public class MeasurableEntry extends TimestampEntry implements IMeasurable {
    private String unit;

    @Override
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
