package it.filippetti.sp.snapshot;

import java.net.URI;
import java.util.Calendar;

public class TimestampEntry implements ITimestampEntry {
    // private Long timestamp;
    private URI metadata;
    private String key;
    private Number value;
    private Calendar timestampWithTimeZone = Calendar.getInstance();
    private URI source;

    public Calendar getCalendar() {
        return timestampWithTimeZone;
    }

    @Override
    public String getKey() {
        return key;
    }

    public URI getMetadata() {
        return metadata;
    }

    @Override
    public URI getSource() {
        return source;
    }

    @Override
    public Long getTimestamp() {
        // return timestamp;
        return timestampWithTimeZone.getTimeInMillis();
    }

    @Override
    public Number getValue() {
        return value;
    }

    public void setCalendar(Calendar calendar) {
        this.setTimestamp(calendar);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMetadata(URI metadata) {
        this.metadata = metadata;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public void setTimestamp(Calendar calendar) {
        timestampWithTimeZone = calendar;
    }

    public void setTimestamp(Long timestamp) {
        // this.timestamp = timestamp;
        timestampWithTimeZone.setTimeInMillis(timestamp);
    }


    @Override
    public Number setValue(Number value) {
        this.value = value;
        return value;
    }
}
