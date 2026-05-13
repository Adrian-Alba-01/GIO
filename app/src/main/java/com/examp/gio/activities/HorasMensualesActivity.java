package com.examp.gio.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.network.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HorasMensualesActivity extends AppCompatActivity {

    TextView tvTotalHoras, tvDiasTrabajados, tvMediaDiaria, tvMesActual;
    ProgressBar progressBar, progressBarMes;
    RecyclerView rvFichajes;
    ImageButton btnMesAnterior, btnMesSiguiente;

    int idUsuario;
    int mesActual;
    int anioActual;

    final int OBJETIVO_HORAS = 160;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] MESES = {
            "Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horas_mensuales);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", 0);

        Calendar cal = Calendar.getInstance();
        mesActual  = cal.get(Calendar.MONTH) + 1;
        anioActual = cal.get(Calendar.YEAR);

        tvTotalHoras      = findViewById(R.id.tvTotalHoras);
        tvDiasTrabajados  = findViewById(R.id.tvDiasTrabajados);
        tvMediaDiaria     = findViewById(R.id.tvMediaDiaria);
        tvMesActual       = findViewById(R.id.tvMesActual);
        progressBar       = findViewById(R.id.progressBar);
        progressBarMes    = findViewById(R.id.progressBarMes);
        rvFichajes        = findViewById(R.id.rvFichajes);
        btnMesAnterior    = findViewById(R.id.btnMesAnterior);
        btnMesSiguiente   = findViewById(R.id.btnMesSiguiente);

        rvFichajes.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnMesAnterior.setOnClickListener(v -> {
            mesActual--;
            if (mesActual < 1) { mesActual = 12; anioActual--; }
            actualizarTituloMes();
            cargarHoras();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            mesActual++;
            if (mesActual > 12) { mesActual = 1; anioActual++; }
            actualizarTituloMes();
            cargarHoras();
        });

        actualizarTituloMes();
        cargarHoras();
    }

    private void actualizarTituloMes() {
        tvMesActual.setText(MESES[mesActual - 1] + " " + anioActual);
    }

    private void cargarHoras() {
        progressBar.setVisibility(View.VISIBLE);
        rvFichajes.setVisibility(View.GONE);

        resetUI();

        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_usuario", idUsuario);
                body.put("mes",  mesActual);
                body.put("anio", anioActual);

                String res = ApiClient.post("/shared/horas_mensuales.php", body.toString());
                JSONObject json = new JSONObject(res);

                if (!json.getBoolean("success")) {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    return;
                }

                double total = json.getDouble("total_horas");
                int    dias  = json.getInt("dias_trabajados");
                double media = json.getDouble("media_diaria");
                JSONArray fichajes = json.getJSONArray("fichajes");

                // Construir adapter en background, bind en UI thread
                FichajeAdapter adapter = new FichajeAdapter(fichajes);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    rvFichajes.setVisibility(View.VISIBLE);

                    tvTotalHoras.setText(formatHoras(total));
                    tvDiasTrabajados.setText(String.valueOf(dias));
                    tvMediaDiaria.setText(formatHoras(media));

                    int progreso = (int) Math.min(total, OBJETIVO_HORAS);
                    progressBarMes.setMax(OBJETIVO_HORAS);
                    progressBarMes.setProgress(progreso);

                    rvFichajes.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        });
    }

    private void resetUI() {
        tvTotalHoras.setText("-- h");
        tvDiasTrabajados.setText("--");
        tvMediaDiaria.setText("-- h");
        progressBarMes.setProgress(0);
    }

    private String formatHoras(double horas) {
        int h = (int) horas;
        int m = (int) Math.round((horas - h) * 60);
        if (m == 0) return h + " h";
        return h + " h " + m + " min";
    }

    // ── Adapter inline ────────────────────────────────────────

    static class FichajeAdapter extends RecyclerView.Adapter<FichajeAdapter.VH> {

        final JSONArray datos;

        FichajeAdapter(JSONArray datos) { this.datos = datos; }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_fichaje_dia, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            try {
                JSONObject f = datos.getJSONObject(pos);
                h.tvDia.setText(f.getString("dia"));
                h.tvEntrada.setText(formatHora(f.optString("hora_entrada", "")));
                h.tvSalida.setText(formatHora(f.optString("hora_salida", "")));
                double horas = f.optDouble("horas_trabajadas", 0);
                int totalMin = (int) Math.round(horas * 60);
                int hh = totalMin / 60;
                int mm = totalMin % 60;
                h.tvHoras.setText(String.format("%dh %02dm", hh, mm));
            } catch (Exception ignored) {}
        }

        @Override
        public int getItemCount() { return datos.length(); }

        private static String formatHora(String datetime) {
            if (datetime == null || datetime.isEmpty()) return "--:--";
            // "2026-03-10 08:00:00" → "08:00"
            String[] parts = datetime.split(" ");
            if (parts.length < 2) return datetime;
            String[] time = parts[1].split(":");
            if (time.length < 2) return parts[1];
            return time[0] + ":" + time[1];
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvDia, tvEntrada, tvSalida, tvHoras;
            VH(View v) {
                super(v);
                tvDia     = v.findViewById(R.id.tvDia);
                tvEntrada = v.findViewById(R.id.tvEntrada);
                tvSalida  = v.findViewById(R.id.tvSalida);
                tvHoras   = v.findViewById(R.id.tvHoras);
            }
        }
    }
}