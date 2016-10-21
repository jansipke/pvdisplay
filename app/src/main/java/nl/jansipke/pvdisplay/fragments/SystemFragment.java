package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

public class SystemFragment extends Fragment {

    private final static String TAG = SystemFragment.class.getSimpleName();

    private View fragmentView;
    private LayoutInflater layoutInflater;
    private PvDataOperations pvDataOperations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        pvDataOperations = new PvDataOperations(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_system, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_system, container, false);
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
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                updateScreen(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        String title = "System";
        if (pvDataOperations != null) {
            SystemPvDatum systemPvDatum = pvDataOperations.loadSystem();
            if (systemPvDatum != null &&
                    systemPvDatum.getSystemName() != null &&
                    systemPvDatum.getSystemName().length() > 0) {
                title = systemPvDatum.getSystemName();
            }
        }
        AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
        if (appCompatActivity != null) {
            ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setTitle(title);
            }
        }
    }

    private void updateScreen(boolean refreshData) {
        Log.d(TAG, "Updating screen");

        StatisticPvDatum statisticPvDatum = pvDataOperations.loadStatistic();
        SystemPvDatum systemPvDatum = pvDataOperations.loadSystem();
        if (refreshData || statisticPvDatum == null || systemPvDatum == null) {
            if (refreshData) {
                Log.i(TAG, "Refreshing statistic and system PV data");
            } else {
                Log.i(TAG, "No statistic or system PV data");
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

            PvDataService.callStatistic(getContext());
            PvDataService.callSystem(getContext());
        }

        if (statisticPvDatum != null && systemPvDatum != null) {
            TextView textView = (TextView) fragmentView.findViewById(R.id.header);
            textView.setText(getResources().getString(
                    R.string.value_kwh,
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getEnergyGenerated() / 1000)));

            LinearLayout tableLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
            tableLinearLayout.removeAllViews();
            String[] keys = {
                    "System size",
                    "Number of panels",
                    "Panel power",
                    "Panel brand",
                    "Inverter power",
                    "Inverter brand",
                    "Latitude",
                    "Longitude",
                    "Average generation",
                    "Maximum generation",
                    "Outputs",
                    "First date",
                    "Record date",
                    "Last date"
            };
            String[] values = {
                    getResources().getString(R.string.value_w,
                            FormatUtils.POWER_FORMAT.format(systemPvDatum.getSystemSize())),
                    systemPvDatum.getNumberOfPanels() + "",
                    getResources().getString(R.string.value_w,
                            FormatUtils.POWER_FORMAT.format(systemPvDatum.getPanelPower())),
                    systemPvDatum.getPanelBrand(),
                    getResources().getString(R.string.value_w,
                            FormatUtils.POWER_FORMAT.format(systemPvDatum.getInverterPower())),
                    systemPvDatum.getInverterBrand(),
                    getResources().getString(R.string.value_degrees,
                            FormatUtils.DEGREES_FORMAT.format(systemPvDatum.getLatitude())),
                    getResources().getString(R.string.value_degrees,
                            FormatUtils.DEGREES_FORMAT.format(systemPvDatum.getLongitude())),
                    getResources().getString(R.string.value_kwh,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getAverageGeneration() / 1000)),
                    getResources().getString(R.string.value_kwh,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getMaximumGeneration() / 1000)),
                    getResources().getString(R.string.value_days, statisticPvDatum.getOutputs()),
                    DateTimeUtils.formatDate(
                            statisticPvDatum.getActualDateFromYear(),
                            statisticPvDatum.getActualDateFromMonth(),
                            statisticPvDatum.getActualDateFromDay(), true),
                    DateTimeUtils.formatDate(
                            statisticPvDatum.getRecordDateYear(),
                            statisticPvDatum.getRecordDateMonth(),
                            statisticPvDatum.getRecordDateDay(), true),
                    DateTimeUtils.formatDate(
                            statisticPvDatum.getActualDateToYear(),
                            statisticPvDatum.getActualDateToMonth(),
                            statisticPvDatum.getActualDateToDay(), true)
            };
            for (int i = 0; i < keys.length; i++) {
                View row = layoutInflater.inflate(R.layout.row_system, null);
                ((TextView) row.findViewById(R.id.key)).setText(keys[i]);
                ((TextView) row.findViewById(R.id.value)).setText(values[i]);
                tableLinearLayout.addView(row);
            }
        }
    }
}
