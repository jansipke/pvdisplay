package nl.jansipke.pvdisplay.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeUtils {

    public static class Year {
        public int year;
        public Year(int year) {
            this.year = year;
        }
        boolean isLaterThan(Year other) {
            return year > other.year;
        }
    }

    public static class YearMonth {
        public int year;
        public int month;
        public YearMonth(int year, int month) {
            this.year = year;
            this.month = month;
        }
        boolean isLaterThan(YearMonth other) {
            return (year > other.year) || (year == other.year && month > other.month);
        }
    }

    public static class YearMonthDay {
        public int year;
        public int month;
        public int day;
        public YearMonthDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
        public boolean isLaterThan(YearMonthDay other) {
            return (year > other.year) || (year == other.year && month > other.month) ||
                    (year == other.year && month == other.month && day > other.day);
        }
    }

    public static YearMonthDay add(YearMonthDay picked, int yearsToAdd, int monthsToAdd, int daysToAdd, boolean allowFuture) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(picked.year, picked.month - 1, picked.day);
        calendar.add(Calendar.YEAR, yearsToAdd);
        calendar.add(Calendar.MONTH, monthsToAdd);
        calendar.add(Calendar.DATE, daysToAdd);
        YearMonthDay yearMonthDay = new YearMonthDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        if (!allowFuture) {
            YearMonthDay todaysYearMonthDay = getTodaysYearMonthDay();
            if (yearMonthDay.isLaterThan(todaysYearMonthDay)) {
                return todaysYearMonthDay;
            }
        }
        return yearMonthDay;
    }

    public static YearMonth addMonths(YearMonth picked, int monthsToAdd, boolean allowFuture) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(picked.year, picked.month - 1, 1);
        calendar.add(Calendar.MONTH, monthsToAdd);
        YearMonth yearMonth = new YearMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        if (!allowFuture) {
            YearMonth todaysYearMonth = getTodaysYearMonth();
            if (yearMonth.isLaterThan(todaysYearMonth)) {
                return todaysYearMonth;
            }
        }
        return yearMonth;
    }

    public static Year addYears(Year picked, int yearsToAdd, boolean allowFuture) {
        Year year = new Year(picked.year + yearsToAdd);
        if (!allowFuture) {
            Year todaysYear = getTodaysYear();
            if (year.isLaterThan(todaysYear)) {
                return todaysYear;
            }
        }
        return year;
    }

    public static String formatYear(int year) {
        return "" + year;
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

    public static Year getTodaysYear() {
        YearMonthDay yearMonthDay = getTodaysYearMonthDay();
        return new Year(yearMonthDay.year);
    }

    public static YearMonth getTodaysYearMonth() {
        YearMonthDay yearMonthDay = getTodaysYearMonthDay();
        return new YearMonth(yearMonthDay.year, yearMonthDay.month);
    }

    public static YearMonthDay getTodaysYearMonthDay() {
        Calendar calendar = new GregorianCalendar();
        return new YearMonthDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String getDayOfWeek(int year, int month, int day) {
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDays[dayOfWeek - 1];
    }

    public static String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }
}
