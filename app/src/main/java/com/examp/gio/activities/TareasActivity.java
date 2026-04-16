package com.examp.gio.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.adapters.TareaAdapter;
import com.examp.gio.models.Tarea;
import com.examp.gio.network.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TareasActivity extends AppCompatActivity {

    RecyclerView rvTareas;
    TareaAdapter adapter;
    List<Tarea> tareas = new ArrayList<>();
    ProgressBar progressBar;
    TextView tvEmpty;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tareas);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", 0);

        rvTareas = findViewById(R.id.rvTareas);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new TareaAdapter(tareas, true);
        rvTareas.setLayoutManager(new LinearLayoutManager(this));
        rvTareas.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        cargarTareas();
    }

    private void cargarTareas() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_usuario", idUsuario);
                String res = ApiClient.post("/empleado/mis_tareas.php", body.toString());
                JSONObject json = new JSONObject(res);

                if (json.getBoolean("success")) {
                    JSONArray arr = json.getJSONArray("tareas");
                    List<Tarea> lista = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject t = arr.getJSONObject(i);
                        lista.add(new Tarea(
                                t.getInt("id_tarea"),
                                t.getString("nombre"),
                                t.getString("descripcion"),
                                t.getString("estado"),
                                t.optString("fecha_inicio", ""),
                                t.optString("fecha_fin", "")
                        ));
                    }
                    runOnUiThread(() -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        tareas.clear();
                        tareas.addAll(lista);
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(tareas.isEmpty() ?
                                android.view.View.VISIBLE : android.view.View.GONE);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> progressBar.setVisibility(android.view.View.GONE));
            }
        });
    }
}