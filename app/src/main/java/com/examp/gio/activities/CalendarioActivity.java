package com.examp.gio.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.examp.gio.R;

public class CalendarioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nota: Asegúrate de que activity_calendario.xml exista o usa el correcto
        // setContentView(R.layout.activity_calendario); 

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}
