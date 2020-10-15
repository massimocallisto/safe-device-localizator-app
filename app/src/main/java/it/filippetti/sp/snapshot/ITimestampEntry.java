package it.filippetti.sp.snapshot;


import java.text.SimpleDateFormat;

public interface ITimestampEntry extends IEntry {
    public static final SimpleDateFormat ISO_8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // public static final SimpleDateFormat ISO_8601_FORMATTER = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sssZ");
    public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    //	Calendar getTimestamp();
    Long getTimestamp();


}
