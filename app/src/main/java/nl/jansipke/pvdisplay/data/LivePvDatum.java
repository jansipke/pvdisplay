package nl.jansipke.pvdisplay.data;

import androidx.annotation.NonNull;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class LivePvDatum {

    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final double energyGeneration; // In Watt (W)
    private final double powerGeneration;  // In Watt Hour (Wh)

    public LivePvDatum(int year, int month, int day, int hour, int minute, double energyGeneration, double powerGeneration) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.energyGeneration = energyGeneration;
        this.powerGeneration = powerGeneration;
    }

    public int getDay() {
        return day;
    }

    public double getEnergyGeneration() {
        return energyGeneration;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getMonth() {
        return month;
    }

    public double getPowerGeneration() {
        return powerGeneration;
    }

    public int getYear() {
        return year;
    }

    @NonNull
    public String toString() {
        return "LivePvDatum[" + DateTimeUtils.formatDateTime(year, month, day, hour, minute) +
                " powerGeneration=" + powerGeneration +
                ", energyGeneration=" + energyGeneration +
                "]";
    }
}
