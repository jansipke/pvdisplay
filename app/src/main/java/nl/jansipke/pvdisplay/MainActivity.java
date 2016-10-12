package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Created main activity");
    }

    public void showDailyGraph(View view) {
        Intent intent = new Intent(MainActivity.this, DailyActivity.class);
        startActivity(intent);
    }

    public void showLiveGraph(View view) {
        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
        startActivity(intent);
    }

    public void showMonthlyGraph(View view) {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    public void showStatistic(View view) {
        Intent intent = new Intent(MainActivity.this, SystemActivity.class);
        startActivity(intent);
    }

    public void showWeeklyGraph(View view) {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    public void showYearlyGraph(View view) {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }
}
