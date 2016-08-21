package nl.jansipke.pvdisplay;

public class StatisticPvDatum {

    private final double energyGenerated;
    private final double averageGeneration;
    private final double minimumGeneration;
    private final double maximumGeneration;
    private final int outputs;
    private final String actualDateFrom;
    private final String actualDateTo;
    private final String recordDate;

    public StatisticPvDatum(double energyGenerated, double averageGeneration, double minimumGeneration, double maximumGeneration, int outputs, String actualDateFrom, String actualDateTo, String recordDate) {
        this.energyGenerated = energyGenerated;
        this.averageGeneration = averageGeneration;
        this.minimumGeneration = minimumGeneration;
        this.maximumGeneration = maximumGeneration;
        this.outputs = outputs;
        this.actualDateFrom = actualDateFrom;
        this.actualDateTo = actualDateTo;
        this.recordDate = recordDate;
    }

    public double getEnergyGenerated() {
        return energyGenerated;
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

    public int getOutputs() {
        return outputs;
    }

    public String getActualDateFrom() {
        return actualDateFrom;
    }

    public String getActualDateTo() {
        return actualDateTo;
    }

    public String getRecordDate() {
        return recordDate;
    }

    @Override
    public String toString() {
        return "StatisticPvDatum{" +
                "energyGenerated=" + energyGenerated +
                ", averageGeneration=" + averageGeneration +
                ", minimumGeneration=" + minimumGeneration +
                ", maximumGeneration=" + maximumGeneration +
                ", outputs=" + outputs +
                ", actualDateFrom='" + actualDateFrom + '\'' +
                ", actualDateTo='" + actualDateTo + '\'' +
                ", recordDate='" + recordDate + '\'' +
                '}';
    }
}
