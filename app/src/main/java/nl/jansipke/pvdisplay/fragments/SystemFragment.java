package nl.jansipke.pvdisplay.fragments;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.SettingsActivity;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDatabase;
import nl.jansipke.pvdisplay.download.PvDownloader;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

public class SystemFragment extends Fragment {

    private final static String TAG = SystemFragment.class.getSimpleName();

    private View fragmentView;
    private LayoutInflater layoutInflater;

    private PvDatabase pvDatabase;
    private PvDownloader pvDownloader;

    private StatisticPvDatum databaseOrDownloadStatistic() {
        StatisticPvDatum datum = pvDatabase.loadStatistic();
        if (datum == null) {
            pvDownloader.downloadStatistic();
            datum = new StatisticPvDatum(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        return datum;
    }

    private SystemPvDatum databaseOrDownloadSystem() {
        SystemPvDatum datum = pvDatabase.loadSystem();
        if (datum == null) {
            pvDownloader.downloadSystem();
            datum = new SystemPvDatum("Unknown system name", 0, "Unknown postcode",0, 0, "Unknown panel brand", 0, 0, "Unknown inverter brand", "Unknown orientation", "Unknown array tilt", "Unknown shade", "Unknown install date", 0, 0);
        }
        return datum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        pvDatabase = new PvDatabase(getContext());

        pvDownloader = new PvDownloader(getContext());
//        pvDownloader.getErrorMessage().observe(this, data -> Toast.makeText(getContext(),data, Toast.LENGTH_LONG).show());
        pvDownloader.getDownloadSuccessCount().observe(this, data -> updateScreen());
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

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_system, container, false);
        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Log.d(TAG, "Clicked refresh");
            pvDownloader.downloadStatistic();
            pvDownloader.downloadSystem();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateScreen() {
        Log.d(TAG, "Updating screen with statistic and system PV data");

        final StatisticPvDatum statisticPvDatum = databaseOrDownloadStatistic();
        final SystemPvDatum systemPvDatum = databaseOrDownloadSystem();

        if (isAdded() && getActivity() != null) {
            LinearLayout linearLayout = fragmentView.findViewById(R.id.system);
            linearLayout.removeAllViews();

            // Name
            View nameCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) nameCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.name);
            ((TextView) nameCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_name));
            final String name = systemPvDatum.getSystemName();
            ((TextView) nameCard.findViewById(R.id.card_text)).setText(name);
            nameCard.findViewById(R.id.card).setOnClickListener(view -> {
                Uri uri = Uri.parse("geo:" +
                                systemPvDatum.getLatitude() + "," +
                                systemPvDatum.getLongitude() + "?z=14");
                Log.d(TAG, "Opening Google Maps for URI: " + uri);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            });
            linearLayout.addView(nameCard);

            // Panels
            View panelsCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) panelsCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.panels);
            ((TextView) panelsCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_panels));
            final String panels = systemPvDatum.getPanelBrand() + "\n" +
                    getResources().getString(R.string.value_panels,
                            systemPvDatum.getNumberOfPanels(),
                            systemPvDatum.getPanelPower(),
                            systemPvDatum.getSystemSize());
            ((TextView) panelsCard.findViewById(R.id.card_text)).setText(panels);
            panelsCard.findViewById(R.id.card).setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String uri = "http://www.google.com/#q=" + systemPvDatum.getPanelBrand();
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            });
            linearLayout.addView(panelsCard);

            // Inverter
            View inverterCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) inverterCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.inverter);
            ((TextView) inverterCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_inverter));
            final String inverter = systemPvDatum.getInverterBrand() + "\n" +
                    getResources().getString(R.string.value_inverter,
                            systemPvDatum.getInverterPower());
            ((TextView) inverterCard.findViewById(R.id.card_text)).setText(inverter);
            inverterCard.findViewById(R.id.card).setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String uri = "http://www.google.com/#q=" + systemPvDatum.getInverterBrand();
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            });
            linearLayout.addView(inverterCard);

            // Savings
            View savingsCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) savingsCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.savings);
            ((TextView) savingsCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_savings));
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
            ((TextView) savingsCard.findViewById(R.id.card_text)).setText(savings);
            savingsCard.findViewById(R.id.card).setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            });
            linearLayout.addView(savingsCard);

            // Statistics - total
            View statisticsTotalCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) statisticsTotalCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.total);
            ((ImageView) statisticsTotalCard.findViewById(R.id.card_right_image)).setImageDrawable(null);
            ((TextView) statisticsTotalCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_total));
            final String statisticsTotal = getResources().getString(R.string.value_statistics_total,
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getEnergyGenerated() / 1000),
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getEnergyExported() / 1000),
                    statisticPvDatum.getOutputs());
            ((TextView) statisticsTotalCard.findViewById(R.id.card_text)).setText(statisticsTotal);
            linearLayout.addView(statisticsTotalCard);

            // Statistics - average
            View statisticsAverageCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) statisticsAverageCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.average);
            ((ImageView) statisticsAverageCard.findViewById(R.id.card_right_image)).setImageDrawable(null);
            ((TextView) statisticsAverageCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_average));
            final String statisticsAverage = getResources().getString(R.string.value_statistics_average,
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getAverageGeneration() / 1000),
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getAverageEfficiency()));
            ((TextView) statisticsAverageCard.findViewById(R.id.card_text)).setText(statisticsAverage);
            linearLayout.addView(statisticsAverageCard);

            // Statistics - record
            View statisticsRecordCard = layoutInflater.inflate(R.layout.card_system, linearLayout, false);
            ((ImageView) statisticsRecordCard.findViewById(R.id.card_left_image)).setImageResource(R.drawable.record);
            ((ImageView) statisticsRecordCard.findViewById(R.id.card_right_image)).setImageDrawable(null);
            ((TextView) statisticsRecordCard.findViewById(R.id.card_title)).setText(getResources().getString(R.string.fragment_system_record));
            final String statisticsRecord = getResources().getString(R.string.value_statistics_record,
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getMaximumGeneration() / 1000),
                    new DateTimeUtils.YearMonthDay(
                            statisticPvDatum.getRecordDateYear(),
                            statisticPvDatum.getRecordDateMonth(),
                            statisticPvDatum.getRecordDateDay()).asString(true),
                    FormatUtils.ENERGY_FORMAT.format(
                            statisticPvDatum.getRecordEfficiency()));
            ((TextView) statisticsRecordCard.findViewById(R.id.card_text)).setText(statisticsRecord);
            linearLayout.addView(statisticsRecordCard);
        }
    }
}
