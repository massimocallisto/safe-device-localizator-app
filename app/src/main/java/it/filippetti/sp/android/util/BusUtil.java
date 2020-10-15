package it.filippetti.sp.android.util;

public class BusUtil {
    public static String busAddress(String address, String bus) {
        return (bus != null && !bus.isEmpty()) ? (bus + ":" + address) : address;
    }

    public static String outputPath(String realm, String serial) {
        return "/" + realm + "/sp/device/message" + "/" + "android/" + serial;
    }

    public static String inputPath(String realm, String serial) {
        return "/" + realm + "/sp/device/setting" + "/" + "android/" + serial;
    }

    public static String endpoint(String address, String endpoint) {
        return address + ((endpoint != null) ? ":" + endpoint : "");
    }


    public static String[] HEX_SYMBOL = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static String toHexString(byte[] inarray) {
        int i, j, in;
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += HEX_SYMBOL[i];
            i = in & 0x0f;
            out += HEX_SYMBOL[i];
        }
        return out;
    }
}
