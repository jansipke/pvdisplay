package nl.jansipke.pvdisplay.utils;

import com.jjoe64.graphview.series.DataPoint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingUtils {

    private final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm");

    public static String formatDataPoints(DataPoint[] dataPoints) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < dataPoints.length; i+= 6) {
            Date date = new Date();
            date.setTime((long) dataPoints[i].getX());
            sb.append(dateTimeFormat.format(date));
            sb.append(", x=" + dataPoints[i].getX());
            sb.append(", y=" + dataPoints[i].getY());
            sb.append("\n");
        }
        return sb.toString();
    }
}
