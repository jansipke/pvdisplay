package nl.jansipke.pvdisplay.data;

import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class HistoricalPvDatum {

    private final int year;
    private final int month;
    private final int day;
    private final double energyGenerated;

    public HistoricalPvDatum(int year, int month, int day, double energyGenerated) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.energyGenerated = energyGenerated;
    }

    public int getDay() {
        return day;
    }

    public double getEnergyGenerated() {
        return energyGenerated;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String toString() {
        return "HistoricalPvDatum[" + DateTimeUtils.formatDate(year, month, day) + ", energyGenerated=" + energyGenerated + "]";
    }
}
