package nl.jansipke.pvdisplay.data;

import androidx.annotation.NonNull;

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

    @NonNull
    public String toString() {
        return "YearlyPvDatum{" + year +
                ", energyGenerated=" + energyGenerated +
                "}";
    }
}
