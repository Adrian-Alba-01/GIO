package com.examp.gio.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.adapters.IncidenciaAdapter;
import com.examp.gio.models.Incidencia;
import com.examp.gio.network.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncidenciasActivity extends AppCompatActivity {

    RecyclerView rvIncidencias;
    IncidenciaAdapter adapter;
    List<Incidencia> incidencias = new ArrayList<>();
    ProgressBar progressBar;
    Button btnNuevaIncidencia;
    int idUsuario;
    int idObra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencias);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", 0);
        idObra = prefs.getInt("id_obra_actual", 0);

        rvIncidencias = findViewById(R.id.rvIncidencias);
        progressBar = findViewById(R.id.progressBar);
        btnNuevaIncidencia = findViewById(R.id.btnNuevaIncidencia);

        adapter = new IncidenciaAdapter(incidencias);
        rvIncidencias.setLayoutManager(new LinearLayoutManager(this));
        rvIncidencias.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnNuevaIncidencia.setOnClickListener(v -> mostrarDialogoNuevaIncidencia());

        cargarIncidencias();
    }

    private void cargarIncidencias() {
        progressBar.setVisibility(View.VISIBLE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_obra", idObra);
                String res = ApiClient.post("/encargado/incidencias.php", body.toString());
                JSONObject json = new JSONObject(res);

                if (json.getBoolean("success")) {
                    JSONArray arr = json.getJSONArray("incidencias");
                    List<Incidencia> lista = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject inc = arr.getJSONObject(i);
                        lista.add(new Incidencia(
                                inc.getInt("id_incidencia"),
                                inc.getString("descripcion"),
                                inc.getString("estado"),
                                inc.optString("fecha", ""),
                                inc.optString("nombre_usuario", "")
                        ));
                    }
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        incidencias.clear();
                        incidencias.addAll(lista);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        });
    }

    private void mostrarDialogoNuevaIncidencia() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Nueva Incidencia");

        EditText input = new EditText(this);
        input.setHint("Descripción de la incidencia");
        input.setPadding(48, 24, 48, 24);
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String desc = input.getText().toString().trim();
            if (!desc.isEmpty()) crearIncidencia(desc);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void crearIncidencia(String descripcion) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("descripcion", descripcion);
                body.put("id_usuario", idUsuario);
                body.put("id_obra", idObra);
                String res = ApiClient.post("/encargado/nueva_incidencia.php", body.toString());
                JSONObject json = new JSONObject(res);
                runOnUiThread(() -> {
                    if (json.optBoolean("success")) {
                        Toast.makeText(this, "Incidencia creada", Toast.LENGTH_SHORT).show();
                        cargarIncidencias();
                    } else {
                        Toast.makeText(this, "Error al crear incidencia", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}