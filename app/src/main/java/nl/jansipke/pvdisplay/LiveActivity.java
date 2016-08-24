package nl.jansipke.pvdisplay;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.YearMonthDay;
import nl.jansipke.pvdisplay.database.PvDataOperations;

public class LiveActivity extends AppCompatActivity {

    private final static String TAG = "LiveActivity";
    private final static SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private final Date now = new Date();
    private int ago = 30;

    private LineGraphSeries<DataPoint> powerSeries;
    private LineGraphSeries<DataPoint> energySeries;
    private DataPoint[] powerDataPoints = new DataPoint[24 * 12];
    private DataPoint[] energyDataPoints = new DataPoint[24 * 12];

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
        graph.getSecondScale().setMaxY(12); // TODO Replace with historical maximum

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        powerSeries = new LineGraphSeries<>();
        powerSeries.setColor(Color.BLUE);
        powerSeries.setTitle(getResources().getString(R.string.graph_legend_power));
        graph.addSeries(powerSeries);

        energySeries = new LineGraphSeries<>();
        energySeries.setColor(Color.RED);
        energySeries.setTitle(getResources().getString(R.string.graph_legend_energy));
        graph.getSecondScale().addSeries(energySeries);
    }

    private void updateGraph(YearMonthDay yearMonthDay, List<LivePvDatum> livePvData) {
        long firstTimestamp = yearMonthDay.getFirstTimestamp(); // 00:00
        int livePvDataIndex = 0;
        double energyValue = 0.0;
        for (int i = 0; i < powerDataPoints.length; i++) {
            long timestamp = firstTimestamp + i * 5 * 60;
            Date date = new Date(timestamp * 1000);
            if (livePvDataIndex < livePvData.size() && livePvData.get(livePvDataIndex).getTimestamp() == timestamp) {
                LivePvDatum livePvDatum = livePvData.get(livePvDataIndex);
                powerDataPoints[i] = new DataPoint(date, livePvDatum.getPowerGeneration());
                energyValue = livePvDatum.getEnergyGeneration() / 1000.0;
                energyDataPoints[i] = new DataPoint(date, energyValue);
                livePvDataIndex++;
            } else {
                powerDataPoints[i] = new DataPoint(date, 0.0);
                energyDataPoints[i] = new DataPoint(date, energyValue);
            }
        }
        powerSeries.resetData(powerDataPoints);
        energySeries.resetData(energyDataPoints);
    }

    private void initTable() {
    }

    private void updateTable(List<LivePvDatum> livePvData) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.table);
        linearLayout.removeAllViews();
        for (LivePvDatum livePvDatum : livePvData) {
            View row = getLayoutInflater().inflate(R.layout.table_row, null);
            ((TextView) row.findViewById(R.id.content1)).setText(timeFormat.format(new Date(livePvDatum.getTimestamp() * 1000)));
            ((TextView) row.findViewById(R.id.content2)).setText(powerFormat.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.content3)).setText(energyFormat.format(livePvDatum.getEnergyGeneration() / 1000.0));
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
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        YearMonthDay yearMonthDay = new YearMonthDay(year, month, day);

        String title = "Live   " + yearMonthDayFormat.format(calendar.getTime());
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }

        PvDataOperations pvDataOperations = new PvDataOperations(getApplicationContext());
        List<LivePvDatum> livePvData = pvDataOperations.loadLive(yearMonthDay);
        updateGraph(yearMonthDay, livePvData);
        updateTable(livePvData);
    }
}
