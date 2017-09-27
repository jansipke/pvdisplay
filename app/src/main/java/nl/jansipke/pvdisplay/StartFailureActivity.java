package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartFailureActivity extends AppCompatActivity {

    private final static String TAG = StartFailureActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_failure);

        Button button = (Button) findViewById(R.id.retry_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(StartFailureActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
