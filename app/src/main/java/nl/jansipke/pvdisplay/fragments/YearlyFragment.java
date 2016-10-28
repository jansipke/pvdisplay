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
import nl.jansipke.pvdisplay.data.YearlyPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class YearlyFragment extends Fragment {

    private final static String TAG = YearlyFragment.class.getSimpleName();

    private View fragmentView;
    private LayoutInflater layoutInflater;
    private PvDataOperations pvDataOperations;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        pvDataOperations = new PvDataOperations(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_year, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_year, container, false);
        updateScreen(false);
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                updateScreen(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateGraph(List<YearlyPvDatum> yearPvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            ColumnChartView columnChartView = new ColumnChartView(context);
            graphLinearLayout.addView(columnChartView);

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> subcolumnValues;
            for (int i = 0; i < yearPvData.size(); i++) {
                YearlyPvDatum yearlyPvDatum = yearPvData.get(i);
                subcolumnValues = new ArrayList<>();
                subcolumnValues.add(new SubcolumnValue(
                        ((float) yearlyPvDatum.getEnergyGenerated()) / 1000,
                        ChartUtils.COLORS[0]));
                columns.add(new Column(subcolumnValues));
            }
            ColumnChartData columnChartData = new ColumnChartData(columns);

            Axis yAxis = Axis
                    .generateAxisFromRange(0, 1500, 250) // TODO Use real maximum value
                    .setMaxLabelChars(6)
                    .setTextColor(Color.GRAY)
                    .setHasLines(true);
            yAxis.setName(getResources().getString(R.string.graph_legend_energy));
            columnChartData.setAxisYLeft(yAxis);

            columnChartView.setColumnChartData(columnChartData);

            columnChartView.setViewportCalculationEnabled(false);
            final Viewport viewport = new Viewport(-1, 1650, yearPvData.size() + 1, 0);  // TODO Use real maximum value
            columnChartView.setMaximumViewport(viewport);
            columnChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<YearlyPvDatum> yearPvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = yearPvData.size() - 1; i >= 0; i--) {
            YearlyPvDatum yearlyPvDatum = yearPvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_year, null);
            ((TextView) row.findViewById(R.id.year)).setText(
                    DateTimeUtils.formatYear(yearlyPvDatum.getYear()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(yearlyPvDatum.getEnergyGenerated() / 1000.0));
            linearLayout.addView(row);
        }
    }

    private void updateTitle() {
        TextView textView = (TextView) fragmentView.findViewById(R.id.title);
        textView.setText(getResources().getString(R.string.title_yearly));
    }

    public void updateScreen(boolean refreshData) {
        Log.d(TAG, "Updating screen");

        List<YearlyPvDatum> yearPvData = pvDataOperations.loadYearly();

        if (refreshData || yearPvData.size() == 0) {
            if (refreshData) {
                Log.d(TAG, "Refreshing year PV data");
            } else {
                Log.d(TAG, "No year PV data");
            }

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getBooleanExtra("success", true)) {
                        updateScreen(false);
                    } else {
                        Toast.makeText(context, intent.getStringExtra("message"),
                                Toast.LENGTH_LONG).show();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callYear(getContext());
        }

        updateTitle();
        updateGraph(yearPvData);
        updateTable(yearPvData);
    }
}
