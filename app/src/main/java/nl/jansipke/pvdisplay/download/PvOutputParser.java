package nl.jansipke.pvdisplay.download;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.DailyPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.data.YearlyPvDatum;

public class PvOutputParser {

    private final static String LINE_SEPARATOR = ";";
    private final static String ITEM_SEPARATOR = ",";
    private final static String SECTION_SEPARATOR = ";";

    public PvOutputParser() {
    }

    public List<DailyPvDatum> parseDaily(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("PV daily data is empty", 0);
        }
        try {
            List<DailyPvDatum> dayPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            int lineNr = 1;
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length != 14) {
                    throw new ParseException("PV daily data is not valid", lineNr);
                }
                String date = items[0];
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                int day = Integer.parseInt(date.substring(6, 8));
                double energyGenerated = Double.parseDouble(items[1]);
                double peakPower = Double.parseDouble(items[5]);
                String condition = items[7];
                dayPvData.add(new DailyPvDatum(
                        year,
                        month,
                        day,
                        energyGenerated,
                        peakPower,
                        condition));
                lineNr++;
            }
            return dayPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public List<LivePvDatum> parseLive(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("PV live data is empty", 0);
        }
        try {
            List<LivePvDatum> livePvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            int lineNr = 1;
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length != 11 && items.length != 17) {
                    throw new ParseException("PV live data is not valid", lineNr);
                }
                String date = items[0]; // yyyyMMdd
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                int day = Integer.parseInt(date.substring(6, 8));
                String time = items[1]; // HH:mm
                int hour = Integer.parseInt(time.substring(0, 2));
                int minute = Integer.parseInt(time.substring(3, 5));
                double energyGeneration = Double.parseDouble(items[2]);
                double powerGeneration = Double.parseDouble(items[4]);
                livePvData.add(new LivePvDatum(
                        year,
                        month,
                        day,
                        hour,
                        minute,
                        energyGeneration,
                        powerGeneration));
                lineNr++;
            }
            return livePvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public List<MonthlyPvDatum> parseMonthly(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("PV monthly data is empty", 0);
        }
        try {
            List<MonthlyPvDatum> monthPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            int lineNr = 1;
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length != 10) {
                    throw new ParseException("PV monthly data is not valid", lineNr);
                }
                String date = items[0];
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                double energyGenerated = Double.parseDouble(items[2]);
                monthPvData.add(new MonthlyPvDatum(
                        year,
                        month,
                        energyGenerated));
                lineNr++;
            }
            return monthPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public StatisticPvDatum parseStatistic(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("PV statistic data is empty", 0);
        }
        try {
            String[] items = data.split(ITEM_SEPARATOR);
            if (items.length != 11) {
                throw new ParseException("PV statistic data is not valid", 1);
            }
            double energyGenerated = Double.parseDouble(items[0]);
            double energyExported = Double.parseDouble(items[1]);
            double averageGeneration = Double.parseDouble(items[2]);
            double minimumGeneration = Double.parseDouble(items[3]);
            double maximumGeneration = Double.parseDouble(items[4]);
            double averageEfficiency = Double.parseDouble(items[5]);
            int outputs = Integer.parseInt(items[6]);
            String actualDateFrom = items[7];
            int actualDateFromYear = Integer.parseInt(actualDateFrom.substring(0, 4));
            int actualDateFromMonth = Integer.parseInt(actualDateFrom.substring(4, 6));
            int actualDateFromDay = Integer.parseInt(actualDateFrom.substring(6, 8));
            String actualDateTo = items[8];
            int actualDateToYear = Integer.parseInt(actualDateTo.substring(0, 4));
            int actualDateToMonth = Integer.parseInt(actualDateTo.substring(4, 6));
            int actualDateToDay = Integer.parseInt(actualDateTo.substring(6, 8));
            double recordEfficiency = Double.parseDouble(items[9]);
            String recordDate = items[10];
            int recordDateYear = Integer.parseInt(recordDate.substring(0, 4));
            int recordDateMonth = Integer.parseInt(recordDate.substring(4, 6));
            int recordDateDay = Integer.parseInt(recordDate.substring(6, 8));
            return new StatisticPvDatum(
                    energyGenerated,
                    energyExported,
                    averageGeneration,
                    minimumGeneration,
                    maximumGeneration,
                    averageEfficiency,
                    outputs,
                    actualDateFromYear,
                    actualDateFromMonth,
                    actualDateFromDay,
                    actualDateToYear,
                    actualDateToMonth,
                    actualDateToDay,
                    recordEfficiency,
                    recordDateYear,
                    recordDateMonth,
                    recordDateDay);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public SystemPvDatum parseSystem(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("PV system data is empty", 0);
        }
        try {
            String[] sections = data.split(SECTION_SEPARATOR);
            if (sections.length != 3) {
                throw new ParseException("PV system data is not valid", 0);
            }
            String[] items = sections[0].split(ITEM_SEPARATOR);
            if (items.length != 16) {
                throw new ParseException("PV system data is not valid", 0);
            }
            String systemName = items[0];
            int systemSize = Integer.parseInt(items[1]);
            int numberOfPanels = Integer.parseInt(items[3]);
            int panelPower = Integer.parseInt(items[4]);
            String panelBrand = items[5];
            int inverterPower = Integer.parseInt(items[7]);
            String inverterBrand = items[8];
            double latitude = Double.parseDouble(items[13]);
            double longitude = Double.parseDouble(items[14]);
            return new SystemPvDatum(
                    systemName,
                    systemSize,
                    numberOfPanels,
                    panelPower,
                    panelBrand,
                    inverterPower,
                    inverterBrand,
                    latitude,
                    longitude);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public List<YearlyPvDatum> parseYearly(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("PV yearly data is empty", 0);
        }
        try {
            List<YearlyPvDatum> yearPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            int lineNr = 1;
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length != 10) {
                    throw new ParseException("PV yearly data is not valid", lineNr);
                }
                int year = Integer.parseInt(items[0]);
                double energyGenerated = Double.parseDouble(items[2]);
                yearPvData.add(new YearlyPvDatum(
                        year,
                        energyGenerated));
                lineNr++;
            }
            return yearPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}
