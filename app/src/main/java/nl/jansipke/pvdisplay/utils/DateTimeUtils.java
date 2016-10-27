package nl.jansipke.pvdisplay.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeUtils {

    public static class YearMonth {
        public int year;
        public int month;
    }

    public static class YearMonthDay {
        public int year;
        public int month;
        public int day;
    }

    public static YearMonthDay addDays(YearMonthDay picked, int daysToAdd) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(picked.year, picked.month - 1, picked.day);
        calendar.add(Calendar.DATE, daysToAdd);
        YearMonthDay yearMonthDay = new YearMonthDay();
        yearMonthDay.year = calendar.get(Calendar.YEAR);
        yearMonthDay.month = calendar.get(Calendar.MONTH) + 1;
        yearMonthDay.day = calendar.get(Calendar.DAY_OF_MONTH);
        return yearMonthDay;
    }

    public static YearMonth addMonths(YearMonth picked, int monthsToAdd) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(picked.year, picked.month - 1, 1);
        calendar.add(Calendar.MONTH, monthsToAdd);
        YearMonth yearMonth = new YearMonth();
        yearMonth.year = calendar.get(Calendar.YEAR);
        yearMonth.month = calendar.get(Calendar.MONTH) + 1;
        return yearMonth;
    }

    public static String formatMonthDay(int month, int day, boolean dashes) {
        StringBuilder sb = new StringBuilder();
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

    public static String formatYearMonth(int year, int month, boolean dashes) {
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        if (dashes) {
            sb.append("-");
        }
        if (month < 10) {
            sb.append("0");
        }
        sb.append(month);
        return sb.toString();
    }

    public static String formatYearMonthDay(int year, int month, int day, boolean dashes) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatYearMonth(year, month, dashes));
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
        return formatYearMonthDay(year, month, day, true) + " " + formatTime(hour, minute, true);
    }

    public static String formatTime(int hour, int minute, boolean colon) {
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        if (colon) {
            sb.append(":");
        }
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        return sb.toString();
    }

    public static int getLastDayOfMonth(int year, int month) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getTodaysYear() {
        return getTodaysYearMonthDay().year;
    }

    public static YearMonth getTodaysYearMonth() {
        YearMonthDay yearMonthDay = getTodaysYearMonthDay();
        YearMonth yearMonth = new YearMonth();
        yearMonth.year = yearMonthDay.year;
        yearMonth.month = yearMonthDay.month;
        return yearMonth;
    }

    public static YearMonthDay getTodaysYearMonthDay() {
        Calendar calendar = new GregorianCalendar();
        YearMonthDay yearMonthDay = new YearMonthDay();
        yearMonthDay.year = calendar.get(Calendar.YEAR);
        yearMonthDay.month = calendar.get(Calendar.MONTH) + 1;
        yearMonthDay.day = calendar.get(Calendar.DAY_OF_MONTH);
        return yearMonthDay;
    }

    public static String getDayOfWeek(int year, int month, int day) {
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDays[dayOfWeek - 1];
    }
}
