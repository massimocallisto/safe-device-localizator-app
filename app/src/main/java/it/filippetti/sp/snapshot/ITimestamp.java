package it.filippetti.sp.snapshot;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public interface ITimestamp extends Serializable {
    /**
     * Complete date plus hours, minutes, seconds and a decimal fraction of a second <br/>
     * YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
     */
    public static final SimpleDateFormat ISO_8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");
    // public static final SimpleDateFormat ISO_8601_FORMATTER = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sssZ");
    public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // Calendar getTimestamp();
    Long getTimestamp();


}
