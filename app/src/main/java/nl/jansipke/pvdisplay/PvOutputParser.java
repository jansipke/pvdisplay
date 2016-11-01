package nl.jansipke.pvdisplay;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.DailyPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.data.YearlyPvDatum;

class PvOutputParser {

    private final static String LINE_SEPARATOR = ";";
    private final static String ITEM_SEPARATOR = ",";

    PvOutputParser() {
    }

    List<DailyPvDatum> parseDay(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("Day data is empty", 0);
        }
        try {
            List<DailyPvDatum> dayPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length < 8) {
                    throw new ParseException("Data does not contain valid day data", 0);
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
            }
            return dayPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    List<LivePvDatum> parseLive(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("Live data is empty", 0);
        }
        try {
            List<LivePvDatum> livePvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length < 5) {
                    throw new ParseException("Data does not contain valid live data", 0);
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
            }
            return livePvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    List<MonthlyPvDatum> parseMonth(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("Month data is empty", 0);
        }
        try {
            List<MonthlyPvDatum> monthPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length < 3) {
                    throw new ParseException("Data does not contain valid month data", 0);
                }
                String date = items[0];
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(4, 6));
                double energyGenerated = Double.parseDouble(items[2]);
                monthPvData.add(new MonthlyPvDatum(
                        year,
                        month,
                        energyGenerated));
            }
            return monthPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    StatisticPvDatum parseStatistic(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("Statistic data is empty", 0);
        }
        try {
            String[] items = data.split(ITEM_SEPARATOR);
            if (items.length < 11) {
                throw new ParseException("Data does not contain valid statistic data", 0);
            }
            double energyGenerated = Double.parseDouble(items[0]);
            double averageGeneration = Double.parseDouble(items[2]);
            double minimumGeneration = Double.parseDouble(items[3]);
            double maximumGeneration = Double.parseDouble(items[4]);
            int outputs = Integer.parseInt(items[6]);
            String actualDateFrom = items[7];
            int actualDateFromYear = Integer.parseInt(actualDateFrom.substring(0, 4));
            int actualDateFromMonth = Integer.parseInt(actualDateFrom.substring(4, 6));
            int actualDateFromDay = Integer.parseInt(actualDateFrom.substring(6, 8));
            String actualDateTo = items[8];
            int actualDateToYear = Integer.parseInt(actualDateTo.substring(0, 4));
            int actualDateToMonth = Integer.parseInt(actualDateTo.substring(4, 6));
            int actualDateToDay = Integer.parseInt(actualDateTo.substring(6, 8));
            String recordDate = items[10];
            int recordDateYear = Integer.parseInt(recordDate.substring(0, 4));
            int recordDateMonth = Integer.parseInt(recordDate.substring(4, 6));
            int recordDateDay = Integer.parseInt(recordDate.substring(6, 8));
            return new StatisticPvDatum(
                    energyGenerated,
                    averageGeneration,
                    minimumGeneration,
                    maximumGeneration,
                    outputs,
                    actualDateFromYear,
                    actualDateFromMonth,
                    actualDateFromDay,
                    actualDateToYear,
                    actualDateToMonth,
                    actualDateToDay,
                    recordDateYear,
                    recordDateMonth,
                    recordDateDay);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    SystemPvDatum parseSystem(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("System data is empty", 0);
        }
        try {
            String[] items = data.split(ITEM_SEPARATOR);
            if (items.length < 15) {
                throw new ParseException("Data does not contain valid system data", 0);
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

    List<YearlyPvDatum> parseYear(String data) throws ParseException {
        if (data == null || data.equals("")) {
            throw new ParseException("Year data is empty", 0);
        }
        try {
            List<YearlyPvDatum> yearPvData = new ArrayList<>();
            String[] lines = data.split(LINE_SEPARATOR);
            for (String line : lines) {
                String[] items = line.split(ITEM_SEPARATOR);
                if (items.length < 3) {
                    throw new ParseException("Data does not contain valid year data", 0);
                }
                int year = Integer.parseInt(items[0]);
                double energyGenerated = Double.parseDouble(items[2]);
                yearPvData.add(new YearlyPvDatum(
                        year,
                        energyGenerated));
            }
            return yearPvData;
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }
}
