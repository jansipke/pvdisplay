package nl.jansipke.pvdisplay;

public class LivePvDatum {

    private final String date;
    private final String time;
    private final double energyGeneration;
    private final double powerGeneration;

    public LivePvDatum(String date, String time, double energyGeneration, double powerGeneration) {
        this.date = date;
        this.time = time;
        this.energyGeneration = energyGeneration;
        this.powerGeneration = powerGeneration;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public double getEnergyGeneration() {
        return energyGeneration;
    }

    public double getPowerGeneration() {
        return powerGeneration;
    }

    public String toString() {
        return date + " " + time + " " + energyGeneration + "Wh " + powerGeneration + " W";
    }
}
