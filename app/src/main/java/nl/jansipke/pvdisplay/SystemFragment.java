package nl.jansipke.pvdisplay;

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
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class SystemFragment extends Fragment {

    private final static String TAG = SystemFragment.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

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

        View view = inflater.inflate(R.layout.fragment_header_table, container, false);

        StatisticPvDatum statisticPvDatum = pvDataOperations.loadStatistic();
        if (statisticPvDatum == null) {
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onCreateView(inflater, container, savedInstanceState);
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callStatistic(getContext());
        }
        SystemPvDatum systemPvDatum = pvDataOperations.loadSystem();
        if (systemPvDatum == null) {
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onCreateView(inflater, container, savedInstanceState);
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callSystem(getContext());
        }

        if (statisticPvDatum != null && systemPvDatum != null) {
            LinearLayout tableLinearLayout = (LinearLayout) view.findViewById(R.id.table);
            tableLinearLayout.removeAllViews();
            String[] keys = {
                    "System name",
                    "System size",
                    "Number of panels",
                    "Panel power",
                    "Panel brand",
                    "Inverter power",
                    "Inverter brand",
                    "Latitude",
                    "Longitude",
                    "Energy generated",
                    "Average generation",
                    "Maximum generation",
                    "Outputs",
                    "First date",
                    "Record date",
                    "Last date"
            };
            String[] values = {
                    systemPvDatum.getSystemName(),
                    systemPvDatum.getSystemSize() + " W",
                    systemPvDatum.getNumberOfPanels() + "",
                    systemPvDatum.getPanelPower() + " W",
                    systemPvDatum.getPanelBrand(),
                    systemPvDatum.getInverterPower() + " W",
                    systemPvDatum.getInverterBrand(),
                    systemPvDatum.getLatitude() + "",
                    systemPvDatum.getLongitude() + "",
                    statisticPvDatum.getEnergyGenerated() / 1000 + " kWh",
                    statisticPvDatum.getAverageGeneration() / 1000 + " kWh",
                    statisticPvDatum.getMaximumGeneration() / 1000 + " kWh",
                    statisticPvDatum.getOutputs() + " days",
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
                View row = inflater.inflate(R.layout.table_2column_row, null);
                ((TextView) row.findViewById(R.id.time)).setText(keys[i]);
                ((TextView) row.findViewById(R.id.peak)).setText(values[i]);
                tableLinearLayout.addView(row);
            }
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.i(TAG, "Clicked refresh");
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
