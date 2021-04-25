package nl.jansipke.pvdisplay.data;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class StatisticPvDatum {

    private final double energyGenerated;
    private final double energyExported;
    private final double averageGeneration;
    private final double minimumGeneration;
    private final double maximumGeneration;
    private final double averageEfficiency;
    private final int outputs;
    private final int actualDateFromYear;
    private final int actualDateFromMonth;
    private final int actualDateFromDay;
    private final int actualDateToYear;
    private final int actualDateToMonth;
    private final int actualDateToDay;
    private final double recordEfficiency;
    private final int recordDateYear;
    private final int recordDateMonth;
    private final int recordDateDay;

    public StatisticPvDatum(double energyGenerated, double energyExported, double averageGeneration,
                            double minimumGeneration, double maximumGeneration, double averageEfficiency, int outputs,
                            int actualDateFromYear, int actualDateFromMonth, int actualDateFromDay,
                            int actualDateToYear, int actualDateToMonth, int actualDateToDay, double recordEfficiency,
                            int recordDateYear, int recordDateMonth, int recordDateDay) {
        this.energyGenerated = energyGenerated;
        this.energyExported = energyExported;
        this.averageGeneration = averageGeneration;
        this.minimumGeneration = minimumGeneration;
        this.maximumGeneration = maximumGeneration;
        this.averageEfficiency = averageEfficiency;
        this.outputs = outputs;
        this.actualDateFromYear = actualDateFromYear;
        this.actualDateFromMonth = actualDateFromMonth;
        this.actualDateFromDay = actualDateFromDay;
        this.actualDateToYear = actualDateToYear;
        this.actualDateToMonth = actualDateToMonth;
        this.actualDateToDay = actualDateToDay;
        this.recordEfficiency = recordEfficiency;
        this.recordDateYear = recordDateYear;
        this.recordDateMonth = recordDateMonth;
        this.recordDateDay = recordDateDay;
    }

    public double getEnergyGenerated() {
        return energyGenerated;
    }

    public double getEnergyExported() {
        return energyExported;
    }

    public double getAverageGeneration() {
        return averageGeneration;
    }

    public double getMinimumGeneration() {
        return minimumGeneration;
    }

    public double getMaximumGeneration() {
        return maximumGeneration;
    }

    public double getAverageEfficiency() {
        return averageEfficiency;
    }

    public int getOutputs() {
        return outputs;
    }

    public int getActualDateFromYear() {
        return actualDateFromYear;
    }

    public int getActualDateFromMonth() {
        return actualDateFromMonth;
    }

    public int getActualDateFromDay() {
        return actualDateFromDay;
    }

    public int getActualDateToYear() {
        return actualDateToYear;
    }

    public int getActualDateToMonth() {
        return actualDateToMonth;
    }

    public int getActualDateToDay() {
        return actualDateToDay;
    }

    public double getRecordEfficiency() {
        return recordEfficiency;
    }

    public int getRecordDateYear() {
        return recordDateYear;
    }

    public int getRecordDateMonth() {
        return recordDateMonth;
    }

    public int getRecordDateDay() {
        return recordDateDay;
    }

    @NonNull
    public String toString() {
        return "StatisticPvDatum[" +
                "energyGenerated=" + energyGenerated +
                ", energyExported=" + energyExported +
                ", averageGeneration=" + averageGeneration +
                ", minimumGeneration=" + minimumGeneration +
                ", maximumGeneration=" + maximumGeneration +
                ", averageEfficiency=" + averageEfficiency +
                ", outputs=" + outputs +
                ", actualDateFromYear=" + actualDateFromYear +
                ", actualDateFromMonth=" + actualDateFromMonth +
                ", actualDateFromDay=" + actualDateFromDay +
                ", actualDateToYear=" + actualDateToYear +
                ", actualDateToMonth=" + actualDateToMonth +
                ", actualDateToDay=" + actualDateToDay +
                ", recordEfficiency=" + recordEfficiency +
                ", recordDateYear=" + recordDateYear +
                ", recordDateMonth=" + recordDateMonth +
                ", recordDateDay=" + recordDateDay +
                "]";
    }
}
