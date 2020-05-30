package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import java.util.Objects;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
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

    private static DateTimeUtils.Year picked;

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
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext()))
                .registerReceiver(broadcastReceiver, intentFilter);

        PvDataService.callMonth(getContext(), picked.year);
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
            picked.year = savedInstanceState.getInt(STATE_KEY_YEAR);
        } else {
            picked = DateTimeUtils.Year.getToday();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_monthly, menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
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
                picked = picked.createCopy(-1, true);
                updateScreen();
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = picked.createCopy(1, false);
                updateScreen();
                break;
            case R.id.action_this_year:
                Log.d(TAG, "Clicked this year");
                picked = DateTimeUtils.Year.getToday();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "Saving fragment state");

        outState.putInt(STATE_KEY_YEAR, picked.year);
    }

    private void updateGraph(List<MonthlyPvDatum> monthlyPvData,
                             List<MonthlyPvDatum> previousYearMonthlyPvData) {
        LinearLayout graphLinearLayout = fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ComboLineColumnChartView comboLineColumnChartView = new ComboLineColumnChartView(context);
            graphLinearLayout.addView(comboLineColumnChartView);

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
            List<Line> lines = new ArrayList<>();
            List<PointValue> lineValues = new ArrayList<>();
            for (int i = 0; i < previousYearMonthlyPvData.size(); i++) {
                MonthlyPvDatum previousYearMonthlyPvDatum = previousYearMonthlyPvData.get(i);
                lineValues.add(new PointValue(i,
                        ((float) previousYearMonthlyPvDatum.getEnergyGenerated()) / 1000));
            }
            Line line = new Line(lineValues);
            line.setPointRadius(3);
            line.setHasLines(false);
            lines.add(line);
            ComboLineColumnChartData comboLineColumnChartData = new ComboLineColumnChartData(
                    new ColumnChartData(columns), new LineChartData(lines));

            RecordPvDatum recordPvDatum = pvDataOperations.loadRecord();
            double yAxisMax = Math.max(recordPvDatum.getMonthlyEnergyGenerated() / 1000, 1.0);
            AxisLabelValues axisLabelValues = FormatUtils.getAxisLabelValues(yAxisMax);
            Axis yAxis = Axis
                    .generateAxisFromRange(0, axisLabelValues.getMax(), axisLabelValues.getStep())
                    .setMaxLabelChars(6)
                    .setTextColor(Color.GRAY)
                    .setHasLines(true);
            yAxis.setName(getResources().getString(R.string.graph_legend_energy));
            comboLineColumnChartData.setAxisYLeft(yAxis);

            comboLineColumnChartView.setComboLineColumnChartData(comboLineColumnChartData);

            comboLineColumnChartView.setViewportCalculationEnabled(false);
            Viewport viewport = new Viewport(
                    -1, axisLabelValues.getView(), monthlyPvData.size() + 1, 0);
            comboLineColumnChartView.setMaximumViewport(viewport);
            comboLineColumnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<MonthlyPvDatum> monthlyPvData) {
        LinearLayout linearLayout = fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = monthlyPvData.size() - 1; i >= 0; i--) {
            MonthlyPvDatum monthlyPvDatum = monthlyPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_month, linearLayout, false);
            ((TextView) row.findViewById(R.id.month)).setText(
                    DateTimeUtils.getMonthName(monthlyPvDatum.getMonth()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(monthlyPvDatum.getEnergyGenerated() / 1000.0));
            linearLayout.addView(row);
        }
    }

    private void updateTitle(int year) {
        TextView textView = fragmentView.findViewById(R.id.title);
        textView.setText(new DateTimeUtils.Year(year).toString());
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with monthly PV data");

        List<MonthlyPvDatum> monthlyPvData = pvDataOperations.loadMonthly(picked);
        List<MonthlyPvDatum> previousYearMonthlyPvData = createFullYear(
                picked.year - 1,
                pvDataOperations.loadMonthly(picked.createCopy(-1, false)));

        if (monthlyPvData.size() == 0) {
            Log.d(TAG, "No monthly PV data for " + picked.year);
            callPvDataService();
        }

        if (isAdded() && getActivity() != null) {
            updateTitle(picked.year);
            updateGraph(createFullYear(picked.year, monthlyPvData), previousYearMonthlyPvData);
            updateTable(monthlyPvData);
        }
    }
}
