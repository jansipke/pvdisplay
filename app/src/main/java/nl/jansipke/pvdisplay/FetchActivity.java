package nl.jansipke.pvdisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class FetchActivity extends AppCompatActivity {

    private final static int NR_DOWNLOADS = 6;
    private final static String TAG = FetchActivity.class.getSimpleName();

    private void fetchPvData() {
        new Thread() {
            public void run() {
                Context context = getApplicationContext();
                DateTimeUtils.YearMonthDay today = DateTimeUtils.YearMonthDay.getToday();

                BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    int downloadsFinished = 0;
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String type = intent.getStringExtra("type");
                        Log.d(TAG, "Fetched " + type);
                        downloadsFinished += 1;
                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setProgress(downloadsFinished);
                        if (downloadsFinished == NR_DOWNLOADS) {
                            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                            Intent mainIntent = new Intent(FetchActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    }
                };

                IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
                LocalBroadcastManager.getInstance(context)
                        .registerReceiver(broadcastReceiver, intentFilter);

                PvDataService.callAll(context, today.year, today.month, today.day);
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(NR_DOWNLOADS);

        Button button = findViewById(R.id.cancel_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(FetchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        boolean autoRefresh = sharedPreferences.getBoolean(getResources().
                getString(R.string.preferences_key_auto_refresh), false);
        if (autoRefresh) {
            fetchPvData();
        } else {
            Intent intent = new Intent(FetchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
