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
import android.support.v7.app.ActionBar;
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

    private List<HistoricalPvDatum> createFullMonth(int year, int month,
                                            List<HistoricalPvDatum> historicalPvData) {
        List<HistoricalPvDatum> fullMonth = new ArrayList<>();
        for (int day = 1; day <= DateTimeUtils.getLastDayOfMonth(year, month); day++) {
            fullMonth.add(new HistoricalPvDatum(year, month, day, 0, 0, ""));
        }
        for (HistoricalPvDatum historicalPvDatum : historicalPvData) {
            int fullMonthIndex = historicalPvDatum.getDay() - 1;
            fullMonth.set(fullMonthIndex, historicalPvDatum);
        }
        return fullMonth;
    }

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
                setTitle();
                updateScreen(false);
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = DateTimeUtils.addMonths(picked, 1);
                setTitle();
                updateScreen(false);
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                setTitle();
                updateScreen(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
        if (appCompatActivity != null) {
            ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setTitle(
                        DateTimeUtils.formatYearMonth(picked.year, picked.month, true));
            }
        }
    }

    private void updateGraph(List<HistoricalPvDatum> historicalPvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        ColumnChartView columnChartView = new ColumnChartView(getContext());
        graphLinearLayout.addView(columnChartView);

        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> subcolumnValues;
        int newestDay = 0;
        for (int i = 0; i < historicalPvData.size(); i++) {
            HistoricalPvDatum historicalPvDatum = historicalPvData.get(i);
            subcolumnValues = new ArrayList<>();
            subcolumnValues.add(new SubcolumnValue(
                    (float) historicalPvDatum.getEnergyGenerated(),
                    ChartUtils.COLORS[0]));
            columns.add(new Column(subcolumnValues));

            if (historicalPvDatum.getDay() > newestDay) {
                newestDay = historicalPvDatum.getDay();
            }
        }
        ColumnChartData columnChartData = new ColumnChartData(columns);

        Axis yAxis = Axis
                .generateAxisFromRange(0, 10000, 1000) // TODO Use real maximum value
                .setMaxLabelChars(6)
                .setTextColor(Color.GRAY)
                .setHasLines(true);
        yAxis.setName(getResources().getString(R.string.graph_legend_energy));
        columnChartData.setAxisYLeft(yAxis);

        columnChartView.setColumnChartData(columnChartData);

        columnChartView.setViewportCalculationEnabled(false);
        final Viewport viewport = new Viewport(-1, 10700, newestDay, 0);  // TODO Use real maximum value
        columnChartView.setMaximumViewport(viewport);
        columnChartView.setCurrentViewport(viewport);
    }

    private void updateTable(List<HistoricalPvDatum> historicalPvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = historicalPvData.size() - 1; i >= 0; i--) {
            HistoricalPvDatum historicalPvDatum = historicalPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_day, null);
            ((TextView) row.findViewById(R.id.date)).setText(
                    DateTimeUtils.getDayOfWeek(
                            historicalPvDatum.getYear(),
                            historicalPvDatum.getMonth(),
                            historicalPvDatum.getDay()) + " " +
                    historicalPvDatum.getDay());
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

        updateGraph(createFullMonth(picked.year, picked.month, historicalPvData));
        updateTable(historicalPvData);
    }
}
