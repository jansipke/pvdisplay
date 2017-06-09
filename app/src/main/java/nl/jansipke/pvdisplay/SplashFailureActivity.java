package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SplashFailureActivity extends AppCompatActivity {

    private final static String TAG = SplashFailureActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_failure);

        Button button = (Button) findViewById(R.id.retry_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SplashFailureActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
