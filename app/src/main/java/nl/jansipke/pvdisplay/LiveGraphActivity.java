package nl.jansipke.pvdisplay;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Date;
import java.util.List;

public class LiveGraphActivity extends AppCompatActivity {

    private final Date now = new Date();
    private final SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyyMMdd");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private int ago = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_graph);

        getSupportActionBar().setTitle(R.string.button_live);
        updateScreen();

        PvDataOperations pvDataOperations = new PvDataOperations(getApplicationContext());
        List<LivePvDatum> livePvData = pvDataOperations.loadLive("20160724");

        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] powerDataPoints = new DataPoint[livePvData.size()];
        DataPoint[] energyDataPoints = new DataPoint[livePvData.size()];
        for (int i = 0; i < livePvData.size(); i++) {
            LivePvDatum livePvDatum = livePvData.get(i);
            try {
                Date date = dateTimeFormat.parse(livePvDatum.getDate() + " " + livePvDatum.getTime());
                powerDataPoints[i] = new DataPoint(date, livePvDatum.getPowerGeneration());
                energyDataPoints[i] = new DataPoint(date, livePvDatum.getEnergyGeneration());
            } catch (ParseException e) {
            }
        }
        LineGraphSeries<DataPoint> powerSeries = new LineGraphSeries<>(powerDataPoints);
        powerSeries.setColor(Color.BLUE);
        powerSeries.setTitle("Power (W)");
        graph.addSeries(powerSeries);

        LineGraphSeries<DataPoint> energySeries = new LineGraphSeries<>(energyDataPoints);
        energySeries.setColor(Color.RED);
        energySeries.setTitle("Energy (Wh)");
        graph.getSecondScale().addSeries(energySeries);

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

    public void switchClick(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        viewFlipper.showNext();
    }

    public void updateScreen() {
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText("Ago: " + ago + "\n" + yearMonthDayFormat.format(now));
    }
}
