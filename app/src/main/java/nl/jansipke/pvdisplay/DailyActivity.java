package nl.jansipke.pvdisplay;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import nl.jansipke.pvdisplay.database.PvDataOperations;

public class DailyActivity extends AppCompatActivity {

    private final static String TAG = DailyActivity.class.getSimpleName();
    private final static NumberFormat powerFormat = new DecimalFormat("#0");
    private final static NumberFormat energyFormat = new DecimalFormat("#0.000");

    private PvDataOperations pvDataOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        showScreen();
    }

    public void showScreen() {
        Log.i(TAG, "Showing screen");

        String title = "Daily";
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }
    }
}
