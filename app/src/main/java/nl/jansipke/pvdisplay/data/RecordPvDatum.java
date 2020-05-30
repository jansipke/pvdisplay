package nl.jansipke.pvdisplay.data;

import androidx.annotation.NonNull;

public class RecordPvDatum {

    private double livePowerGeneration;
    private double dailyEnergyGenerated;
    private double monthlyEnergyGenerated;
    private double yearlyEnergyGenerated;

    public RecordPvDatum(double livePowerGeneration, double dailyEnergyGenerated,
                         double monthlyEnergyGenerated, double yearlyEnergyGenerated) {
        this.livePowerGeneration = livePowerGeneration;
        this.dailyEnergyGenerated = dailyEnergyGenerated;
        this.monthlyEnergyGenerated = monthlyEnergyGenerated;
        this.yearlyEnergyGenerated = yearlyEnergyGenerated;
    }

    public double getLivePowerGeneration() {
        return livePowerGeneration;
    }

    public double getDailyEnergyGenerated() {
        return dailyEnergyGenerated;
    }

    public double getMonthlyEnergyGenerated() {
        return monthlyEnergyGenerated;
    }

    public double getYearlyEnergyGenerated() {
        return yearlyEnergyGenerated;
    }

    public void setLivePowerGeneration(double livePower) {
        this.livePowerGeneration = livePower;
    }

    public void setDailyEnergyGenerated(double dailyEnergyGenerated) {
        this.dailyEnergyGenerated = dailyEnergyGenerated;
    }

    public void setMonthlyEnergyGenerated(double monthlyEnergyGenerated) {
        this.monthlyEnergyGenerated = monthlyEnergyGenerated;
    }

    public void setYearlyEnergyGenerated(double yearlyEnergyGenerated) {
        this.yearlyEnergyGenerated = yearlyEnergyGenerated;
    }

    @NonNull
    public String toString() {
        return "RecordPvDatum[" +
                "livePowerGeneration=" + livePowerGeneration +
                ", dailyEnergyGenerated=" + dailyEnergyGenerated +
                ", monthlyEnergyGenerated=" + monthlyEnergyGenerated +
                ", yearlyEnergyGenerated=" + yearlyEnergyGenerated +
                ']';
    }
}
