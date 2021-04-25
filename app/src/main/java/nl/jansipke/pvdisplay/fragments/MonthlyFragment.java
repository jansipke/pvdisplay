package nl.jansipke.pvdisplay.fragments;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.AxisLabelValues;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.database.PvDatabase;
import nl.jansipke.pvdisplay.download.PvDownloader;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class MonthlyFragment extends Fragment {

    private final static String TAG = MonthlyFragment.class.getSimpleName();

    private final static String STATE_KEY_YEAR = "year";

    private static DateTimeUtils.Year picked;

    private View fragmentView;
    private LayoutInflater layoutInflater;

    private PvDatabase pvDatabase;
    private PvDownloader pvDownloader;

    private List<Double> createDifferences(List<MonthlyPvDatum> monthlyPvDataPicked,
                                           List<MonthlyPvDatum> monthlyPvDataComparison) {
        List<Double> differences = new ArrayList<>();
        List<MonthlyPvDatum> comparisonFullYear = createFullYear(new DateTimeUtils.Year(0), monthlyPvDataComparison);
        for (MonthlyPvDatum monthlyPvDatum : monthlyPvDataPicked) {
            final double energyPicked = monthlyPvDatum.getEnergyGenerated();
            final double energyComparison = comparisonFullYear.get(monthlyPvDatum.getMonth() - 1).getEnergyGenerated();
            differences.add(energyPicked - energyComparison);
        }
        return differences;
    }

    private List<MonthlyPvDatum> createFullYear(DateTimeUtils.Year y, List<MonthlyPvDatum> monthPvData) {
        List<MonthlyPvDatum> fullYear = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            fullYear.add(new MonthlyPvDatum(y.year, month, 0));
        }
        for (MonthlyPvDatum monthlyPvDatum : monthPvData) {
            int fullYearIndex = monthlyPvDatum.getMonth() - 1;
            fullYear.set(fullYearIndex, monthlyPvDatum);
        }
        return fullYear;
    }

    private List<MonthlyPvDatum> databaseOrDownload(DateTimeUtils.Year y) {
        List<MonthlyPvDatum> data = pvDatabase.loadMonthly(y);
        if (data.size() == 0) {
            pvDownloader.downloadMonthly(y);
        }
        return data;
    }

    private String getMonthlyComparison() {
        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_monthly_comparison), "year");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            Log.d(TAG, "Loading fragment state");
            picked = new DateTimeUtils.Year(savedInstanceState.getInt(STATE_KEY_YEAR));
        } else {
            picked = DateTimeUtils.Year.getToday();
        }

        pvDatabase = new PvDatabase(getContext());

        pvDownloader = new PvDownloader(getContext());
        pvDownloader.getErrorMessage().observe(this, data -> Toast.makeText(getContext(),data, Toast.LENGTH_LONG).show());
        pvDownloader.getDownloadSuccessCount().observe(this, data -> updateScreen());
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

        Button comparisonButton = fragmentView.findViewById(R.id.comparison_button);
        comparisonButton.setText(getMonthlyComparison());
        comparisonButton.setOnClickListener(view -> {
            String monthlyComparison = getMonthlyComparison();
            switch (monthlyComparison) {
                case "off": monthlyComparison = "year"; break;
                case "year": monthlyComparison = "off"; break;
            }
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            sharedPreferences.edit().putString(getResources().
                    getString(R.string.preferences_key_monthly_comparison), monthlyComparison).apply();
            ((Button) view).setText(monthlyComparison);
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
                pvDownloader.downloadMonthly(picked);
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

    private void updateGraph(List<MonthlyPvDatum> monthlyPvDataPicked,
                             List<MonthlyPvDatum> monthlyPvDataComparison,
                             boolean showComparison) {
        LinearLayout graphLinearLayout = fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ComboLineColumnChartView comboLineColumnChartView = new ComboLineColumnChartView(context);
            graphLinearLayout.addView(comboLineColumnChartView);

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> subcolumnValues;
            float maxEnergyGenerated = 5;
            for (int i = 0; i < monthlyPvDataPicked.size(); i++) {
                MonthlyPvDatum monthlyPvDatum = monthlyPvDataPicked.get(i);
                float y = ((float) monthlyPvDatum.getEnergyGenerated()) / 1000;
                subcolumnValues = new ArrayList<>();
                maxEnergyGenerated = Math.max(maxEnergyGenerated, y);
                subcolumnValues.add(new SubcolumnValue(y, ChartUtils.COLORS[0]));
                columns.add(new Column(subcolumnValues));
            }
            List<Line> lines = new ArrayList<>();
            List<PointValue> lineValues = new ArrayList<>();
            if (showComparison) {
                for (int i = 0; i < monthlyPvDataComparison.size(); i++) {
                    MonthlyPvDatum previousYearMonthlyPvDatum = monthlyPvDataComparison.get(i);
                    float y = ((float) previousYearMonthlyPvDatum.getEnergyGenerated()) / 1000;
                    maxEnergyGenerated = Math.max(maxEnergyGenerated, y);
                    lineValues.add(new PointValue(i, y));
                }
            }
            Line line = new Line(lineValues);
            line.setPointRadius(3);
            line.setHasLines(false);
            lines.add(line);
            ComboLineColumnChartData comboLineColumnChartData = new ComboLineColumnChartData(
                    new ColumnChartData(columns), new LineChartData(lines));

            RecordPvDatum recordPvDatum = pvDatabase.loadRecord();
            double yAxisMax = Math.max(recordPvDatum.getMonthlyEnergyGenerated() / 1000, maxEnergyGenerated);
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
                    -1, axisLabelValues.getView(), monthlyPvDataPicked.size() + 1, 0);
            comboLineColumnChartView.setMaximumViewport(viewport);
            comboLineColumnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<MonthlyPvDatum> monthlyPvData,
                             List<Double> differences,
                             boolean showComparison) {
        LinearLayout linearLayout = fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = monthlyPvData.size() - 1; i >= 0; i--) {
            MonthlyPvDatum monthlyPvDatum = monthlyPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_month, linearLayout, false);
            ((TextView) row.findViewById(R.id.month)).setText(
                    DateTimeUtils.getMonthName(monthlyPvDatum.getMonth()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(monthlyPvDatum.getEnergyGenerated() / 1000.0));

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

    private void updateTitle(DateTimeUtils.Year picked) {
        TextView textView = fragmentView.findViewById(R.id.title);
        textView.setText(picked.toString());
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with monthly PV data");

        List<MonthlyPvDatum> monthlyPvDataPicked = databaseOrDownload(picked);

        if (isAdded() && getActivity() != null) {
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            String monthlyComparison = sharedPreferences.getString(getResources().
                    getString(R.string.preferences_key_monthly_comparison), "year");
            DateTimeUtils.Year comparison;
            boolean showComparison = false;
            List<MonthlyPvDatum> monthlyPvDataComparison = new ArrayList<>();
            List<MonthlyPvDatum> monthlyPvDataComparisonFullYear = new ArrayList<>();
            if ("year".equals(monthlyComparison)) {
                showComparison = true;
                comparison = picked.createCopy(-1, false);
                monthlyPvDataComparison = databaseOrDownload(comparison);
                monthlyPvDataComparisonFullYear = createFullYear(comparison, monthlyPvDataComparison);
            }

            updateTitle(picked);
            updateGraph(
                    createFullYear(picked, monthlyPvDataPicked),
                    monthlyPvDataComparisonFullYear,
                    showComparison);
            updateTable(
                    monthlyPvDataPicked,
                    createDifferences(monthlyPvDataPicked, monthlyPvDataComparison),
                    showComparison);
        }
    }
}
