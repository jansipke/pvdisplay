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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext()))
                .registerReceiver(broadcastReceiver, intentFilter);

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        String dailyComparison = sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_daily_comparison), "year");
        DateTimeUtils.YearMonth comparison;
        if ("year".equals(dailyComparison)) {
            comparison = picked.createCopy(-1, 0, false);
            PvDataService.callDay(getContext(), comparison.year, comparison.month);
        }

        PvDataService.callDay(getContext(), picked.year, picked.month);
    }

    private List<Double> createDifferences(List<DailyPvDatum> dailyPvDataPicked,
                                           List<DailyPvDatum> dailyPvDataComparison) {
        List<Double> differences = new ArrayList<>();
        List<DailyPvDatum> comparisonFullMonth = createFullMonth(new DateTimeUtils.YearMonth(0, 0), dailyPvDataComparison);
        for (DailyPvDatum dailyPvDatum : dailyPvDataPicked) {
            final double energyPicked = dailyPvDatum.getEnergyGenerated();
            final double energyComparison = comparisonFullMonth.get(dailyPvDatum.getDay() - 1).getEnergyGenerated();
            differences.add(energyPicked - energyComparison);
        }
        return differences;
    }

    private List<DailyPvDatum> createFullMonth(DateTimeUtils.YearMonth ym,
                                               List<DailyPvDatum> dayPvData) {
        List<DailyPvDatum> fullMonth = new ArrayList<>();
        int lastDayOfMonth = ym.getLastDayOfMonth();
        for (int day = 1; day <= lastDayOfMonth; day++) {
            fullMonth.add(new DailyPvDatum(ym.year, ym.month, day, 0, 0, ""));
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
                    return ContextCompat.getDrawable(activity, R.drawable.weather_cloudy);
                case "Fine":
                    return ContextCompat.getDrawable(activity, R.drawable.weather_fine);
                case "Mostly Cloudy":
                    return ContextCompat.getDrawable(activity, R.drawable.weather_mostly_cloudy);
                case "Partly Cloudy":
                    return ContextCompat.getDrawable(activity, R.drawable.weather_partly_cloudy);
                case "Showers":
                    return ContextCompat.getDrawable(activity, R.drawable.weather_showers);
                case "Snow":
                    return ContextCompat.getDrawable(activity, R.drawable.weather_snow);
                default:
                    return ContextCompat.getDrawable(activity, R.drawable.help);
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
            picked = new DateTimeUtils.YearMonth(
                    savedInstanceState.getInt(STATE_KEY_YEAR),
                    savedInstanceState.getInt(STATE_KEY_MONTH));
        } else {
            picked = DateTimeUtils.YearMonth.getToday();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_daily, menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_day, container, false);

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        String dailyComparison = sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_daily_comparison), "year");

        Button comparisonButton = fragmentView.findViewById(R.id.comparison_button);
        comparisonButton.setText(dailyComparison);
        comparisonButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final SharedPreferences sharedPreferences = PreferenceManager.
                        getDefaultSharedPreferences(getContext());
                String dailyComparison = sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_daily_comparison), "year");
                switch (dailyComparison) {
                    case "off": dailyComparison = "year"; break;
                    case "year": dailyComparison = "off"; break;
                }
                sharedPreferences.edit().putString(getResources().
                        getString(R.string.preferences_key_daily_comparison), dailyComparison).apply();
                ((Button) view).setText(dailyComparison);
                updateScreen();
            }
        });

        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = picked.createCopy(0, -1, true);
                updateScreen();
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = picked.createCopy(0, 1, false);
                updateScreen();
                break;
            case R.id.action_this_month:
                Log.d(TAG, "Clicked this month");
                picked = DateTimeUtils.YearMonth.getToday();
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
        outState.putInt(STATE_KEY_MONTH, picked.month);
    }

    private void updateGraph(List<DailyPvDatum> dailyPvDataPicked,
                             List<DailyPvDatum> dailyPvDataComparison,
                             boolean showComparison) {
        LinearLayout graphLinearLayout = fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ComboLineColumnChartView comboLineColumnChartView = new ComboLineColumnChartView(context);
            graphLinearLayout.addView(comboLineColumnChartView);

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> subcolumnValues;
            for (int i = 0; i < dailyPvDataPicked.size(); i++) {
                DailyPvDatum dailyPvDatum = dailyPvDataPicked.get(i);
                subcolumnValues = new ArrayList<>();
                subcolumnValues.add(new SubcolumnValue(
                        ((float) dailyPvDatum.getEnergyGenerated()) / 1000,
                        ChartUtils.COLORS[0]));
                columns.add(new Column(subcolumnValues));
            }

            List<Line> lines = new ArrayList<>();
            List<PointValue> lineValues = new ArrayList<>();
            if (showComparison) {
                for (int i = 0; i < dailyPvDataComparison.size(); i++) {
                    DailyPvDatum dailyPvDatum = dailyPvDataComparison.get(i);
                    lineValues.add(new PointValue(i,
                            ((float) dailyPvDatum.getEnergyGenerated()) / 1000));
                }
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
                    -1, axisLabelValues.getView(), dailyPvDataPicked.size() + 1, 0);
            comboLineColumnChartView.setMaximumViewport(viewport);
            comboLineColumnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<DailyPvDatum> dailyPvData,
                             List<Double> differences,
                             boolean showComparison) {
        LinearLayout linearLayout = fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = dailyPvData.size() - 1; i >= 0; i--) {
            DailyPvDatum dailyPvDatum = dailyPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_day, linearLayout, false);
            final String dayOfWeek = new DateTimeUtils.YearMonthDay(
                    dailyPvDatum.getYear(),
                    dailyPvDatum.getMonth(),
                    dailyPvDatum.getDay()).getDayOfWeek();
            final String day = dayOfWeek + " " + dailyPvDatum.getDay();
            ((TextView) row.findViewById(R.id.day)).setText(day);
            final Drawable drawable = getDrawable(dailyPvDatum.getCondition());
            if (drawable != null) {
                ((ImageView) row.findViewById(R.id.condition)).setImageDrawable(
                        drawable);
            }
            ((TextView) row.findViewById(R.id.peak)).setText(
                    FormatUtils.POWER_FORMAT.format(dailyPvDatum.getPeakPower()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(dailyPvDatum.getEnergyGenerated() / 1000.0));

            if (showComparison) {
                double difference = differences.get(i) / 1000.0;
                String differenceText = "0.000";
                int differenceColor = Color.rgb(61, 61, 61);
                if (difference < 0) {
                    differenceText = FormatUtils.ENERGY_FORMAT.format(difference);
                    differenceColor = Color.rgb(153, 61, 61);
                }
                if (difference > 0) {
                    differenceText = "+" + FormatUtils.ENERGY_FORMAT.format(difference);
                    differenceColor = Color.rgb(61, 153, 61);
                }
                ((TextView) row.findViewById(R.id.comparison)).setText(differenceText);
                ((TextView) row.findViewById(R.id.comparison)).setTextColor(differenceColor);
            }

            linearLayout.addView(row);
        }
    }

    private void updateTitle(DateTimeUtils.YearMonth picked) {
        TextView textView = fragmentView.findViewById(R.id.title);
        textView.setText(picked.asString(true));
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with daily PV data");

        List<DailyPvDatum> dailyPvDataPicked = pvDataOperations.loadDaily(picked);
        if (dailyPvDataPicked.size() == 0) {
            Log.d(TAG, "No daily PV data for " + picked);
            callPvDataService();
        }

        if (isAdded() && getActivity() != null) {
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            String dailyComparison = sharedPreferences.getString(getResources().
                    getString(R.string.preferences_key_daily_comparison), "year");
            DateTimeUtils.YearMonth comparison;
            boolean showComparison = false;
            List<DailyPvDatum> dailyPvDataComparison = new ArrayList<>();
            if ("year".equals(dailyComparison)) {
                showComparison = true;
                comparison = picked.createCopy(-1, 0, false);
                dailyPvDataComparison = pvDataOperations.loadDaily(comparison);
                if (dailyPvDataComparison.size() == 0) {
                    Log.d(TAG, "No daily PV data for " + comparison);
                    callPvDataService();
                }
            }

            updateTitle(picked);
            updateGraph(
                    createFullMonth(picked, dailyPvDataPicked),
                    dailyPvDataComparison,
                    showComparison);
            updateTable(
                    dailyPvDataPicked,
                    createDifferences(dailyPvDataPicked, dailyPvDataComparison),
                    showComparison);
        }
    }
}
