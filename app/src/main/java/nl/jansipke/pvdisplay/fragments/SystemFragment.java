package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private void updateScreen(boolean refreshData) {
        Log.d(TAG, "Updating screen");

        StatisticPvDatum statisticPvDatum = pvDataOperations.loadStatistic();
        SystemPvDatum systemPvDatum = pvDataOperations.loadSystem();
        if (refreshData || statisticPvDatum == null || systemPvDatum == null) {
            if (refreshData) {
                Log.d(TAG, "Refreshing statistic and system PV data");
            } else {
                Log.d(TAG, "No statistic or system PV data");
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
            ((TextView) fragmentView.findViewById(R.id.system)).setText(
                    systemPvDatum.getSystemName() + "\n" +
                    getResources().getString(R.string.value_location,
                            FormatUtils.DEGREES_FORMAT.format(systemPvDatum.getLatitude()),
                            FormatUtils.DEGREES_FORMAT.format(systemPvDatum.getLongitude())));

            ((TextView) fragmentView.findViewById(R.id.panels)).setText(
                    systemPvDatum.getPanelBrand() + "\n" +
                    getResources().getString(R.string.value_panels,
                            systemPvDatum.getNumberOfPanels(),
                            systemPvDatum.getPanelPower(),
                            systemPvDatum.getSystemSize()));

            ((TextView) fragmentView.findViewById(R.id.inverter)).setText(
                    systemPvDatum.getInverterBrand() + "\n" +
                    getResources().getString(R.string.value_inverter,
                            systemPvDatum.getInverterPower()));

            ((TextView) fragmentView.findViewById(R.id.statistics)).setText(
                    getResources().getString(R.string.value_statistics_total,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getEnergyGenerated() / 1000)) + "\n" +
                    getResources().getString(R.string.value_statistics_average,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getAverageGeneration() / 1000)) + "\n" +
                    getResources().getString(R.string.value_statistics_record,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getMaximumGeneration() / 1000),
                            DateTimeUtils.formatYearMonthDay(
                                    statisticPvDatum.getRecordDateYear(),
                                    statisticPvDatum.getRecordDateMonth(),
                                    statisticPvDatum.getRecordDateDay(), true)) + "\n" +
                    getResources().getString(R.string.value_statistics_from_to,
                            DateTimeUtils.formatYearMonthDay(
                                    statisticPvDatum.getActualDateFromYear(),
                                    statisticPvDatum.getActualDateFromMonth(),
                                    statisticPvDatum.getActualDateFromDay(), true),
                            DateTimeUtils.formatYearMonthDay(
                                    statisticPvDatum.getActualDateToYear(),
                                    statisticPvDatum.getActualDateToMonth(),
                                    statisticPvDatum.getActualDateToDay(), true)));


//            TextView textView = (TextView) fragmentView.findViewById(R.id.header);
//            textView.setText(getResources().getString(
//                    R.string.value_kwh,
//                    FormatUtils.ENERGY_FORMAT.format(
//                            statisticPvDatum.getEnergyGenerated() / 1000)));
//
//            LinearLayout tableLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
//            tableLinearLayout.removeAllViews();
//            String[] keys = {
//                    "System name",
//                    "System size",
//                    "Number of panels",
//                    "Panel power",
//                    "Panel brand",
//                    "Inverter power",
//                    "Inverter brand",
//                    "Latitude",
//                    "Longitude",
//                    "Average generation",
//                    "Maximum generation",
//                    "Outputs",
//                    "First date",
//                    "Record date",
//                    "Last date"
//            };
//            String[] values = {
//                    systemPvDatum.getSystemName(),
//                    getResources().getString(R.string.value_w,
//                            FormatUtils.POWER_FORMAT.format(systemPvDatum.getSystemSize())),
//                    systemPvDatum.getNumberOfPanels() + "",
//                    getResources().getString(R.string.value_w,
//                            FormatUtils.POWER_FORMAT.format(systemPvDatum.getPanelPower())),
//                    systemPvDatum.getPanelBrand(),
//                    getResources().getString(R.string.value_w,
//                            FormatUtils.POWER_FORMAT.format(systemPvDatum.getInverterPower())),
//                    systemPvDatum.getInverterBrand(),
//                    getResources().getString(R.string.value_degrees,
//                            FormatUtils.DEGREES_FORMAT.format(systemPvDatum.getLatitude())),
//                    getResources().getString(R.string.value_degrees,
//                            FormatUtils.DEGREES_FORMAT.format(systemPvDatum.getLongitude())),
//                    getResources().getString(R.string.value_kwh,
//                            FormatUtils.ENERGY_FORMAT.format(
//                                    statisticPvDatum.getAverageGeneration() / 1000)),
//                    getResources().getString(R.string.value_kwh,
//                            FormatUtils.ENERGY_FORMAT.format(
//                                    statisticPvDatum.getMaximumGeneration() / 1000)),
//                    getResources().getString(R.string.value_days, statisticPvDatum.getOutputs()),
//                    DateTimeUtils.formatYearMonthDay(
//                            statisticPvDatum.getActualDateFromYear(),
//                            statisticPvDatum.getActualDateFromMonth(),
//                            statisticPvDatum.getActualDateFromDay(), true),
//                    DateTimeUtils.formatYearMonthDay(
//                            statisticPvDatum.getRecordDateYear(),
//                            statisticPvDatum.getRecordDateMonth(),
//                            statisticPvDatum.getRecordDateDay(), true),
//                    DateTimeUtils.formatYearMonthDay(
//                            statisticPvDatum.getActualDateToYear(),
//                            statisticPvDatum.getActualDateToMonth(),
//                            statisticPvDatum.getActualDateToDay(), true)
//            };
//            for (int i = 0; i < keys.length; i++) {
//                View row = layoutInflater.inflate(R.layout.row_system, null);
//                ((TextView) row.findViewById(R.id.key)).setText(keys[i]);
//                ((TextView) row.findViewById(R.id.value)).setText(values[i]);
//                tableLinearLayout.addView(row);
//            }
        }
    }
}
