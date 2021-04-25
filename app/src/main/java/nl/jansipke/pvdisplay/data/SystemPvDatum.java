package nl.jansipke.pvdisplay.data;

import androidx.annotation.NonNull;

public class SystemPvDatum {

    private final String systemName;
    private final int systemSize;
    private final String postcode;
    private final int numberOfPanels;
    private final int panelPower;
    private final String panelBrand;
    private final int inverters;
    private final int inverterPower;
    private final String inverterBrand;
    private final String orientation;
    private final String arrayTilt;
    private final String shade;
    private final String installDate;
    private final double latitude;
    private final double longitude;

    public SystemPvDatum(String systemName, int systemSize, String postcode, int numberOfPanels, int panelPower,
                         String panelBrand, int inverters, int inverterPower, String inverterBrand, String orientation,
                         String arrayTilt, String shade, String installDate, double latitude, double longitude) {
        this.systemName = systemName;
        this.systemSize = systemSize;
        this.postcode = postcode;
        this.numberOfPanels = numberOfPanels;
        this.panelPower = panelPower;
        this.panelBrand = panelBrand;
        this.inverters = inverters;
        this.inverterPower = inverterPower;
        this.inverterBrand = inverterBrand;
        this.orientation = orientation;
        this.arrayTilt = arrayTilt;
        this.shade = shade;
        this.installDate = installDate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSystemName() {
        return systemName;
    }

    public int getSystemSize() {
        return systemSize;
    }

    public String getPostcode() {
        return postcode;
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

    public int getInverters() {
        return inverters;
    }

    public int getInverterPower() {
        return inverterPower;
    }

    public String getInverterBrand() {
        return inverterBrand;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getArrayTilt() {
        return arrayTilt;
    }

    public String getShade() {
        return shade;
    }

    public String getInstallDate() {
        return installDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @NonNull
    public String toString() {
        return "SystemPvDatum{" +
                "systemName='" + systemName + '\'' +
                ", systemSize=" + systemSize +
                ", postcode='" + postcode + '\'' +
                ", numberOfPanels=" + numberOfPanels +
                ", panelPower=" + panelPower +
                ", panelBrand='" + panelBrand + '\'' +
                ", inverters=" + inverters +
                ", inverterPower=" + inverterPower +
                ", inverterBrand='" + inverterBrand + '\'' +
                ", orientation='" + orientation + '\'' +
                ", arrayTilt=" + arrayTilt +
                ", shade='" + shade + '\'' +
                ", installDate='" + installDate + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                "}";
    }
}
