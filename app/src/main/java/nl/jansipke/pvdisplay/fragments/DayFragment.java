package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class DayFragment extends Fragment {

    private final static String TAG = DayFragment.class.getSimpleName();

    private static DateTimeUtils.YearMonth picked;

    private View fragmentView;
    private LayoutInflater layoutInflater;
    private PvDataOperations pvDataOperations;

    private Drawable getDrawable(String condition) {
        if (condition.equals("Cloudy")) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_cloudy);
        } else if (condition.equals("Fine")) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_fine);
        } else if (condition.equals("Mostly Cloudy")) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_mostly_cloudy);
        } else if (condition.equals("Partly Cloudy")) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_partly_cloudy);
        } else if (condition.equals("Showers")) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_showers);
        } else if (condition.equals("Snow")) {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_snow);
        } else {
            return ContextCompat.getDrawable(getActivity(), R.drawable.ic_help_black_48dp);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        picked = DateTimeUtils.getTodaysYearMonth();

        pvDataOperations = new PvDataOperations(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_day, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_day, container, false);
        updateScreen(false);
        return fragmentView;
    }

    public void onFragmentSelected() {
        Log.d(TAG, "Fragment selected");
        setTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = DateTimeUtils.addMonths(picked, -1);
                updateScreen(false);
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = DateTimeUtils.addMonths(picked, 1);
                updateScreen(false);
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                updateScreen(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                DateTimeUtils.formatDate(picked.year, picked.month, true));
    }

    private void updateGraph(List<HistoricalPvDatum> historicalPvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        ColumnChartView columnChartView = new ColumnChartView(getContext());
        graphLinearLayout.addView(columnChartView);

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
                .setMaxLabelChars(10)
                .setTextColor(Color.GRAY);
        columnChartData.setAxisXBottom(xAxis);

        Axis yAxis = Axis
                .generateAxisFromRange(0, 10000, 1000) // TODO Use real maximum value
                .setMaxLabelChars(6)
                .setTextColor(Color.GRAY);
        yAxis.setName(getResources().getString(R.string.graph_legend_energy));
        columnChartData.setAxisYLeft(yAxis);

        columnChartView.setColumnChartData(columnChartData);

        columnChartView.setViewportCalculationEnabled(false);
        final Viewport viewport = new Viewport(-1, 11000, newestDay, 0);
        columnChartView.setMaximumViewport(viewport);
        columnChartView.setCurrentViewport(viewport);
    }

    private void updateTable(List<HistoricalPvDatum> historicalPvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = historicalPvData.size() - 1; i >= 0; i--) {
            HistoricalPvDatum historicalPvDatum = historicalPvData.get(i);
            View row = layoutInflater.inflate(R.layout.table_day_row, null);
            ((TextView) row.findViewById(R.id.date)).setText(DateTimeUtils.formatDate(
                    historicalPvDatum.getYear(),
                    historicalPvDatum.getMonth(),
                    historicalPvDatum.getDay(),
                    true));
            ((ImageView) row.findViewById(R.id.condition)).setImageDrawable(
                    getDrawable(historicalPvDatum.getCondition()));
            ((TextView) row.findViewById(R.id.peak)).setText(
                    FormatUtils.POWER_FORMAT.format(historicalPvDatum.getPeakPower()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(historicalPvDatum.getEnergyGenerated() / 1000.0));
            linearLayout.addView(row);
        }
    }

    public void updateScreen(boolean refreshData) {
        Log.d(TAG, "Updating screen");

        setTitle();

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
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callHistorical(getContext(),
                    picked.year, picked.month, 1, picked.year, picked.month, 31);
        }
        updateGraph(historicalPvData);
        updateTable(historicalPvData);
    }
}
