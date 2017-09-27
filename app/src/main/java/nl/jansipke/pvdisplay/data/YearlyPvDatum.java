package nl.jansipke.pvdisplay.data;

public class YearlyPvDatum {

    private final int year;
    private final double energyGenerated;

    public YearlyPvDatum(int year, double energyGenerated) {
        this.year = year;
        this.energyGenerated = energyGenerated;
    }

    public double getEnergyGenerated() {
        return energyGenerated;
    }

    public int getYear() {
        return year;
    }

    public String toString() {
        return "YearlyPvDatum[" + year +
                ", energyGenerated=" + energyGenerated +
                "]";
    }
}
