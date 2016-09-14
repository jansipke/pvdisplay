package nl.jansipke.pvdisplay.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LivePvDatum {

    private final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private final long timestamp;          // Seconds since epoch
    private final double energyGeneration; // In Watt (W)
    private final double powerGeneration;  // In Watt Hour (Wh)
    private final Date date;

    public LivePvDatum(long timestamp, double energyGeneration, double powerGeneration) {
        this.timestamp = timestamp;
        this.energyGeneration = energyGeneration;
        this.powerGeneration = powerGeneration;
        date = new Date(timestamp * 1000);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getEnergyGeneration() {
        return energyGeneration;
    }

    public double getPowerGeneration() {
        return powerGeneration;
    }

    public Date getDate() {
        return date;
    }

    public String toString() {
        return "LivePvDatum[" + dateTimeFormat.format(date) + " power=" + powerGeneration + " W, energy=" + energyGeneration + " Wh]";
    }
}
