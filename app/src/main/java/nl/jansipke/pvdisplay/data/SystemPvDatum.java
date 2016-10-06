package nl.jansipke.pvdisplay.data;

public class SystemPvDatum {

    private final String systemName;
    private final int systemSize;
    private final int numberOfPanels;
    private final int panelPower;
    private final String panelBrand;
    private final int inverterPower;
    private final String inverterBrand;
    private final double latitude;
    private final double longitude;

    public SystemPvDatum(String systemName, int systemSize, int numberOfPanels, int panelPower,
                         String panelBrand, int inverterPower, String inverterBrand,
                         double latitude, double longitude) {
        this.systemName = systemName;
        this.systemSize = systemSize;
        this.numberOfPanels = numberOfPanels;
        this.panelPower = panelPower;
        this.panelBrand = panelBrand;
        this.inverterPower = inverterPower;
        this.inverterBrand = inverterBrand;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSystemName() {
        return systemName;
    }

    public int getSystemSize() {
        return systemSize;
    }

    public int getNumberOfPanels() {
        return numberOfPanels;
    }

    public int getPanelPower() {
        return panelPower;
    }

    public String getPanelBrand() {
        return panelBrand;
    }

    public int getInverterPower() {
        return inverterPower;
    }

    public String getInverterBrand() {
        return inverterBrand;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "SystemPvDatum[" +
                "systemName='" + systemName + '\'' +
                ", systemSize=" + systemSize +
                ", numberOfPanels=" + numberOfPanels +
                ", panelPower=" + panelPower +
                ", panelBrand='" + panelBrand + '\'' +
                ", inverterPower=" + inverterPower +
                ", inverterBrand='" + inverterBrand + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ']';
    }
}
