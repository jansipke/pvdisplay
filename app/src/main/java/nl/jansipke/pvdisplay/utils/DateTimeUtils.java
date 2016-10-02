package nl.jansipke.pvdisplay.utils;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {

    public static String formatDate(int year, int month, int day, boolean dashes) {
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        if (dashes) {
            sb.append("-");
        }
        if (month < 10) {
            sb.append("0");
        }
        sb.append(month);
        if (dashes) {
            sb.append("-");
        }
        if (day < 10) {
            sb.append("0");
        }
        sb.append(day);
        return sb.toString();
    }

    public static String formatDateTime(int year, int month, int day, int hour, int minute) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatDate(year, month, day, true));
        sb.append(" ");
        sb.append(formatTime(hour, minute));
        return sb.toString();
    }

    public static String formatTime(int hour, int minute) {
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(":");
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        return sb.toString();
    }

    public static Date getDate(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, hour, minute);
        return calendar.getTime();
    }
}
