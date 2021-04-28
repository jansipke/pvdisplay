package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AppCompatActivity;
import nl.jansipke.pvdisplay.download.PvDownloader;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class FetchActivity extends AppCompatActivity {

    private final static String TAG = FetchActivity.class.getSimpleName();

    private final static AtomicInteger nrDownloads = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);

        Button button = findViewById(R.id.cancel_button);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(FetchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        boolean autoRefresh = sharedPreferences.getBoolean(getResources().
                getString(R.string.preferences_key_auto_refresh), false);
        if (autoRefresh) {
            PvDownloader pvDownloader = new PvDownloader(getApplicationContext());

            pvDownloader.downloadLive(DateTimeUtils.YearMonthDay.getToday());
            nrDownloads.addAndGet(1);
            switch(sharedPreferences.getString(getResources().
                    getString(R.string.preferences_key_live_comparison), "day")) {
                case "day":
                    pvDownloader.downloadLive(DateTimeUtils.YearMonthDay.getToday().createCopy(0, 0, -1, false));
                    nrDownloads.addAndGet(1);
                    break;
                case "year":
                    pvDownloader.downloadLive(DateTimeUtils.YearMonthDay.getToday().createCopy(-1, 0, 0, false));
                    nrDownloads.addAndGet(1);
                    break;
            }

            pvDownloader.downloadDaily(DateTimeUtils.YearMonth.getToday());
            nrDownloads.addAndGet(1);
            if ("year".equals(sharedPreferences.getString(getResources().
                    getString(R.string.preferences_key_daily_comparison), "year"))) {
                pvDownloader.downloadDaily(DateTimeUtils.YearMonth.getToday().createCopy(-1, 0, false));
                nrDownloads.addAndGet(1);
            }

            pvDownloader.downloadMonthly(DateTimeUtils.Year.getToday());
            nrDownloads.addAndGet(1);
            if ("year".equals(sharedPreferences.getString(getResources().
                    getString(R.string.preferences_key_monthly_comparison), "year"))) {
                pvDownloader.downloadMonthly(DateTimeUtils.Year.getToday().createCopy(-1, false));
                nrDownloads.addAndGet(1);
            }

            pvDownloader.downloadYearly();
            nrDownloads.addAndGet(1);

            pvDownloader.downloadSystem();
            nrDownloads.addAndGet(1);

            pvDownloader.downloadStatistic();
            nrDownloads.addAndGet(1);

            ProgressBar progressBar = findViewById(R.id.progress_bar);
            progressBar.setMax(nrDownloads.get());

            pvDownloader.getDownloadTotalCount().observe(this, data -> {
                Log.d(TAG, "Downloaded " + data + " of " + nrDownloads + " pieces of data");
                progressBar.setProgress(data);
                if (data >= nrDownloads.get()) {
                    Intent mainIntent = new Intent(FetchActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            });
        } else {
            Intent intent = new Intent(FetchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
