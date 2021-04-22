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

    private final static int NR_DOWNLOADS = 6;
    private final static String TAG = FetchActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(NR_DOWNLOADS);

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
            pvDownloader.getDownloadTotalCount().observe(this, data -> {
                Log.d(TAG, "Downloaded " + data + " of " + NR_DOWNLOADS + " pieces of data");
                progressBar.setProgress(data);
                if (data == NR_DOWNLOADS) {
                    Intent mainIntent = new Intent(FetchActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            });
            pvDownloader.downloadSystem();
            pvDownloader.downloadStatistic();
            pvDownloader.downloadLive(DateTimeUtils.YearMonthDay.getToday());
            pvDownloader.downloadDaily(DateTimeUtils.YearMonth.getToday());
            pvDownloader.downloadMonthly(DateTimeUtils.Year.getToday());
            pvDownloader.downloadYearly();
        } else {
            Intent intent = new Intent(FetchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
