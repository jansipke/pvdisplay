package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;

import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

public class SystemFragment extends Fragment {

    private final static String TAG = SystemFragment.class.getSimpleName();

    private View fragmentView;
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
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(broadcastReceiver, intentFilter);

        PvDataService.callStatistic(getContext());
        PvDataService.callSystem(getContext());
    }

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

        fragmentView = inflater.inflate(R.layout.fragment_system, container, false);
        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                callPvDataService();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateScreen() {
        Log.d(TAG, "Updating screen with statistic and system PV data");

        StatisticPvDatum statisticPvDatum = pvDataOperations.loadStatistic();
        final SystemPvDatum systemPvDatum = pvDataOperations.loadSystem();
        if (statisticPvDatum == null || systemPvDatum == null) {
            Log.d(TAG, "No statistic or system PV data");
            callPvDataService();
            return;
        }

        if (isAdded() && getActivity() != null) {
            TextView nameTextView = (TextView) fragmentView.findViewById(R.id.system);
            nameTextView.setText(systemPvDatum.getSystemName());
            nameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("geo:" +
                            systemPvDatum.getLatitude() + "," +
                            systemPvDatum.getLongitude() + "?z=14");
                    Log.d(TAG, "Opening Google Maps for URI: " + uri);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            });

            TextView panelsTextView = (TextView) fragmentView.findViewById(R.id.panels);
            panelsTextView.setText(
                    systemPvDatum.getPanelBrand() + "\n" +
                            getResources().getString(R.string.value_panels,
                                    systemPvDatum.getNumberOfPanels(),
                                    systemPvDatum.getPanelPower(),
                                    systemPvDatum.getSystemSize()));
            panelsTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String uri = "http://www.google.com/#q=" + systemPvDatum.getPanelBrand();
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            });

            TextView inverterTextView = (TextView) fragmentView.findViewById(R.id.inverter);
            inverterTextView.setText(
                    systemPvDatum.getInverterBrand() + "\n" +
                            getResources().getString(R.string.value_inverter,
                                    systemPvDatum.getInverterPower()));
            inverterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String uri = "http://www.google.com/#q=" + systemPvDatum.getInverterBrand();
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            });

            TextView statisticsTextView = (TextView) fragmentView.findViewById(R.id.statistics);
            statisticsTextView.setText(
                    getResources().getString(R.string.value_statistics_total,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getEnergyGenerated() / 1000),
                            statisticPvDatum.getOutputs()) + "\n" +
                            getResources().getString(R.string.value_statistics_average,
                                    FormatUtils.ENERGY_FORMAT.format(
                                            statisticPvDatum.getAverageGeneration() / 1000)) + "\n" +
                            getResources().getString(R.string.value_statistics_record,
                                    FormatUtils.ENERGY_FORMAT.format(
                                            statisticPvDatum.getMaximumGeneration() / 1000),
                                    DateTimeUtils.formatYearMonthDay(
                                            statisticPvDatum.getRecordDateYear(),
                                            statisticPvDatum.getRecordDateMonth(),
                                            statisticPvDatum.getRecordDateDay(), true)) + "\n");
            statisticsTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = getContext();
                    if (context != null) {
                        PvDataOperations pvDataOperations = new PvDataOperations(context);
                        RecordPvDatum recordPvDatum = pvDataOperations.loadRecord();
                        String text =
                                "Live power: " + getResources().getString(R.string.value_w,
                                        FormatUtils.POWER_FORMAT.format(
                                                recordPvDatum.getLivePowerGeneration())) +
                                "\nDaily energy: " + getResources().getString(R.string.value_kwh,
                                        FormatUtils.ENERGY_FORMAT.format(
                                                recordPvDatum.getDailyEnergyGenerated() / 1000)) +
                                "\nMonthly energy: " + getResources().getString(R.string.value_kwh,
                                        FormatUtils.ENERGY_FORMAT.format(
                                                recordPvDatum.getMonthlyEnergyGenerated() / 1000)) +
                                "\nYearly energy: " + getResources().getString(R.string.value_kwh,
                                        FormatUtils.ENERGY_FORMAT.format(
                                                recordPvDatum.getYearlyEnergyGenerated() / 1000));

                        Toast.makeText(context, text,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
