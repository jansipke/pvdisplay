package nl.jansipke.pvdisplay.data;

public class HistoricalPvDatum {

    private final YearMonthDay yearMonthDay;
    private final double energyGenerated;

    public HistoricalPvDatum(YearMonthDay yearMonthDay, double energyGenerated) {
        this.yearMonthDay = yearMonthDay;
        this.energyGenerated = energyGenerated;
    }

    public YearMonthDay getYearMonthDay() {
        return yearMonthDay;
    }

    public double getEnergyGenerated() {
        return energyGenerated;
    }

    public String toString() {
        return "HistoricalPvDatum[yearMonthDay=" + yearMonthDay + ", energyGenerated=" + energyGenerated + "]";
    }
}
