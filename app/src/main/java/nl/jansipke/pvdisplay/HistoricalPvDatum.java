package nl.jansipke.pvdisplay;

public class HistoricalPvDatum {

    private final String date;
    private final double energyGenerated;

    public HistoricalPvDatum(String date, double energyGenerated) {
        this.date = date;
        this.energyGenerated = energyGenerated;
    }

    public String getDate() {
        return date;
    }

    public double getEnergyGenerated() {
        return energyGenerated;
    }

    public String toString() {
        return date + " " + energyGenerated + "Wh";
    }
}
