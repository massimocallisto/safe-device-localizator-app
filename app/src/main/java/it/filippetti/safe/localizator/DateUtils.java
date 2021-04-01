package it.filippetti.safe.localizator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public final static SimpleDateFormat stdFormatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String formatDate(String localDate) throws ParseException {

        SimpleDateFormat fromPreviousFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date lDate = fromPreviousFormat.parse(localDate);
        SimpleDateFormat toCurrentFormat = new SimpleDateFormat("dd/MM/yyyy");
        return toCurrentFormat.format(lDate);
    }

    public static String formatDateTime(String localDate) throws ParseException {

        SimpleDateFormat fromPreviousFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date lDate = fromPreviousFormat.parse(localDate);
        SimpleDateFormat toCurrentFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return toCurrentFormat.format(lDate);
    }

    public static String formatGwDateTime(String localDate) throws ParseException {

        SimpleDateFormat fromPreviousFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date lDate = fromPreviousFormat.parse(localDate);
        SimpleDateFormat toCurrentFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return toCurrentFormat.format(lDate);
    }

    public static String dateString(Date aDate) {
        return aDate != null ?
                stdFormatDate.format(aDate) : "";

    }
}
