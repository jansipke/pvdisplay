package nl.jansipke.pvdisplay.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;

public class DateTimeUtils {

    public static class HourMinute {
        public final int hour;
        public final int minute;

        public HourMinute(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public String asString(boolean colon) {
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

        @NonNull
        public String toString() {
            return this.asString(true);
        }
    }

    public static class Year {
        public final int year;

        public Year(int year) {
            this.year = year;
        }

        public Year createCopy(int yearsToAdd, boolean allowFuture) {
            Year copy = new Year(year + yearsToAdd);
            if (!allowFuture) {
                Year today = Year.getToday();
                if (copy.isLaterThan(today)) {
                    return today;
                }
            }
            return copy;
        }

        public static Year getToday() {
            Calendar calendar = new GregorianCalendar();
            return new Year(calendar.get(Calendar.YEAR));
        }

        public boolean isLaterThan(Year other) {
            return year > other.year;
        }

        @NonNull
        public String toString() {
            return String.valueOf(year);
        }
    }

    public static class YearMonth {
        public final int year;
        public final int month;

        public YearMonth(int year, int month) {
            this.year = year;
            this.month = month;
        }

        public String asString(boolean dashes) {
            StringBuilder sb = new StringBuilder();
            if (year > 0) {
                sb.append(year);
            } else {
                sb.append("0000");
            }
            if (dashes) {
                sb.append("-");
            }
            if (month < 10) {
                sb.append("0");
            }
            sb.append(month);
            return sb.toString();
        }

        public YearMonth createCopy(int yearsToAdd, int monthsToAdd, boolean allowFuture) {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month - 1, 1);
            calendar.add(Calendar.YEAR, yearsToAdd);
            calendar.add(Calendar.MONTH, monthsToAdd);
            YearMonth copy = new YearMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
            if (!allowFuture) {
                YearMonth today = YearMonth.getToday();
                if (copy.isLaterThan(today)) {
                    return today;
                }
            }
            return copy;
        }

        public int getLastDayOfMonth() {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month - 1, 1);
            return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        public static YearMonth getToday() {
            Calendar calendar = new GregorianCalendar();
            return new YearMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        }

        public boolean isLaterThan(YearMonth other) {
            return (year > other.year) || (year == other.year && month > other.month);
        }

        @NonNull
        public String toString() {
            return this.asString(true);
        }
    }

    public static class YearMonthDay {
        public final int year;
        public final int month;
        public final int day;

        public YearMonthDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public String asString(boolean dashes) {
            StringBuilder sb = new StringBuilder();
            if (year > 0) {
                sb.append(year);
            } else {
                sb.append("0000");
            }
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

        public YearMonthDay createCopy(int yearsToAdd, int monthsToAdd, int daysToAdd, boolean allowFuture) {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month - 1, day);
            calendar.add(Calendar.YEAR, yearsToAdd);
            calendar.add(Calendar.MONTH, monthsToAdd);
            calendar.add(Calendar.DATE, daysToAdd);
            YearMonthDay copy = new YearMonthDay(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            if (!allowFuture) {
                YearMonthDay today = YearMonthDay.getToday();
                if (copy.isLaterThan(today)) {
                    return today;
                }
            }
            return copy;
        }

        public String getDayOfWeek() {
            String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month - 1, day);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDays[dayOfWeek - 1];
        }

        public static YearMonthDay getToday() {
            Calendar calendar = new GregorianCalendar();
            return new YearMonthDay(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
        }

        public boolean isLaterThan(YearMonthDay other) {
            return (year > other.year) || (year == other.year && month > other.month) ||
                    (year == other.year && month == other.month && day > other.day);
        }

        @NonNull
        public String toString() {
            return this.asString(true);
        }
    }

    public static String formatDateTime(int year, int month, int day, int hour, int minute) {
        YearMonthDay ymd = new YearMonthDay(year, month, day);
        HourMinute hm = new HourMinute(hour, minute);
        return ymd + " " + hm;
    }

    public static String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }
}
