package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import io.fabric.sdk.android.Fabric;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String WEBSITE_URL = "http://www.jansipke.nl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        Log.i(TAG, "Creating main activity");

        setContentView(R.layout.activity_main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SystemPvDatum systemPvDatum = new PvDataOperations(this).loadSystem();
        if (systemPvDatum != null) {
            Answers.getInstance().logLogin(new LoginEvent()
                    .putCustomAttribute("System Name", systemPvDatum.getSystemName())
                    .putCustomAttribute("System Size", systemPvDatum.getSystemSize())
                    .putCustomAttribute("System Latitude", systemPvDatum.getLatitude())
                    .putCustomAttribute("System Longitude", systemPvDatum.getLongitude()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "Clicked settings");
                Intent intent = new Intent(this, SettingsActivity.class);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.action_about:
                Log.d(TAG, "Clicked about");
                Uri webpage = Uri.parse(WEBSITE_URL);
                intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
