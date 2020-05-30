package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.HashMap;
import java.util.Map;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.utils.NetworkUtils;

public class StartActivity extends AppCompatActivity {

    private void getInitialSystemPvDatum(final String systemId, final String apiKey) {
        new Thread() {
            public void run() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Pvoutput-Apikey", apiKey);
                headers.put("X-Pvoutput-SystemId", systemId);
                String url = PvDataService.URL_BASE + "getsystem.jsp";
                Intent intent = null;
                try {
                    String result = NetworkUtils.httpGet(url, headers);
                    SystemPvDatum systemPvDatum = new PvOutputParser().parseSystem(result);
                    intent = new Intent(StartActivity.this, StartSuccessActivity.class);
                    intent.putExtra("systemId", systemId);
                    intent.putExtra("apiKey", apiKey);
                    intent.putExtra("systemName", systemPvDatum.getSystemName());
                } catch (Exception e) {
                    intent = new Intent(StartActivity.this, StartFailureActivity.class);
                } finally {
                    startActivity(intent);
                    finish();
                }
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        String systemId = sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_pvoutput_system_id), "");
        String apiKey = sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_pvoutput_api_key), "");
        if (systemId.equals("") || (apiKey.equals(""))) {
            Button button = findViewById(R.id.continue_button);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String systemId = ((EditText) findViewById(R.id.system_id)).getText().toString();
                    String apiKey = ((EditText) findViewById(R.id.api_key)).getText().toString();
                    getInitialSystemPvDatum(systemId, apiKey);
                }
            });
        } else {
            Intent intent = new Intent(StartActivity.this, FetchActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
