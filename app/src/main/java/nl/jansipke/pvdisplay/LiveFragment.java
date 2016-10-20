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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class LiveFragment extends Fragment implements Serializable {

    private final static String TAG = LiveFragment.class.getSimpleName();

    private static DatePickerListener datePickerListener;
    private static DateTimeUtils.YearMonthDay picked;

    private View fragmentView;
    private LayoutInflater layoutInflater;
    private PvDataOperations pvDataOperations;

    public static class DatePickerFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getContext(), datePickerListener,
                    picked.year, picked.month - 1, picked.day);
        }
    }

    private class DatePickerListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            picked.year = year;
            picked.month = month + 1;
            picked.day = day;
            LiveFragment.this.updateScreen(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        datePickerListener = new DatePickerListener();
        picked = DateTimeUtils.getTodaysYearMonthDay();

        pvDataOperations = new PvDataOperations(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_live, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_live, container, false);
        updateScreen(false);
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = DateTimeUtils.addDays(picked, -1);
                updateScreen(false);
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = DateTimeUtils.addDays(picked, 1);
                updateScreen(false);
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                updateScreen(true);
                break;
            case R.id.action_date:
                Log.d(TAG, "Clicked date");
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getFragmentManager(), "datePicker");
                break;
            case R.id.action_today:
                Log.d(TAG, "Clicked today");
                picked = DateTimeUtils.getTodaysYearMonthDay();
                updateScreen(false);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateGraph(List<LivePvDatum> livePvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        LineChartView lineChartView = new LineChartView(getContext());
        graphLinearLayout.addView(lineChartView);

        List<PointValue> powerPointValues = new ArrayList<>();
        List<AxisValue> xAxisValues = new ArrayList<>();
        for (int i = 0; i < livePvData.size(); i++) {
            LivePvDatum livePvDatum = livePvData.get(i);

            float x = (float) i;
            float y = (float) livePvDatum.getPowerGeneration();
            powerPointValues.add(new PointValue(x, y));

            String xLabel = DateTimeUtils.formatTime(
                    livePvDatum.getHour(),
                    livePvDatum.getMinute(),
                    true);
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
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        Axis xAxis = new Axis()
                .setValues(xAxisValues)
                .setMaxLabelChars(8)
                .setTextColor(Color.GRAY);
        lineChartData.setAxisXBottom(xAxis);

        Axis yAxis = Axis
                .generateAxisFromRange(0, 1500, 250) // TODO Use real maximum value
                .setMaxLabelChars(6)
                .setTextColor(Color.GRAY)
                .setHasLines(true);
        yAxis.setName(getResources().getString(R.string.graph_legend_power));
        lineChartData.setAxisYLeft(yAxis);

        lineChartView.setLineChartData(lineChartData);

        lineChartView.setViewportCalculationEnabled(false);
        final Viewport viewport = new Viewport(-1, 1600, livePvData.size(), 0); // TODO Use real maximum value
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);
    }

    private void updateTable(List<LivePvDatum> livePvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = livePvData.size() - 1; i >= 0; i--) {
            LivePvDatum livePvDatum = livePvData.get(i);
            View row = layoutInflater.inflate(R.layout.table_live_row, null);
            ((TextView) row.findViewById(R.id.time)).setText(DateTimeUtils.formatTime(
                    livePvDatum.getHour(),
                    livePvDatum.getMinute(),
                    true));
            ((TextView) row.findViewById(R.id.power)).setText(
                    FormatUtils.POWER_FORMAT.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(livePvDatum.getEnergyGeneration() / 1000.0));
            linearLayout.addView(row);
        }
    }

    public void updateScreen(boolean refreshData) {
        Log.d(TAG, "Updating screen");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                DateTimeUtils.formatDate(picked.year, picked.month, picked.day, true));

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
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callLive(getContext(), picked.year, picked.month, picked.day);
        }
        updateGraph(livePvData);
        updateTable(livePvData);
    }
}
