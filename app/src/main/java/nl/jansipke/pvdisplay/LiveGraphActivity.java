package nl.jansipke.pvdisplay;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;

public class LiveGraphActivity extends AppCompatActivity {

    private final static String TAG = "LiveGraphActivity";

    private final Date now = new Date();
    private final SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

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

    private void updateGraph(String date) {
        PvDataOperations pvDataOperations = new PvDataOperations(getApplicationContext());
        List<LivePvDatum> livePvData = pvDataOperations.loadLive(date);

        if (livePvData.size() == 0) {
            livePvData.add(new LivePvDatum(date, "00:00", 0.0, 0.0));
            livePvData.add(new LivePvDatum(date, "23:59", 0.0, 0.0));
        }
        DataPoint[] powerDataPoints = new DataPoint[livePvData.size()];
        DataPoint[] energyDataPoints = new DataPoint[livePvData.size()];
        for (int i = 0; i < livePvData.size(); i++) {
            LivePvDatum livePvDatum = livePvData.get(i);
            try {
                Date dateTime = dateTimeFormat.parse(livePvDatum.getDate() + " " + livePvDatum.getTime());
                powerDataPoints[i] = new DataPoint(dateTime, livePvDatum.getPowerGeneration());
                energyDataPoints[i] = new DataPoint(dateTime, livePvDatum.getEnergyGeneration());
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date time", e);
            }
        }
        powerSeries.resetData(powerDataPoints);
        energySeries.resetData(energyDataPoints);
    }

    private void initTable() {
    }

    private void updateTable(String date) {
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText("Ago: " + ago + "\n" + date); // TODO Implement table
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
            supportActionBar.setTitle("Live " + year + "-" + month + "-" + day);
        }

        updateGraph(date);
        updateTable(date);
    }
}
