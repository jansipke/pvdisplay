package nl.jansipke.pvdisplay.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.AxisLabelValues;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.database.PvDatabase;
import nl.jansipke.pvdisplay.download.PvDownloader;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class LiveFragment extends Fragment {

    private final static String TAG = LiveFragment.class.getSimpleName();

    private final static String STATE_KEY_YEAR = "year";
    private final static String STATE_KEY_MONTH = "month";
    private final static String STATE_KEY_DAY = "day";

    private static DatePickerListener datePickerListener;
    private static DateTimeUtils.YearMonthDay picked;

    private View fragmentView;
    private LayoutInflater layoutInflater;

    private PvDatabase pvDatabase;
    private PvDownloader pvDownloader;

    public static class DatePickerFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(Objects.requireNonNull(getContext()), datePickerListener,
                    picked.year, picked.month - 1, picked.day);
        }
    }

    private class DatePickerListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            picked = new DateTimeUtils.YearMonthDay(year, month + 1, day);
            DateTimeUtils.YearMonthDay today = DateTimeUtils.YearMonthDay.getToday();
            if (picked.isLaterThan(today)) {
                picked = today;
            }
            LiveFragment.this.updateScreen();
        }
    }

    private List<Double> createDifferences(List<LivePvDatum> livePvDataPicked, List<LivePvDatum> livePvDataComparison) {
        List<Double> differences = new ArrayList<>();
        List<LivePvDatum> comparisonFullDay = createFullDay(new DateTimeUtils.YearMonthDay(0, 0, 0), 0, 24, livePvDataComparison);
        for (LivePvDatum livePvDatum : livePvDataPicked) {
            int fullDayIndex = livePvDatum.getHour() * 12 + livePvDatum.getMinute() / 5;
            final double energyPicked = livePvDatum.getEnergyGeneration();
            final double energyComparison = comparisonFullDay.get(fullDayIndex).getEnergyGeneration();
            differences.add(energyPicked - energyComparison);
        }
        return differences;
    }

    private List<Double> createDifferences(List<LivePvDatum> livePvDataPicked, double energyGenerationComparison) {
        List<Double> differences = new ArrayList<>();
        for (LivePvDatum livePvDatum : livePvDataPicked) {
            if (energyGenerationComparison > 0) {
                final double energyGenerationPicked = livePvDatum.getEnergyGeneration();
                differences.add(energyGenerationPicked / energyGenerationComparison);
            } else {
                differences.add(0.0);
            }
        }
        return differences;
    }

    private List<LivePvDatum> createFullDay(DateTimeUtils.YearMonthDay date,
                                            int startHour, int endHour,
                                            List<LivePvDatum> livePvData) {
        // Create full day with all zeroes
        List<LivePvDatum> fullDay = new ArrayList<>();
        for (int hour = startHour; hour < endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 5) {
                fullDay.add(new LivePvDatum(date.year, date.month, date.day, hour, minute, 0, 0));
            }
        }
        // Replace with actual values where present
        for (LivePvDatum livePvDatum : livePvData) {
            if (livePvDatum.getHour() >= startHour && livePvDatum.getHour() < endHour) {
                int fullDayIndex = (livePvDatum.getHour() - startHour) * 12 + livePvDatum.getMinute() / 5;
                fullDay.set(fullDayIndex, livePvDatum);
            }
        }
        // Ensure energy generation keeps the same or increases
        double maxEnergyGeneration = 0;
        for (int i = 0; i < fullDay.size(); i++) {
            final LivePvDatum livePvDatum = fullDay.get(i);
            if (livePvDatum.getEnergyGeneration() < maxEnergyGeneration) {
                fullDay.set(i, new LivePvDatum(date.year, date.month, date.day,
                                               livePvDatum.getHour(), livePvDatum.getMinute(),
                                               maxEnergyGeneration, livePvDatum.getPowerGeneration()));
            } else {
                maxEnergyGeneration = livePvDatum.getEnergyGeneration();
            }
        }
        return fullDay;
    }

    private List<LivePvDatum> databaseOrDownload(DateTimeUtils.YearMonthDay ymd) {
        List<LivePvDatum> data = pvDatabase.loadLive(ymd);
        if (data.size() == 0) {
            pvDownloader.downloadLive(ymd);
        }
        if ("avg".equals(getLiveComparison())) {
            if (pvDatabase.loadStatistic() == null) {
                pvDownloader.downloadStatistic();
            }
        }
        return data;
    }

    private String getLiveComparison() {
        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_live_comparison), "day");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            Log.d(TAG, "Loading fragment state");
            picked = new DateTimeUtils.YearMonthDay(
                    savedInstanceState.getInt(STATE_KEY_YEAR),
                    savedInstanceState.getInt(STATE_KEY_MONTH),
                    savedInstanceState.getInt(STATE_KEY_DAY));
        } else {
            picked = DateTimeUtils.YearMonthDay.getToday();
        }

        datePickerListener = new DatePickerListener();

        pvDatabase = new PvDatabase(getContext());

        pvDownloader = new PvDownloader(getContext());
        pvDownloader.getErrorMessage().observe(this, data -> Toast.makeText(getContext(),data, Toast.LENGTH_LONG).show());
        pvDownloader.getDownloadSuccessCount().observe(this, data -> updateScreen());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_live, menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_live, container, false);

        Button comparisonButton = fragmentView.findViewById(R.id.comparison_button);
        comparisonButton.setText(getLiveComparison());
        comparisonButton.setOnClickListener(view -> {
            String liveComparison = getLiveComparison();
            switch (liveComparison) {
                case "off": liveComparison = "day"; break;
                case "day": liveComparison = "year"; break;
                case "year": liveComparison = "avg"; break;
                case "avg": liveComparison = "off"; break;
            }
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            sharedPreferences.edit().putString(getResources().
                    getString(R.string.preferences_key_live_comparison), liveComparison).apply();
            ((Button) view).setText(liveComparison);
            updateScreen();
        });

        updateScreen();
        return fragmentView;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = picked.createCopy(0, 0, -1, true);
                updateScreen();
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = picked.createCopy(0, 0, 1, false);
                updateScreen();
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                pvDownloader.downloadLive(picked);
                switch (getLiveComparison()) {
                    case "day":
                        pvDownloader.downloadLive(picked.createCopy(0, 0, -1, false));
                        break;
                    case "year":
                        pvDownloader.downloadLive(picked.createCopy(-1, 0, 0, false));
                        break;
                }
                break;
            case R.id.action_date:
                Log.d(TAG, "Clicked date");
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getParentFragmentManager(), "datePicker");
                break;
            case R.id.action_today:
                Log.d(TAG, "Clicked today");
                picked = DateTimeUtils.YearMonthDay.getToday();
                updateScreen();
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
        outState.putInt(STATE_KEY_DAY, picked.day);
    }

    private void updateGraph(List<LivePvDatum> livePvDataPicked,
                             List<LivePvDatum> livePvDataComparison,
                             boolean showComparison) {
        LinearLayout graphLinearLayout = fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            LineChartView lineChartView = new LineChartView(context);
            graphLinearLayout.addView(lineChartView);

            List<Line> lines = new ArrayList<>();

            List<PointValue> powerPointValuesPicked = new ArrayList<>();
            float maxPowerGeneration = 5;
            for (int i = 0; i < livePvDataPicked.size(); i++) {
                LivePvDatum livePvDatum = livePvDataPicked.get(i);
                float x = (float) i;
                float y = (float) livePvDatum.getPowerGeneration();
                maxPowerGeneration = Math.max(maxPowerGeneration, y);
                powerPointValuesPicked.add(new PointValue(x, y));
            }
            Line powerLinePicked = new Line(powerPointValuesPicked)
                    .setColor(ChartUtils.COLORS[0])
                    .setHasPoints(false)
                    .setCubic(true)
                    .setFilled(true);

            if (showComparison) {
                List<PointValue> powerPointValuesComparison = new ArrayList<>();
                for (int i = 0; i < livePvDataComparison.size(); i++) {
                    LivePvDatum livePvDatum = livePvDataComparison.get(i);
                    float x = (float) i;
                    float y = (float) livePvDatum.getPowerGeneration();
                    maxPowerGeneration = Math.max(maxPowerGeneration, y);
                    powerPointValuesComparison.add(new PointValue(x, y));
                }
                Line powerLineComparison = new Line(powerPointValuesComparison)
                        .setColor(Color.rgb(223, 223, 223))
                        .setHasPoints(false)
                        .setCubic(true)
                        .setFilled(false);
                lines.add(powerLineComparison);
            }

            lines.add(powerLinePicked);
            LineChartData lineChartData = new LineChartData();
            lineChartData.setLines(lines);

            RecordPvDatum recordPvDatum = pvDatabase.loadRecord();
            assert recordPvDatum != null;
            double yAxisMax = Math.max(recordPvDatum.getLivePowerGeneration(), maxPowerGeneration);
            AxisLabelValues axisLabelValues = FormatUtils.getAxisLabelValues(yAxisMax);
            Axis yAxis = Axis
                    .generateAxisFromRange(0, axisLabelValues.getMax(), axisLabelValues.getStep())
                    .setMaxLabelChars(6)
                    .setTextColor(Color.GRAY)
                    .setHasLines(true);
            yAxis.setName(getResources().getString(R.string.graph_legend_power));
            lineChartData.setAxisYLeft(yAxis);

            lineChartView.setLineChartData(lineChartData);

            lineChartView.setViewportCalculationEnabled(false);
            Viewport viewport = new Viewport(-1, axisLabelValues.getView(), livePvDataPicked.size(), 0);
            lineChartView.setMaximumViewport(viewport);
            lineChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<LivePvDatum> livePvData,
                             List<Double> differences,
                             boolean showComparison,
                             boolean showPercentage) {
        LinearLayout linearLayout = fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        LivePvDatum livePvDatum;
        View row;
        for (int i = livePvData.size() - 1; i >= 0; i--) {
            livePvDatum = livePvData.get(i);
            row = layoutInflater.inflate(R.layout.row_live, linearLayout, false);
            ((TextView) row.findViewById(R.id.time)).setText(new DateTimeUtils.HourMinute(
                    livePvDatum.getHour(), livePvDatum.getMinute()).asString(true));
            ((TextView) row.findViewById(R.id.power)).setText(
                    FormatUtils.POWER_FORMAT.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(livePvDatum.getEnergyGeneration() / 1000.0));

            if (showComparison) {
                if (showPercentage) {
                    double difference = differences.get(i);
                    String differenceText = FormatUtils.PERCENTAGE_FORMAT.format(difference);
                    int differenceColor = Color.rgb(61, 61, 61);
                    if (difference < 1) {
                        differenceColor = Color.rgb(153, 61, 61);
                    }
                    if (difference > 1) {
                        differenceColor = Color.rgb(61, 153, 61);
                    }
                    ((TextView) row.findViewById(R.id.comparison)).setText(differenceText);
                    ((TextView) row.findViewById(R.id.comparison)).setTextColor(differenceColor);
                } else {
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
            }

            linearLayout.addView(row);
        }
    }

    private void updateTitle(DateTimeUtils.YearMonthDay picked) {
        String title = picked.getDayOfWeek() + "  " + picked.asString(true);
        TextView textView = fragmentView.findViewById(R.id.title);
        textView.setText(title);
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with live PV data");

        List<LivePvDatum> livePvDataPicked = databaseOrDownload(picked);

        if (isAdded() && getActivity() != null) {
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            int startHour;
            try {
                startHour = Integer.parseInt(sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_graph_hour_start), "0"));
            } catch (Exception e) {
                startHour = 0;
            }
            int endHour;
            try {
                endHour = Integer.parseInt(sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_graph_hour_end), "24"));
            } catch (Exception e) {
                endHour = 24;
            }

            DateTimeUtils.YearMonthDay comparison;
            boolean showComparison = false;
            boolean showPercentage = false;
            List<LivePvDatum> livePvDataComparison;
            List<LivePvDatum> livePvDataComparisonFullDay = new ArrayList<>();
            List<Double> differences = new ArrayList<>();
            switch (getLiveComparison()) {
                case "day":
                    showComparison = true;
                    comparison = picked.createCopy(0, 0, -1, false);
                    livePvDataComparison = databaseOrDownload(comparison);
                    livePvDataComparisonFullDay = createFullDay(comparison, startHour, endHour, livePvDataComparison);
                    differences = createDifferences(livePvDataPicked, livePvDataComparison);
                    break;
                case "year":
                    showComparison = true;
                    comparison = picked.createCopy(-1,0,0, false);
                    livePvDataComparison = databaseOrDownload(comparison);
                    livePvDataComparisonFullDay = createFullDay(comparison, startHour, endHour, livePvDataComparison);
                    differences = createDifferences(livePvDataPicked, livePvDataComparison);
                    break;
                case "avg":
                    final StatisticPvDatum statisticPvDatum = pvDatabase.loadStatistic();
                    if (statisticPvDatum != null) {
                        showComparison = true;
                        showPercentage = true;
                        double averageGeneration = statisticPvDatum.getAverageGeneration();
                        differences = createDifferences(livePvDataPicked, averageGeneration);
                    }
                    break;
            }

            updateTitle(picked);
            updateGraph(
                    createFullDay(picked, startHour, endHour, livePvDataPicked),
                    livePvDataComparisonFullDay,
                    showComparison);
            updateTable(
                    livePvDataPicked,
                    differences,
                    showComparison,
                    showPercentage);
        }
    }
}
