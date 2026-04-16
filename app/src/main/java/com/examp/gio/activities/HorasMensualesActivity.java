package com.examp.gio.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.examp.gio.R;

public class HorasMensualesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horas_mensuales);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        cargarHoras();
    }

    private void cargarHoras() {
        android.content.SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        int idUsuario = prefs.getInt("id_usuario", 0);

        java.util.concurrent.ExecutorService executor =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                org.json.JSONObject body = new org.json.JSONObject();
                body.put("id_usuario", idUsuario);
                String res = com.examp.gio.network.ApiClient.post(
                        "/empleado/horas_mensuales.php", body.toString());
                org.json.JSONObject json = new org.json.JSONObject(res);
                if (json.getBoolean("success")) {
                    String totalHoras = json.getString("total_horas");
                    runOnUiThread(() -> {
                        TextView tv = findViewById(R.id.tvTotalHoras);
                        if (tv != null) tv.setText("Total horas: " + totalHoras + "h");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}