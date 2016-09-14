package nl.jansipke.pvdisplay.parsers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;

public class PvOutputParser {

    private final static String LINE_SEPARATOR = ";";
    private final static String ITEM_SEPARATOR = ",";

    public PvOutputParser() {
    }

    public List<HistoricalPvDatum> parseHistorical(String data) throws ParseException {
        try {
            List<HistoricalPvDatum> historicalPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                String date = items[0];
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                int day = Integer.parseInt(date.substring(6, 8));
                double energyGenerated = Double.parseDouble(items[1]);
                historicalPvData.add(new HistoricalPvDatum(year, month, day, energyGenerated));
            }
            return historicalPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public List<LivePvDatum> parseLive(String data) throws ParseException {
        try {
            List<LivePvDatum> livePvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                String date = items[0]; // yyyyMMdd
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                int day = Integer.parseInt(date.substring(6, 8));
                String time = items[1]; // HH:mm
                int hour = Integer.parseInt(time.substring(0, 2));
                int minute = Integer.parseInt(time.substring(3, 5));
                double energyGeneration = Double.parseDouble(items[2]);
                double powerGeneration = Double.parseDouble(items[4]);
                livePvData.add(new LivePvDatum(year, month, day, hour, minute, energyGeneration, powerGeneration));
            }
            return livePvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public StatisticPvDatum parseStatistic(String data) throws ParseException {
        try {
            String[] items = data.split(ITEM_SEPARATOR);
            double energyGenerated = Double.parseDouble(items[0]);
            double averageGeneration = Double.parseDouble(items[2]);
            double minimumGeneration = Double.parseDouble(items[3]);
            double maximumGeneration = Double.parseDouble(items[4]);
            int outputs = Integer.parseInt(items[6]);
            String actualDateFrom = items[7];
            String actualDateTo = items[8];
            String recordDate = items[10];
            return new StatisticPvDatum(energyGenerated, averageGeneration, minimumGeneration, maximumGeneration, outputs, actualDateFrom, actualDateTo, recordDate);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}
