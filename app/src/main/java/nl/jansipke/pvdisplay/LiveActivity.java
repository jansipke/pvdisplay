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

public class LiveActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static class DatePickerFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)
                    getActivity(), picked.year, picked.month - 1, picked.day);
        }
    }

    private final static String TAG = LiveActivity.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private static DateTimeUtils.YearMonthDay picked;

    private PvDataOperations pvDataOperations;

    public void clickedOnOldest(View view) {
        // TODO Implement
    }

    public void clickedOnPrevious(View view) {
        picked = DateTimeUtils.addDays(picked, -1);
        updateScreen(false);
    }

    public void clickedOnNext(View view) {
        picked = DateTimeUtils.addDays(picked, 1);
        updateScreen(false);
    }

    public void clickedOnNewest(View view) {
        picked = DateTimeUtils.getToday();
        updateScreen(false);
    }

    public void clickedOnDate(View view) {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
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
        setContentView(R.layout.activity_live);

        picked = DateTimeUtils.getToday();
        pvDataOperations = new PvDataOperations(getApplicationContext());

        updateScreen(false);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        picked.year = year;
        picked.month = month + 1;
        picked.day = day;
        updateScreen(false);
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

            String xLabel = DateTimeUtils.formatTime(
                    livePvDatum.getHour(), livePvDatum.getMinute());
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
            ((TextView) row.findViewById(R.id.content1)).setText(
                    DateTimeUtils.formatTime(livePvDatum.getHour(), livePvDatum.getMinute()));
            ((TextView) row.findViewById(R.id.content2)).setText(
                    powerFormat.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.content3)).setText(
                    energyFormat.format(livePvDatum.getEnergyGeneration() / 1000.0));
            linearLayout.addView(row);
        }
    }

    public void updateScreen(boolean refreshData) {
        Log.i(TAG, "Updating screen");

        String title = "Live   " + DateTimeUtils.formatDate(
                picked.year, picked.month, picked.day, true);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }

        List<LivePvDatum> livePvData = pvDataOperations.loadLive(
                picked.year, picked.month, picked.day);

        if (refreshData || livePvData.size() == 0) {
            if (refreshData) {
                Log.i(TAG, "Refreshing live PV data for " + DateTimeUtils.formatDate(
                        picked.year, picked.month, picked.day, true));
            } else {
                Log.i(TAG, "No live PV data for " + DateTimeUtils.formatDate(
                        picked.year, picked.month, picked.day, true));
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

            PvDataService.callLive(getApplicationContext(), picked.year, picked.month, picked.day);
        }
        updateGraph(livePvData);
        updateTable(livePvData);
    }
}
