package nl.jansipke.pvdisplay.parsers;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;

public class PvOutputParser {

    public PvOutputParser() {
    }

    public List<HistoricalPvDatum> parseHistorical(String data) {
        List<HistoricalPvDatum> historicalPvData = new ArrayList<>();
        String[] lines = data.split(";");
        for (String line : lines) {
            String[] items = line.split(",");
            String date = items[0];
            double energyGenerated = Double.parseDouble(items[1]);
            historicalPvData.add(new HistoricalPvDatum(date, energyGenerated));
        }
        return historicalPvData;
    }

    public List<LivePvDatum> parseLive(String data) {
        Log.i("PvOutputParser", data);
        List<LivePvDatum> livePvData = new ArrayList<>();
        String[] lines = data.split(";");
        for (String line : lines) {
            String[] items = line.split(",");
            String date = items[0];
            String time = items[1];
            double energyGeneration = Double.parseDouble(items[2]);
            double powerGeneration = Double.parseDouble(items[4]);
            livePvData.add(new LivePvDatum(date, time, energyGeneration, powerGeneration));
        }
        return livePvData;
    }

    public StatisticPvDatum parseStatistic(String data) {
        String[] items = data.split(",");
        double energyGenerated = Double.parseDouble(items[0]);
        double averageGeneration = Double.parseDouble(items[2]);
        double minimumGeneration = Double.parseDouble(items[3]);
        double maximumGeneration = Double.parseDouble(items[4]);
        int outputs = Integer.parseInt(items[6]);
        String actualDateFrom = items[7];
        String actualDateTo = items[8];
        String recordDate = items[10];
        return new StatisticPvDatum(energyGenerated, averageGeneration, minimumGeneration, maximumGeneration, outputs, actualDateFrom, actualDateTo, recordDate);
    }
}
