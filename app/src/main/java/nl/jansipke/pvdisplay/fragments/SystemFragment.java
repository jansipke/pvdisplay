package nl.jansipke.pvdisplay.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.Objects;
import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.SettingsActivity;
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
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext()))
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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
        if (item.getItemId() == R.id.action_refresh) {
            Log.d(TAG, "Clicked refresh");
            callPvDataService();
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
            TextView nameTextView = fragmentView.findViewById(R.id.system_name_text);
            nameTextView.setText(systemPvDatum.getSystemName());
            fragmentView.findViewById(R.id.system_name_button).setOnClickListener(new View.OnClickListener() {
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

            TextView panelsTextView = fragmentView.findViewById(R.id.system_panels_text);
            final String panels = systemPvDatum.getPanelBrand() + "\n" +
                    getResources().getString(R.string.value_panels,
                            systemPvDatum.getNumberOfPanels(),
                            systemPvDatum.getPanelPower(),
                            systemPvDatum.getSystemSize());
            panelsTextView.setText(panels);
            fragmentView.findViewById(R.id.system_panels_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String uri = "http://www.google.com/#q=" + systemPvDatum.getPanelBrand();
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            });

            TextView inverterTextView = fragmentView.findViewById(R.id.system_inverter_text);
            final String inverter = systemPvDatum.getInverterBrand() + "\n" +
                    getResources().getString(R.string.value_inverter,
                            systemPvDatum.getInverterPower());
            inverterTextView.setText(inverter);
            fragmentView.findViewById(R.id.system_inverter_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String uri = "http://www.google.com/#q=" + systemPvDatum.getInverterBrand();
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            });

            TextView statisticsTextView = fragmentView.findViewById(R.id.system_statistics_text);
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            final String currency = sharedPreferences.getString(getResources().
                    getString(R.string.preferences_key_savings_currency), "EUR");
            double per_kwh;
            try {
                per_kwh = Double.parseDouble(sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_savings_per_kwh), "0.20"));
            } catch (Exception e) {
                per_kwh = 0.20;
            }
            double adjustment;
            try {
                adjustment = Double.parseDouble(sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_savings_adjustment), "0"));
            } catch (Exception e) {
                adjustment = 0;
            }
            final String savings = currency + " " + FormatUtils.SAVINGS_FORMAT.format(
                    statisticPvDatum.getEnergyGenerated() * per_kwh / 1000.0 + adjustment);
            final String statistics = getResources().getString(R.string.value_statistics_total,
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getEnergyGenerated() / 1000),
                    statisticPvDatum.getOutputs()) + "\n" +
                    getResources().getString(R.string.value_statistics_average,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getAverageGeneration() / 1000)) + "\n" +
                    getResources().getString(R.string.value_statistics_record,
                            FormatUtils.ENERGY_FORMAT.format(
                                    statisticPvDatum.getMaximumGeneration() / 1000),
                            new DateTimeUtils.YearMonthDay(
                                    statisticPvDatum.getRecordDateYear(),
                                    statisticPvDatum.getRecordDateMonth(),
                                    statisticPvDatum.getRecordDateDay()).asString(true)) + "\n" +
                    "Saved " + savings + " in total";
            statisticsTextView.setText(statistics);
            fragmentView.findViewById(R.id.system_statistics_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
