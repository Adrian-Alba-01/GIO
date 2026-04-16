package com.examp.gio.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.examp.gio.R;

public class ObrasActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obras);
        if (findViewById(R.id.btnBack) != null)
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}