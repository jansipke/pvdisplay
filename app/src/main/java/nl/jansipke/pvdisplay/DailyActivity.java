package nl.jansipke.pvdisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class DailyActivity extends AppCompatActivity {

    private final static String TAG = DailyActivity.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private static DateTimeUtils.YearMonth picked;

    private PvDataOperations pvDataOperations;

    public void clickedOnOldest(View view) {
        // TODO Implement
    }

    public void clickedOnPrevious(View view) {
        picked = DateTimeUtils.addMonths(picked, -1);
        updateScreen(false);
    }

    public void clickedOnNext(View view) {
        picked = DateTimeUtils.addMonths(picked, 1);
        updateScreen(false);
    }

    public void clickedOnNewest(View view) {
        picked = DateTimeUtils.getTodaysYearMonth();
        updateScreen(false);
    }

    public void clickedOnRefresh(View view) {
        updateScreen(true);
    }

    public void clickedOnSwitch(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        viewFlipper.showNext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        picked = DateTimeUtils.getTodaysYearMonth();
        pvDataOperations = new PvDataOperations(getApplicationContext());

        updateScreen(false);
    }

    private void updateGraph(List<HistoricalPvDatum> historicalPvData) {
        ColumnChartView columnChartView = (ColumnChartView) findViewById(graph);

        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> subcolumnValues;
        List<AxisValue> xAxisValues = new ArrayList<>();
        int newestDay = 0;
        for (int i = 0; i < historicalPvData.size(); i++) {
            HistoricalPvDatum historicalPvDatum = historicalPvData.get(i);
            subcolumnValues = new ArrayList<>();
            subcolumnValues.add(new SubcolumnValue(
                    (float) historicalPvDatum.getEnergyGenerated(),
                    ChartUtils.COLORS[0]));
            columns.add(new Column(subcolumnValues));

            String xLabel = DateTimeUtils.formatDate(
                    historicalPvDatum.getYear(),
                    historicalPvDatum.getMonth(),
                    historicalPvDatum.getDay(),
                    true);
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(xLabel);
            xAxisValues.add(axisValue);

            if (historicalPvDatum.getDay() > newestDay) {
                newestDay = historicalPvDatum.getDay();
            }
        }
        ColumnChartData columnChartData = new ColumnChartData(columns);

        Axis xAxis = new Axis()
                .setValues(xAxisValues)
                .setMaxLabelChars(10);
        columnChartData.setAxisXBottom(xAxis);

        Axis yAxis = Axis
                .generateAxisFromRange(0, 10000, 1000) // TODO Use real maximum value
                .setMaxLabelChars(6);
        columnChartData.setAxisYLeft(yAxis);

        columnChartView.setColumnChartData(columnChartData);

        columnChartView.setViewportCalculationEnabled(false);
        final Viewport viewport = new Viewport(-1, 10500, newestDay, 0);
        columnChartView.setMaximumViewport(viewport);
        columnChartView.setCurrentViewport(viewport);
    }

    private void updateTable(List<HistoricalPvDatum> historicalPvData) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.table);
        linearLayout.removeAllViews();
        final int nrRows = 16;
        View[] rows = new View[nrRows];
        for (int i = 0; i < nrRows; i++) {
            rows[i] = getLayoutInflater().inflate(R.layout.table_4column_row, null);
        }
        for (HistoricalPvDatum historicalPvDatum : historicalPvData) {
            int day = historicalPvDatum.getDay();
            if (day <= 15 ) {
                ((TextView) rows[day - 1].findViewById(R.id.content1)).setText("" + day);
                ((TextView) rows[day - 1].findViewById(R.id.content2)).setText(energyFormat.format(
                        historicalPvDatum.getEnergyGenerated() / 1000.0));
            } else {
                ((TextView) rows[day - nrRows].findViewById(R.id.content3)).setText("" + day);
                ((TextView) rows[day - nrRows].findViewById(R.id.content4)).setText(energyFormat.format(
                        historicalPvDatum.getEnergyGenerated() / 1000.0));
            }
        }
        for (int i = 0; i < nrRows; i++) {
            linearLayout.addView(rows[i]);
        }
    }

    public void updateScreen(boolean refreshData) {
        Log.i(TAG, "Showing screen");

        String title = "Daily   " + DateTimeUtils.formatDate(
                picked.year, picked.month, true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }

        List<HistoricalPvDatum> historicalPvData = pvDataOperations.loadHistorical(
                picked.year, picked.month, 1,
                picked.year, picked.month, 31);

        if (refreshData || historicalPvData.size() == 0) {
            if (refreshData) {
                Log.i(TAG, "Refreshing historical PV data for month " +
                        DateTimeUtils.formatDate(picked.year, picked.month, 1, true) +
                        " to " +
                        DateTimeUtils.formatDate(picked.year, picked.month, 31, true));
            } else {
                Log.i(TAG, "No live PV data for " +
                        DateTimeUtils.formatDate(picked.year, picked.month, 1, true) +
                        " to " +
                        DateTimeUtils.formatDate(picked.year, picked.month, 31, true));
            }

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateScreen(false);
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callHistorical(getApplicationContext(),
                    picked.year, picked.month, 1, picked.year, picked.month, 31);
        }
        updateGraph(historicalPvData);
        updateTable(historicalPvData);
    }
}
