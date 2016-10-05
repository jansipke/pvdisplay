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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;

public class StatisticActivity extends AppCompatActivity {

    private final static String TAG = StatisticActivity.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private PvDataOperations pvDataOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        pvDataOperations = new PvDataOperations(getApplicationContext());
        showScreen();
    }

    public void showScreen() {
        Log.i(TAG, "Showing screen");

        String title = "Statistic";
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

        if (statisticPvDatum != null) {
            Log.i(TAG, statisticPvDatum.toString());
        }
    }
}
