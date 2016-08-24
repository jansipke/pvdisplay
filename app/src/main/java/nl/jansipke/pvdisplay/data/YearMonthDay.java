package nl.jansipke.pvdisplay.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class YearMonthDay {

    private final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.US);

    private final int year;
    private final int month;
    private final int day;

    private final long firstTimestamp;
    private final long lastTimestamp;

    public YearMonthDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        firstTimestamp = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.DATE, 1);
        lastTimestamp = calendar.getTimeInMillis() / 1000 - 1;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    @Override
    public String toString() {
        return "YearMonthDay[year=" + year + " , month=" + month + ", day=" + day + "]";
    }
}
