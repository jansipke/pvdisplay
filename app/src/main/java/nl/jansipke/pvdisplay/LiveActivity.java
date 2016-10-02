package nl.jansipke.pvdisplay;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class LiveActivity extends AppCompatActivity {

    public static class DatePickerFragment
            extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DateTimeUtils.YearMonthDay yearMonthDay = DateTimeUtils.getYearMonthDay(daysAgo);
            int year = yearMonthDay.year;
            int month = yearMonthDay.month;
            int day = yearMonthDay.day;
            return new DatePickerDialog(getActivity(), this, year, month - 1, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Log.i(TAG, "Date picked: " + DateTimeUtils.formatDate(year, month, day, true));
        }
    }

    private final static String TAG = LiveActivity.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private static int daysAgo = 0;

    private PvDataOperations pvDataOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        pvDataOperations = new PvDataOperations(getApplicationContext());
        updateScreen(false);
    }

    public void oldestClick(View view) {
        // TODO Implement
    }

    public void previousClick(View view) {
        daysAgo++;
        updateScreen(false);
    }

    public void nextClick(View view) {
        if (daysAgo > 0) {
            daysAgo--;
            updateScreen(false);
        }
    }

    public void newestClick(View view) {
        if (daysAgo > 0) {
            daysAgo = 0;
            updateScreen(false);
        }
    }

    public void dateClick(View view) {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void refreshClick(View view) {
        updateScreen(true);
    }

    public void switchClick(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        viewFlipper.showNext();
    }

    private void updateGraph(List<LivePvDatum> livePvData) {
        LineChartView lineChartView = (LineChartView) findViewById(graph);

        List<PointValue> powerPointValues = new ArrayList<>();
        List<PointValue> maxPointValues = new ArrayList<>();
        List<AxisValue> xAxisValues = new ArrayList<>();
        for (int i = 0; i < livePvData.size(); i++) {
            LivePvDatum livePvDatum = livePvData.get(i);

            float x = (float) i;
            float y = (float) livePvDatum.getPowerGeneration();
            powerPointValues.add(new PointValue(x, y));
            maxPointValues.add(new PointValue(x, 1450));

            String xLabel = DateTimeUtils.formatTime(livePvDatum.getHour(), livePvDatum.getMinute());
            AxisValue axisValue = new AxisValue(x);
            axisValue.setLabel(xLabel);
            xAxisValues.add(axisValue);
        }
        List<Line> lines = new ArrayList<>();
        Line powerLine = new Line(powerPointValues)
                .setColor(ChartUtils.COLORS[0])
                .setHasPoints(false)
                .setCubic(true)
                .setFilled(true);
        lines.add(powerLine);
        Line maxLine = new Line(maxPointValues)
                .setColor(Color.LTGRAY)
                .setHasPoints(false);
        lines.add(maxLine);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        Axis xAxis = new Axis()
                .setValues(xAxisValues)
                .setMaxLabelChars(8);
        lineChartData.setAxisXBottom(xAxis);

        Axis yAxis = Axis
                .generateAxisFromRange(0, 1400, 200)
                .setMaxLabelChars(5);
        lineChartData.setAxisYLeft(yAxis);
        lineChartView.setLineChartData(lineChartData);
    }

    private void updateTable(List<LivePvDatum> livePvData) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.table);
        linearLayout.removeAllViews();
        for (LivePvDatum livePvDatum : livePvData) {
            View row = getLayoutInflater().inflate(R.layout.table_row, null);
            ((TextView) row.findViewById(R.id.content1)).setText(DateTimeUtils.formatTime(livePvDatum.getHour(), livePvDatum.getMinute()));
            ((TextView) row.findViewById(R.id.content2)).setText(powerFormat.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.content3)).setText(energyFormat.format(livePvDatum.getEnergyGeneration() / 1000.0));
            linearLayout.addView(row);
        }
    }

    public void updateScreen(boolean refreshData) {
        Log.i(TAG, "Updating screen");
        DateTimeUtils.YearMonthDay yearMonthDay = DateTimeUtils.getYearMonthDay(daysAgo);
        int year = yearMonthDay.year;
        int month = yearMonthDay.month;
        int day = yearMonthDay.day;

        String title = "Live   " + DateTimeUtils.formatDate(year, month, day, true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }

        List<LivePvDatum> livePvData = pvDataOperations.loadLive(year, month, day);

        if (refreshData || livePvData.size() == 0) {
            if (refreshData) {
                Log.i(TAG, "Refreshing live PV data for " + DateTimeUtils.formatDate(year, month, day, true));
            } else {
                Log.i(TAG, "No live PV data for " + DateTimeUtils.formatDate(year, month, day, true));
            }

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateScreen(false);
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.call(getApplicationContext(), year, month, day);
        }
        updateGraph(livePvData);
        updateTable(livePvData);
    }
}
