package nl.jansipke.pvdisplay;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String WEBSITE = "https://github.com/jansipke/pvdisplay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Creating main activity");

        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View application = findViewById(R.id.application);
        if (application != null) {
            ViewCompat.setOnApplyWindowInsetsListener(application, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                mlp.leftMargin = insets.left;
                mlp.bottomMargin = insets.bottom;
                mlp.rightMargin = insets.right;
                mlp.topMargin = insets.top;
                v.setLayoutParams(mlp);
                return WindowInsetsCompat.CONSUMED;
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setStatusBarContrastEnforced(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            Log.d(TAG, "Clicked settings");
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.action_about_donate) {
            Log.d(TAG, "Clicked about/donate");
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "No activity found for viewing the about/donate website");
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
