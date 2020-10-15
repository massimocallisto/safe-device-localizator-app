package it.filippetti.sp.android.util;

public interface IJZConstants {
    public static final long SEND_TIMEOUT = 3000;

    /**/
    public static final String ADDRESS_ENDPOINT_DISCOVER = "jz.driver.discover";

    public static final String ADDRESS_JZ_DRIVER_MASTER_MESSAGE = "jz.driver.master.message";
    public static final String ADDRESS_JZ_DRIVER_MASTER_COMMAND = "jz.driver.master.command";

    /**/
    public static final String ADDRESS_JZ_DRIVER_MANAGE_COMMAND = "jz.driver.manage.command";

    public final static String TOPIC_SEPARATOR = "/";
    public final static String TOPIC_DATA_FRAME_INPUT = "/jz/data/frame/input";
    // public final static String TOPIC_DATA_FRAME_MESSAGE = "/jz/data/frame/message";
    public final static String TOPIC_DATA_FRAME_COMMAND = "/jz/data/frame/command";

    /**/
//    public final static String TOPIC_JZ_DATA_FRAME_MANAGE_MESSAGE = "/jz/data/frame/message";
    public final static String TOPIC_JZ_DATA_FRAME_MANAGE_COMMAND = "/jz/data/frame/command";

    /* master */
    public final static String TOPIC_JZ_DATA_FRAME_MASTER_MESSAGE = "/jz/master/frame/message";
    public final static String TOPIC_JZ_DATA_FRAME_MASTER_COMMAND = "/jz/master/frame/command";

}
