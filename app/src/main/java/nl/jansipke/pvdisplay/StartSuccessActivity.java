package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class StartSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_success);

        Intent intent = getIntent();

        ((TextView) findViewById(R.id.system_name_text)).setText(intent.getStringExtra("systemName"));

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit()
                .putString(getResources().getString(R.string.preferences_key_pvoutput_system_id), intent.getStringExtra("systemId"))
                .putString(getResources().getString(R.string.preferences_key_pvoutput_api_key), intent.getStringExtra("apiKey"))
                .apply();

        Button button = findViewById(R.id.start_button);
        button.setOnClickListener(view -> {
            Intent intent1 = new Intent(StartSuccessActivity.this, FetchActivity.class);
            startActivity(intent1);
            finish();
        });
    }
}
