package nl.jansipke.pvdisplay.data;

import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class DailyPvDatum {

    private final int year;
    private final int month;
    private final int day;
    private final double energyGenerated;
    private final double peakPower;
    private final String condition;

    public DailyPvDatum(int year, int month, int day,
                        double energyGenerated, double peakPower, String condition) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.energyGenerated = energyGenerated;
        this.peakPower = peakPower;
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
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

    public double getPeakPower() {
        return peakPower;
    }

    public int getYear() {
        return year;
    }

    public String toString() {
        return "DailyPvDatum[" + new DateTimeUtils.YearMonthDay(year, month, day) +
                ", energyGenerated=" + energyGenerated +
                ", peakPower=" + peakPower +
                ", condition=" + condition +
                "]";
    }
}
