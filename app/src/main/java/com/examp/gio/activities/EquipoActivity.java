package com.examp.gio.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.examp.gio.R;

public class EquipoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nota: Asegúrate de que activity_equipo.xml sea el layout correcto
        setContentView(R.layout.activity_equipo);

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}
