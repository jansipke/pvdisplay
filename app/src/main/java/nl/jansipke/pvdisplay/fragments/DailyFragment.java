package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
import nl.jansipke.pvdisplay.data.DailyPvDatum;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class DailyFragment extends Fragment {

    private final static String TAG = DailyFragment.class.getSimpleName();

    private final static String STATE_KEY_YEAR = "year";
    private final static String STATE_KEY_MONTH = "month";

    private static DateTimeUtils.YearMonth picked;

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

        PvDataService.callDay(getContext(), picked.year, picked.month);
    }

    private List<DailyPvDatum> createFullMonth(int year, int month,
                                               List<DailyPvDatum> dayPvData) {
        List<DailyPvDatum> fullMonth = new ArrayList<>();
        for (int day = 1; day <= DateTimeUtils.getLastDayOfMonth(year, month); day++) {
            fullMonth.add(new DailyPvDatum(year, month, day, 0, 0, ""));
        }
        for (DailyPvDatum dailyPvDatum : dayPvData) {
            int fullMonthIndex = dailyPvDatum.getDay() - 1;
            fullMonth.set(fullMonthIndex, dailyPvDatum);
        }
        return fullMonth;
    }

    private Drawable getDrawable(String condition) {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            switch (condition) {
                case "Cloudy":
                    return ContextCompat.getDrawable(activity, R.drawable.ic_cloudy);
                case "Fine":
                    return ContextCompat.getDrawable(activity, R.drawable.ic_fine);
                case "Mostly Cloudy":
                    return ContextCompat.getDrawable(activity, R.drawable.ic_mostly_cloudy);
                case "Partly Cloudy":
                    return ContextCompat.getDrawable(activity, R.drawable.ic_partly_cloudy);
                case "Showers":
                    return ContextCompat.getDrawable(activity, R.drawable.ic_showers);
                case "Snow":
                    return ContextCompat.getDrawable(activity, R.drawable.ic_snow);
                default:
                    return ContextCompat.getDrawable(activity, R.drawable.ic_help_black_48dp);
            }
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        pvDataOperations = new PvDataOperations(getContext());

        if (savedInstanceState != null) {
            Log.d(TAG, "Loading fragment state");
            picked = new DateTimeUtils.YearMonth();
            picked.year = savedInstanceState.getInt(STATE_KEY_YEAR);
            picked.month = savedInstanceState.getInt(STATE_KEY_MONTH);
        } else {
            picked = DateTimeUtils.getTodaysYearMonth();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_daily, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_day, container, false);
        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = DateTimeUtils.addMonths(picked, -1, true);
                updateScreen();
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = DateTimeUtils.addMonths(picked, 1, false);
                updateScreen();
                break;
            case R.id.action_this_month:
                Log.d(TAG, "Clicked this month");
                picked = DateTimeUtils.getTodaysYearMonth();
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

        outState.putInt(STATE_KEY_YEAR, picked.year);
        outState.putInt(STATE_KEY_MONTH, picked.month);
    }

    private void updateGraph(List<DailyPvDatum> dailyPvData,
                             List<DailyPvDatum> previousYearDailyPvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ComboLineColumnChartView comboLineColumnChartView = new ComboLineColumnChartView(context);
            graphLinearLayout.addView(comboLineColumnChartView);

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> subcolumnValues;
            for (int i = 0; i < dailyPvData.size(); i++) {
                DailyPvDatum dailyPvDatum = dailyPvData.get(i);
                subcolumnValues = new ArrayList<>();
                subcolumnValues.add(new SubcolumnValue(
                        ((float) dailyPvDatum.getEnergyGenerated()) / 1000,
                        ChartUtils.COLORS[0]));
                columns.add(new Column(subcolumnValues));
            }
            List<Line> lines = new ArrayList<>();
            List<PointValue> lineValues = new ArrayList<>();
            for (int i = 0; i < previousYearDailyPvData.size(); i++) {
                DailyPvDatum previousYearDailyPvDatum = previousYearDailyPvData.get(i);
                lineValues.add(new PointValue(i,
                        ((float) previousYearDailyPvDatum.getEnergyGenerated()) / 1000));
            }
            Line line = new Line(lineValues);
            line.setPointRadius(3);
            line.setHasLines(false);
            lines.add(line);
            ComboLineColumnChartData comboLineColumnChartData = new ComboLineColumnChartData(
                    new ColumnChartData(columns), new LineChartData(lines));

            RecordPvDatum recordPvDatum = pvDataOperations.loadRecord();
            double yAxisMax = Math.max(recordPvDatum.getDailyEnergyGenerated() / 1000, 1.0);
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
                    -1, axisLabelValues.getView(), dailyPvData.size() + 1, 0);
            comboLineColumnChartView.setMaximumViewport(viewport);
            comboLineColumnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<DailyPvDatum> dailyPvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = dailyPvData.size() - 1; i >= 0; i--) {
            DailyPvDatum dailyPvDatum = dailyPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_day, null);
            ((TextView) row.findViewById(R.id.day)).setText(
                    DateTimeUtils.getDayOfWeek(
                            dailyPvDatum.getYear(),
                            dailyPvDatum.getMonth(),
                            dailyPvDatum.getDay()) + " " +
                    dailyPvDatum.getDay());
            final Drawable drawable = getDrawable(dailyPvDatum.getCondition());
            if (drawable != null) {
                ((ImageView) row.findViewById(R.id.condition)).setImageDrawable(
                        drawable);
            }
            ((TextView) row.findViewById(R.id.peak)).setText(
                    FormatUtils.POWER_FORMAT.format(dailyPvDatum.getPeakPower()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(dailyPvDatum.getEnergyGenerated() / 1000.0));
            linearLayout.addView(row);
        }
    }

    private void updateTitle(int year, int month) {
        TextView textView = (TextView) fragmentView.findViewById(R.id.title);
        textView.setText(DateTimeUtils.formatYearMonth(year, month, true));
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with daily PV data");

        List<DailyPvDatum> dailyPvData = pvDataOperations.loadDaily(picked.year, picked.month);
        List<DailyPvDatum> previousYearDailyPvData = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        boolean showPrevious = sharedPreferences.getBoolean(getResources().
                getString(R.string.preferences_key_show_previous), true);
        if (showPrevious) {
            previousYearDailyPvData = createFullMonth(
                    picked.year - 1,
                    picked.month,
                    pvDataOperations.loadDaily(picked.year - 1, picked.month));
        }

        if (dailyPvData.size() == 0) {
            Log.d(TAG, "No daily PV data for " +
                    DateTimeUtils.formatYearMonth(picked.year, picked.month, true));
            callPvDataService();
        }

        if (isAdded() && getActivity() != null) {
            updateTitle(picked.year, picked.month);
            updateGraph(createFullMonth(picked.year, picked.month, dailyPvData),
                    previousYearDailyPvData);
            updateTable(dailyPvData);
        }
    }
}
