package nl.jansipke.pvdisplay.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FormatUtils {

    public final static NumberFormat ENERGY_FORMAT = new DecimalFormat("#0.000");
    public final static NumberFormat POWER_FORMAT = new DecimalFormat("#0");
    public final static NumberFormat DEGREES_FORMAT = new DecimalFormat("#0.0000");
}
