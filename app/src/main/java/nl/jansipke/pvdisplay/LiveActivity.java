package nl.jansipke.pvdisplay;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;

public class LiveActivity extends AppCompatActivity {

    private final static String TAG = "LiveActivity";

    private final Date now = new Date();
    private final SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final NumberFormat powerFormat = new DecimalFormat("#0");
    private final NumberFormat energyFormat = new DecimalFormat("#0.000");

    private int ago = 0;
    private LineGraphSeries<DataPoint> powerSeries;
    private LineGraphSeries<DataPoint> energySeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_graph);

        initGraph();
        initTable();
        updateScreen();
    }

    public void previousClick(View view) {
        ago++;
        updateScreen();
    }

    public void nextClick(View view) {
        if (ago > 0) {
            ago--;
            updateScreen();
        }
    }

    public void nowClick(View view) {
        if (ago > 0) {
            ago = 0;
            updateScreen();
        }
    }

    private void initGraph() {
        GraphView graph = (GraphView) findViewById(R.id.graph);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext(), timeFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getGridLabelRenderer().setNumVerticalLabels(4);
        graph.getGridLabelRenderer().setHumanRounding(false);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1500); // TODO Replace with historical maximum
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(12000); // TODO Replace with historical maximum

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        powerSeries = new LineGraphSeries<>();
        powerSeries.setColor(Color.BLUE);
        powerSeries.setTitle("Power (W)");
        graph.addSeries(powerSeries);

        energySeries = new LineGraphSeries<>();
        energySeries.setColor(Color.RED);
        energySeries.setTitle("Energy (Wh)");
        graph.getSecondScale().addSeries(energySeries);
    }

    private DataPoint createDataPoint(String date, String time, double value) {
        try {
            Date dateTime = dateTimeFormat.parse(date + " " + time);
            return new DataPoint(dateTime, value);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date time", e);
            return null;
        }
    }

    private void updateGraph(String date, List<LivePvDatum> livePvData) {
        DataPoint[] powerDataPoints;
        DataPoint[] energyDataPoints;

        if (livePvData.size() == 0) {
            powerDataPoints = new DataPoint[2];
            powerDataPoints[0] = createDataPoint(date, "00:00", 0.0);
            powerDataPoints[1] = createDataPoint(date, "23:59", 0.0);
            energyDataPoints = new DataPoint[2];
            energyDataPoints[0] = createDataPoint(date, "00:00", 0.0);
            energyDataPoints[1] = createDataPoint(date, "23:59", 0.0);
        } else {
            powerDataPoints = new DataPoint[livePvData.size()];
            energyDataPoints = new DataPoint[livePvData.size()];
            for (int i = 0; i < livePvData.size(); i++) {
                LivePvDatum livePvDatum = livePvData.get(i);
                powerDataPoints[i] = createDataPoint(livePvDatum.getDate(), livePvDatum.getTime(), livePvDatum.getPowerGeneration());
                energyDataPoints[i] = createDataPoint(livePvDatum.getDate(), livePvDatum.getTime(), livePvDatum.getEnergyGeneration());
            }
        }

        powerSeries.resetData(powerDataPoints);
        energySeries.resetData(energyDataPoints);
    }

    private void initTable() {
    }

    private void updateTable(String date, List<LivePvDatum> livePvData) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.table);
        linearLayout.removeAllViews();
        for (LivePvDatum livePvDatum : livePvData) {
            View row = getLayoutInflater().inflate(R.layout.table_row, null);
            ((TextView) row.findViewById(R.id.content1)).setText(livePvDatum.getTime());
            ((TextView) row.findViewById(R.id.content2)).setText(powerFormat.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.content3)).setText(energyFormat.format(livePvDatum.getEnergyGeneration() / 1000));
            linearLayout.addView(row);
        }
    }

    public void switchClick(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        viewFlipper.showNext();
    }

    public void updateScreen() {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -ago);
        String date = yearMonthDayFormat.format(calendar.getTime());

        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Live   " + year + "-" + month + "-" + day);
        }

        PvDataOperations pvDataOperations = new PvDataOperations(getApplicationContext());
        List<LivePvDatum> livePvData = pvDataOperations.loadLive(date);
        updateGraph(date, livePvData);
        updateTable(date, livePvData);
    }
}
