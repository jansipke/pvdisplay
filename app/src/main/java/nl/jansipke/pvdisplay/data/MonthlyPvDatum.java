package nl.jansipke.pvdisplay.data;

import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class MonthlyPvDatum {

    private final int year;
    private final int month;
    private final double energyGenerated;

    public MonthlyPvDatum(int year, int month, double energyGenerated) {
        this.year = year;
        this.month = month;
        this.energyGenerated = energyGenerated;
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
        return "MonthlyPvDatum[" + DateTimeUtils.formatYearMonth(year, month, true) +
                ", energyGenerated=" + energyGenerated +
                "]";
    }
}
