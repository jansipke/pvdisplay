package nl.jansipke.pvdisplay.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeUtils {

    public static class YearMonthDay {
        public int year;
        public int month;
        public int day;
    }

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
        return formatDate(year, month, day, true) + " " + formatTime(hour, minute);
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

    public static YearMonthDay getYearMonthDay(int daysAgo) {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -daysAgo);
        YearMonthDay yearMonthDay = new YearMonthDay();
        yearMonthDay.year = calendar.get(Calendar.YEAR);
        yearMonthDay.month = calendar.get(Calendar.MONTH) + 1;
        yearMonthDay.day = calendar.get(Calendar.DAY_OF_MONTH);
        return yearMonthDay;
    }
}
