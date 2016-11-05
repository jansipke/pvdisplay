package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.AxisLabelValues;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class MonthlyFragment extends Fragment {

    private final static String TAG = MonthlyFragment.class.getSimpleName();

    private final static String STATE_KEY_YEAR = "year";

    private static int pickedYear;

    private View fragmentView;
    private LayoutInflater layoutInflater;
    private PvDataOperations pvDataOperations;

    private void callPvDataService() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                if (intent.getBooleanExtra("success", true)) {
                    updateScreen();
                } else {
                    Toast.makeText(context, intent.getStringExtra("message"),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(broadcastReceiver, intentFilter);

        PvDataService.callMonth(getContext(), pickedYear);
    }

    private List<MonthlyPvDatum> createFullYear(int year, List<MonthlyPvDatum> monthPvData) {
        List<MonthlyPvDatum> fullYear = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            fullYear.add(new MonthlyPvDatum(year, month, 0));
        }
        for (MonthlyPvDatum monthlyPvDatum : monthPvData) {
            int fullYearIndex = monthlyPvDatum.getMonth() - 1;
            fullYear.set(fullYearIndex, monthlyPvDatum);
        }
        return fullYear;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        pvDataOperations = new PvDataOperations(getContext());

        if (savedInstanceState != null) {
            Log.d(TAG, "Loading fragment state");
            pickedYear = savedInstanceState.getInt(STATE_KEY_YEAR);
        } else {
            pickedYear = DateTimeUtils.getTodaysYear();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_monthly, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_month, container, false);
        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                pickedYear--;
                updateScreen();
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                pickedYear++;
                updateScreen();
                break;
            case R.id.action_this_year:
                Log.d(TAG, "Clicked this year");
                pickedYear = DateTimeUtils.getTodaysYear();
                updateScreen();
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                callPvDataService();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "Saving fragment state");

        outState.putInt(STATE_KEY_YEAR, pickedYear);
    }

    private void updateGraph(List<MonthlyPvDatum> monthlyPvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ColumnChartView columnChartView = new ColumnChartView(context);
            graphLinearLayout.addView(columnChartView);

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> subcolumnValues;
            for (int i = 0; i < monthlyPvData.size(); i++) {
                MonthlyPvDatum monthlyPvDatum = monthlyPvData.get(i);
                subcolumnValues = new ArrayList<>();
                subcolumnValues.add(new SubcolumnValue(
                        ((float) monthlyPvDatum.getEnergyGenerated()) / 1000,
                        ChartUtils.COLORS[0]));
                columns.add(new Column(subcolumnValues));
            }
            ColumnChartData columnChartData = new ColumnChartData(columns);

            RecordPvDatum recordPvDatum = pvDataOperations.loadRecord();
            AxisLabelValues axisLabelValues = FormatUtils.getAxisLabelValues(
                    recordPvDatum.getMonthlyEnergyGenerated() / 1000);
            Axis yAxis = Axis
                    .generateAxisFromRange(0, axisLabelValues.getMax(), axisLabelValues.getStep())
                    .setMaxLabelChars(6)
                    .setTextColor(Color.GRAY)
                    .setHasLines(true);
            yAxis.setName(getResources().getString(R.string.graph_legend_energy));
            columnChartData.setAxisYLeft(yAxis);

            columnChartView.setColumnChartData(columnChartData);

            columnChartView.setViewportCalculationEnabled(false);
            Viewport viewport = new Viewport(
                    -1, axisLabelValues.getView(), monthlyPvData.size() + 1, 0);
            columnChartView.setMaximumViewport(viewport);
            columnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<MonthlyPvDatum> monthlyPvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = monthlyPvData.size() - 1; i >= 0; i--) {
            MonthlyPvDatum monthlyPvDatum = monthlyPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_month, null);
            ((TextView) row.findViewById(R.id.month)).setText(
                    DateTimeUtils.getMonthName(monthlyPvDatum.getMonth()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(monthlyPvDatum.getEnergyGenerated() / 1000.0));
            linearLayout.addView(row);
        }
    }

    private void updateTitle(int year) {
        TextView textView = (TextView) fragmentView.findViewById(R.id.title);
        textView.setText("" + year);
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen");

        List<MonthlyPvDatum> monthlyPvData = pvDataOperations.loadMonthly(pickedYear);
        if (monthlyPvData.size() == 0) {
            Log.d(TAG, "No monthly PV data for " + pickedYear);
            callPvDataService();
            return;
        }

        if (isAdded() && getActivity() != null) {
            Log.d(TAG, "Updating title, graph and table");
            updateTitle(pickedYear);
            updateGraph(createFullYear(pickedYear, monthlyPvData));
            updateTable(monthlyPvData);
        }
    }
}
