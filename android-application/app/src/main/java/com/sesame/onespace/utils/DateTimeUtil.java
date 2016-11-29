package com.sesame.onespace.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chongos on 9/14/15 AD.
 */
public class DateTimeUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    /**
     * Current Timestamp
     * @return
     */
    public static String getCurrentTimeStamp() {
        return (System.currentTimeMillis() / 1000L) + "";
    }

    /**
     * Convert timestamp to String time (HH:mm) Ex: 12:45, 23:03
     * @param timestamp
     * @return
     */
    public static String getTimeByTimestamp(String timestamp) {
        return TIME_FORMAT.format(convertTimestampToDate(timestamp));
    }

    /**
     * Convert timestamp to String date (MMM dd) Ex: Sep 12, Jan 01
     * @param timestamp
     * @return
     */
    public static String getDateByTimestamp(String timestamp) {
        return DATE_FORMAT.format(convertTimestampToDate(timestamp));
    }


    /**
     * Convert timestamp to String with condition
     * if it is a today will return only time (HH:mm),
     * or it is a yesterday will return "Yesterday"
     * or it is a day in last week will return name of day
     * else return date (MMM dd)
     * @param timestamp
     * @return
     */
    public static String getShortDateTimeByTimeStamp(String timestamp) {
        return getDateTime(convertTimestampToDate(timestamp),
                convertTimestampToDate(getCurrentTimeStamp()));
    }

    public static boolean isSameDate(String timestamp1, String timestamp2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(convertTimestampToDate(timestamp1)).equals(fmt.format(convertTimestampToDate(timestamp2)));
    }


    private static String getDateTime(Date startDate, Date endDate) {
        long startTime = startDate.getTime() / 1000;
        long endTime = endDate.getTime() / 1000;
        long midnightOfEndDate = endTime - ((endTime) % (24 * 60 * 60));
        long diff = midnightOfEndDate - startTime;
        double diffDays = Math.ceil(diff / (24.0 * 60 * 60));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);

        if(diffDays > 7)
            return DATE_FORMAT.format(startDate);
        else if(diffDays > 1)
            return new SimpleDateFormat("E", Locale.getDefault()).format(startDate);
        else if(diffDays == 1)
            return "Yesterday";
        return TIME_FORMAT.format(startDate);

    }

    private static Date convertTimestampToDate(String timestamp) {
        return new Date(Long.parseLong(timestamp) * 1000L);
    }


}
