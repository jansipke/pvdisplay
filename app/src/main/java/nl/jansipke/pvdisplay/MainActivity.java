package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void fetch(View view) {
        Log.i(TAG, "Starting PvDataService");
        Intent intent = new Intent(getApplicationContext(), PvDataService.class);
        intent.putExtra("type", "live");
        intent.putExtra("year", 2016);
        intent.putExtra("month", 9);
        intent.putExtra("day", 24);
        startService(intent);
    }

    public void showLiveGraph(View view) {
        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
        startActivity(intent);
    }
}
