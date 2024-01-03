package nl.jansipke.pvdisplay.fragments;

import static nl.jansipke.pvdisplay.R.id.graph;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.AxisLabelValues;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.data.YearlyPvDatum;
import nl.jansipke.pvdisplay.database.PvDatabase;
import nl.jansipke.pvdisplay.download.PvDownloader;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

public class YearlyFragment extends Fragment {

    private final static String TAG = YearlyFragment.class.getSimpleName();

    private View fragmentView;
    private LayoutInflater layoutInflater;

    private PvDatabase pvDatabase;
    private PvDownloader pvDownloader;

    private List<YearlyPvDatum> databaseOrDownload() {
        List<YearlyPvDatum> data = pvDatabase.loadYearly();
        if (data.size() == 0) {
            pvDownloader.downloadYearly();
        }
        return data;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        pvDatabase = new PvDatabase(getContext());

        pvDownloader = new PvDownloader(getContext());
//        pvDownloader.getErrorMessage().observe(this, data -> Toast.makeText(getContext(),data, Toast.LENGTH_LONG).show());
        pvDownloader.getDownloadSuccessCount().observe(this, data -> updateScreen());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_yearly, menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_year, container, false);
        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Log.d(TAG, "Clicked refresh");
            pvDownloader.downloadYearly();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateGraph(List<YearlyPvDatum> yearlyPvData) {
        LinearLayout graphLinearLayout = fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ColumnChartView columnChartView = new ColumnChartView(context);
            graphLinearLayout.addView(columnChartView);

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> subcolumnValues;
            float maxEnergyGenerated = 5;
            for (int i = 0; i < yearlyPvData.size(); i++) {
                YearlyPvDatum yearlyPvDatum = yearlyPvData.get(i);
                float y = ((float) yearlyPvDatum.getEnergyGenerated()) / 1000;
                maxEnergyGenerated = Math.max(maxEnergyGenerated, y);
                subcolumnValues = new ArrayList<>();
                subcolumnValues.add(new SubcolumnValue(y, ChartUtils.COLORS[0]));
                columns.add(new Column(subcolumnValues));
            }
            ColumnChartData columnChartData = new ColumnChartData(columns);

            RecordPvDatum recordPvDatum = pvDatabase.loadRecord();
            double yAxisMax = Math.max(recordPvDatum.getYearlyEnergyGenerated() / 1000, maxEnergyGenerated);
            AxisLabelValues axisLabelValues = FormatUtils.getAxisLabelValues(yAxisMax);
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
                    -1, axisLabelValues.getView(), yearlyPvData.size() + 1, 0);
            columnChartView.setMaximumViewport(viewport);
            columnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<YearlyPvDatum> yearlyPvData) {
        LinearLayout linearLayout = fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = yearlyPvData.size() - 1; i >= 0; i--) {
            YearlyPvDatum yearlyPvDatum = yearlyPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_year, linearLayout, false);
            ((TextView) row.findViewById(R.id.year)).setText(
                    new DateTimeUtils.Year(yearlyPvDatum.getYear()).toString());
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(yearlyPvDatum.getEnergyGenerated() / 1000));
            linearLayout.addView(row);
        }
    }

    private void updateTitle() {
        TextView textView = fragmentView.findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.title_yearly));
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with yearly PV data");

        List<YearlyPvDatum> yearlyPvData = databaseOrDownload();

        if (isAdded() && getActivity() != null) {
            updateTitle();
            updateGraph(yearlyPvData);
            updateTable(yearlyPvData);
        }
    }
}
