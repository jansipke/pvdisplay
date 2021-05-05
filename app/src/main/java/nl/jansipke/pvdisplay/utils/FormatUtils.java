package nl.jansipke.pvdisplay.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import nl.jansipke.pvdisplay.data.AxisLabelValues;

public class FormatUtils {

    public final static NumberFormat ENERGY_FORMAT = new DecimalFormat("#0.000");
    public final static NumberFormat POWER_FORMAT = new DecimalFormat("#0");
    public final static NumberFormat SAVINGS_FORMAT = new DecimalFormat("#0.00");
    public final static NumberFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.00 %");

    public static AxisLabelValues getAxisLabelValues(double maxValue) {
        final int nrSteps = 5;
        final double viewFactor = 1.1;

        double step = maxValue / nrSteps;
        int scale = 1;
        while (step > 10) {
            step /= 10;
            scale *= 10;
        }
        step = Math.ceil(step);

        return new AxisLabelValues(
                (float) (scale * step * nrSteps),
                (float) (scale * step),
                (float) (scale * viewFactor * step * nrSteps));
    }
}
