package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartSuccessActivity extends AppCompatActivity {

    private final static String TAG = StartSuccessActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_success);

        Intent intent = getIntent();

        ((TextView) findViewById(R.id.system)).setText(intent.getStringExtra("systemName"));

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit()
                .putString(getResources().getString(R.string.preferences_key_pvoutput_system_id), intent.getStringExtra("systemId"))
                .putString(getResources().getString(R.string.preferences_key_pvoutput_api_key), intent.getStringExtra("apiKey"))
                .apply();

        Button button = (Button) findViewById(R.id.start_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(StartSuccessActivity.this, FetchActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
