package it.filippetti.sp.snapshot;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.UUID;


public class Event {
    private UUID uuid = UUID.randomUUID();
    private UUID cuid;

    private LinkedHashSet<UUID> ruid = new LinkedHashSet<>();

    private Calendar timestampWithTimeZone = Calendar.getInstance();

    public void addElementRuid(UUID uuid) {

        this.ruid.add(uuid);
    }

    public Calendar getCalendar() {
        return timestampWithTimeZone;
    }

    public UUID getCuid() {
        return cuid;
    }

    public LinkedHashSet<UUID> getRuid() {
        return ruid;
    }

    public Long getTimestamp() {
        // return timestamp;
        return timestampWithTimeZone.getTimeInMillis();
    }


    public UUID getUuid() {
        return uuid;
    }

    public void setCalendar(Calendar calendar) {
        this.setTimestamp(calendar);
    }

    public void setCuid(UUID cuid) {
        this.cuid = cuid;
    }

    public void setRuid(LinkedHashSet<UUID> ruid) {
        this.ruid = ruid;
    }

    public void setTimestamp(Calendar calendar) {

        this.timestampWithTimeZone = calendar;
    }

    public void setTimestamp(Long timestamp) {
        // this.timestamp = timestamp;
        timestampWithTimeZone.setTimeInMillis(timestamp);
    }



    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
