package it.filippetti.sp.snapshot;

import java.net.URI;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;


public class Channel<V, Q extends Quantity> extends MeasurableEntry implements IChannel, Cloneable, IFlowState {
    private static final String ID_SEPARATOR = "/";
    // private Calendar timestamp;
    // private Long timestamp;
    // private String label;
    // private Number value;
    // private String unit;
    private String extension;
    /**/
    private URI target;
    private Double probability;
    private Double variance;
    private Double standardDeviation;
    private URI reference;
    private Map<String, Object> meta = new LinkedHashMap<>();


    public Channel() {
    }

    public Channel(Calendar timestamp, String key, Measure<V, Quantity> measure) {
        this(key);
        this.setMeasure(measure);
        this.setTimestamp(timestamp);
    }

    public Channel(Calendar timestamp, String key, Number value, String unit) {
        this(key);
        this.setTimestamp(timestamp);
        this.setValue(value);
        this.setUnit(unit);
    }

    public Channel(String label) {
        this.setKey(label);
    }

    @Deprecated
    public Channel(String name, Measure<V, Quantity> measure, Calendar timestamp) {
        // this(name, measure, timestamp.getTimeInMillis());
        this.setKey(name);
        this.setMeasure(measure);
        this.setCalendar(timestamp);
    }

    // @Deprecated
    // public Measurement(String name, Measure<V, Quantity> measure, Long timestamp) {
    // this.setKey(name);
    // this.setMeasure(measure);
    // this.setTimestamp(timestamp);
    // }

    @Deprecated
    public Channel(String name, Number value, String unit) {
        this.setKey(name);
        this.setValue(value);
        this.setUnit(unit);
    }

    // @Deprecated
    // public Measurement(String name, Measure<V, Quantity> measure) {
    // this(name, measure, new GregorianCalendar());
    // }

    @Deprecated
    public Channel(String name, Number value, String unit, Calendar timestamp) {
        this(name, value, unit);
        this.putCalendar(timestamp);

    }

    @Deprecated
    public Channel(String name, Number value, String unit, Long timestamp) {
        this.setKey(name);
        this.setValue(value);
        this.setUnit(unit);
        this.setTimestamp(timestamp);
    }

    public Object clone() {
        Channel s = new Channel(this.getKey());
        s.putTimestamp(this.getTimestamp()).putValue(this.getValue()).putUnit(this.getUnit());
        s.putExtension(this.getExtension());
        return s;
    }

    // public Calendar getCalendar() {
    // GregorianCalendar c = new GregorianCalendar();
    // c.setTimeInMillis(getTimestamp());
    // return c;
    // }

    public String getExtension() {
        return extension;
    }

    // public String getKey() {
    // return label;
    // }

    public String getID() {
        String id = (getSource() != null ? getSource().toString() : "");
        id += ID_SEPARATOR + (getTarget() != null ? getTarget().toString() : "");
        id += ID_SEPARATOR + (getLabel() != null ? getLabel().toString() : "");
        return id;
    }

    @Override
    public String getLabel() {
        return this.getKey();
    }

    // public Calendar getTimestamp() {
    // return timestamp;
    // }

    public Measure<V, Quantity> getMeasure() {
        return (Measure<V, Quantity>) Measure.valueOf(getValue().doubleValue(), Unit.valueOf(getUnit() != null ? getUnit() : ""));
    }

    // public Long getTimestamp() {
    // return timestamp;
    // }

    // public String getUnit() {
    // return this.unit;
    // }

    // public Number getValue() {
    // return this.value != null ? this.value : null;
    // }

    public Map<String, Object> getMeta() {
        return meta;
    }

    @Deprecated
    public String getName() {
        return getLabel();
    }

    public Double getProbability() {
        return probability;
    }

    public URI getReference() {
        return reference;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public URI getTarget() {
        return target;
    }

    public Double getVariance() {
        return variance;
    }

    public Channel<V, Q> putCalendar(Calendar timestamp) {
        this.setTimestamp(timestamp);
        return this;
    }

    public Channel<V, Q> putExtension(String extension) {
        this.setExtension(extension);
        return this;
    }

    public Channel<V, Q> putKey(String key) {
        this.setKey(key);
        return this;
    }

    public Channel<V, Q> putLabel(String label) {
        this.setLabel(label);
        return this;
    }

    public Channel<V, Q> putMeasure(Double value, String unit) {
        this.setMeasure(value, unit);
        return this;
    }

    public Channel<V, Q> putMeasure(Measure<V, Quantity> measure) {
        this.setMeasure(measure);
        return this;
    }

    public Channel<V, Q> putMeasure(Number value, String unit) {
        this.setMeasure(value, unit);
        return this;
    }

    public Channel<V, Q> putMetadata(URI ref) {
        this.setMetadata(ref);
        return this;
    }

    @Deprecated
    public Channel<V, Q> putName(String name) {
        this.setName(name);
        return this;
    }

    public Channel<V, Q> putProbability(Double probability) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Probability must be in [0,1] range");
        }
        this.setProbability(probability);
        return this;
    }

    public Channel<V, Q> putSource(URI source) {
        this.setSource(source);
        return this;
    }

    public Channel putTarget(URI target) {
        this.setTarget(target);
        return this;
    }

    @Deprecated
    public Channel<V, Q> putTimestamp(Calendar timestamp) {
        this.setTimestamp(timestamp);
        return this;
    }

    public Channel<V, Q> putTimestamp(Long timestamp) {
        this.setTimestamp(timestamp);
        return this;
    }



    public Channel<V, Q> putUnit(String unit) {
        this.setUnit(unit);
        return this;
    }

    public Channel<V, Q> putValue(Double value) {
        this.setValue(value);
        return this;
    }

    public Channel<V, Q> putValue(Number value) {
        this.setValue(value);
        return this;
    }

    // @Deprecated
    // public void setKey(String key) {
    // this.label = key;
    // }

    public Channel<V, Q> putVariance(Double variance) {
        this.setVariance(variance);
        return this;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setLabel(String label) {
        // this.label = label;
        setKey(label);
    }

    public void setMeasure(Double value, String unit) {
        // this.measure = (Measure<V, Quantity>) Measure.valueOf(value,
        // Unit.valueOf(unit));
        this.setValue(value);
        this.setUnit(unit);
    }

    // public void setTimestamp(Calendar timestamp) {
    // this.timestamp = timestamp;
    // }

    // public void setTimestamp(Long timestamp) {
    // this.timestamp = timestamp;
    // }

    // public void setUnit(String unit) {
    // this.unit = unit;
    // // this.unit = Unit.valueOf(unit).toString();
    // }

    public void setMeasure(Measure<V, Quantity> measure) {
        // this.measure = measure;
        // this.value = measure.doubleValue(measure.getUnit());
        // this.unit = measure.getUnit().toString();
        this.setValue(measure.doubleValue(measure.getUnit()));
        this.setUnit(measure.getUnit().toString());
    }

    // public Number setValue(Number value) {
    // this.value = value;
    // return value;
    // }

    public void setMeasure(Number value, String unit) {
        // this.measure = (Measure<V, Quantity>) Measure.valueOf(value,
        // Unit.valueOf(unit));
        this.setValue(value);
        this.setUnit(unit);
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Deprecated
    public void setName(String name) {
        // this.label = name;
        setKey(name);
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public void setReference(URI reference) {
        this.reference = reference;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }


    public void setTarget(URI target) {
        this.target = target;
    }

    public Number setValue(Double value) {
        // this.value = value;
        return super.setValue(value);
    }

    public void setVariance(Double variance) {
        this.variance = variance;
    }

//    @Override
//    public String toString() {
//        StringBuilder b = new StringBuilder();
//        // b.append("@{").append(this.getTimestamp() != null ? TIMESTAMP_FORMATTER.format(this.getTimestamp().getTime()) : "").append("}");
////		b.append("@{").append(this.getTimestamp() != null ? TIMESTAMP_FORMATTER.format(this.getCalendar().getTime()) : "").append("}");
//        b.append("@{").append(this.getTimestampWithTimeZone() != null ? this.getTimestampWithTimeZone() : "").append("}");
//        b.append(" ");
//        b.append(this.getSource() != null ? "'" + this.getSource() + "'." : "");
//        b.append("'").append(this.getLabel()).append("'");
//        b.append(" = ");
//        b.append(this.getValue() != null ? this.getValue() : "");
//        b.append(" ");
//        b.append("[").append(this.getUnit() != null ? this.getUnit() : "").append("]");
//        return b.toString();
//    }

}
