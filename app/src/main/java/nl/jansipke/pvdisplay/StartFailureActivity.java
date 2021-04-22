package nl.jansipke.pvdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartFailureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_failure);

        Button button = findViewById(R.id.retry_button);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(StartFailureActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
