package nl.jansipke.pvdisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class SystemActivity extends AppCompatActivity {

    private final static String TAG = SystemActivity.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private PvDataOperations pvDataOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);

        pvDataOperations = new PvDataOperations(getApplicationContext());
        showScreen();
    }

    public void showScreen() {
        Log.i(TAG, "Showing screen");

        String title = "System";
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }

        StatisticPvDatum statisticPvDatum = pvDataOperations.loadStatistic();
        if (statisticPvDatum == null) {
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    showScreen();
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callStatistic(getApplicationContext());
        }
        SystemPvDatum systemPvDatum = pvDataOperations.loadSystem();
        if (systemPvDatum == null) {
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    showScreen();
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callSystem(getApplicationContext());
        }

        if (statisticPvDatum != null && systemPvDatum != null) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.table);
            linearLayout.removeAllViews();
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
                View row = getLayoutInflater().inflate(R.layout.table_2column_row, null);
                ((TextView) row.findViewById(R.id.content1)).setText(keys[i]);
                ((TextView) row.findViewById(R.id.content2)).setText(values[i]);
                linearLayout.addView(row);
            }
        }
    }
}
