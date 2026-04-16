package com.examp.gio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmpleadoHomeActivity extends AppCompatActivity {

    TextView tvBienvenido, tvDireccion, tvReloj, tvEstadoFichaje;
    ImageView ivNotificaciones, ivFotos, ivUbicacion, ivPerfil;
    Button btnFichar, btnHorasMensuales, btnVerTodas;
    RecyclerView rvTareaActual, rvTareasPendientes;
    LinearLayout llFichaje;

    TareaAdapter adapterActual, adapterPendientes;
    List<Tarea> tareasActuales = new ArrayList<>();
    List<Tarea> tareasPendientes = new ArrayList<>();

    Handler handlerReloj = new Handler(Looper.getMainLooper());
    Runnable runnableReloj;

    int idUsuario;
    String nombreUsuario;
    boolean fichado = false;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_home);

        // Recuperar sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", 0);
        nombreUsuario = prefs.getString("nombre", "Usuario");

        initViews();
        setupReloj();
        cargarDatos();

        btnFichar.setOnClickListener(v -> fichar());
        btnHorasMensuales.setOnClickListener(v -> {
            startActivity(new Intent(this, HorasMensualesActivity.class));
        });
        btnVerTodas.setOnClickListener(v -> {
            startActivity(new Intent(this, TareasActivity.class));
        });

        // Bottom nav
        findViewById(R.id.navInicio).setOnClickListener(v -> { /* ya estamos aquí */ });
        findViewById(R.id.navUbicacion).setOnClickListener(v ->
                startActivity(new Intent(this, ObrasActivity.class)));
        findViewById(R.id.navTareas).setOnClickListener(v ->
                startActivity(new Intent(this, TareasActivity.class)));
        findViewById(R.id.navPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));
    }

    private void initViews() {
        tvBienvenido = findViewById(R.id.tvBienvenido);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvReloj = findViewById(R.id.tvReloj);
        tvEstadoFichaje = findViewById(R.id.tvEstadoFichaje);
        btnFichar = findViewById(R.id.btnFichar);
        btnHorasMensuales = findViewById(R.id.btnHorasMensuales);
        btnVerTodas = findViewById(R.id.btnVerTodas);
        rvTareaActual = findViewById(R.id.rvTareaActual);
        rvTareasPendientes = findViewById(R.id.rvTareasPendientes);

        tvBienvenido.setText("¡Bienvenido " + nombreUsuario + "!");

        adapterActual = new TareaAdapter(tareasActuales, false);
        rvTareaActual.setLayoutManager(new LinearLayoutManager(this));
        rvTareaActual.setAdapter(adapterActual);

        adapterPendientes = new TareaAdapter(tareasPendientes, false);
        rvTareasPendientes.setLayoutManager(new LinearLayoutManager(this));
        rvTareasPendientes.setAdapter(adapterPendientes);
    }

    private void setupReloj() {
        runnableReloj = new Runnable() {
            @Override
            public void run() {
                String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                tvReloj.setText(hora);
                handlerReloj.postDelayed(this, 1000);
            }
        };
        handlerReloj.post(runnableReloj);
    }

    private void cargarDatos() {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_usuario", idUsuario);

                // Cargar obra actual
                String resObra = ApiClient.post("/empleado/obra_actual.php", body.toString());
                JSONObject jsonObra = new JSONObject(resObra);
                if (jsonObra.getBoolean("success")) {
                    JSONObject obra = jsonObra.getJSONObject("obra");
                    String direccion = obra.getString("nombre");
                    runOnUiThread(() -> tvDireccion.setText(direccion));
                }

                // Cargar tareas
                String resTareas = ApiClient.post("/empleado/mis_tareas.php", body.toString());
                JSONObject jsonTareas = new JSONObject(resTareas);
                if (jsonTareas.getBoolean("success")) {
                    JSONArray todas = jsonTareas.getJSONArray("tareas");
                    List<Tarea> actuales = new ArrayList<>();
                    List<Tarea> pendientes = new ArrayList<>();

                    for (int i = 0; i < todas.length(); i++) {
                        JSONObject t = todas.getJSONObject(i);
                        Tarea tarea = new Tarea(
                                t.getInt("id_tarea"),
                                t.getString("nombre"),
                                t.getString("descripcion"),
                                t.getString("estado"),
                                t.optString("fecha_inicio", ""),
                                t.optString("fecha_fin", "")
                        );
                        if ("en_proceso".equals(tarea.estado)) {
                            actuales.add(tarea);
                        } else if ("pendiente".equals(tarea.estado)) {
                            pendientes.add(tarea);
                        }
                    }
                    runOnUiThread(() -> {
                        tareasActuales.clear();
                        tareasActuales.addAll(actuales);
                        adapterActual.notifyDataSetChanged();

                        tareasPendientes.clear();
                        tareasPendientes.addAll(pendientes);
                        adapterPendientes.notifyDataSetChanged();
                    });
                }

                // Comprobar estado fichaje
                String resFichaje = ApiClient.post("/empleado/estado_fichaje.php", body.toString());
                JSONObject jsonFichaje = new JSONObject(resFichaje);
                if (jsonFichaje.getBoolean("success")) {
                    boolean activo = jsonFichaje.getBoolean("fichado");
                    runOnUiThread(() -> actualizarEstadoFichaje(activo));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fichar() {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_usuario", idUsuario);
                // En producción añadir latitud/longitud real con LocationManager
                body.put("latitud", 40.4168);
                body.put("longitud", -3.7038);

                String endpoint = fichado ? "/empleado/salida.php" : "/empleado/entrada.php";
                String res = ApiClient.post(endpoint, body.toString());
                JSONObject json = new JSONObject(res);

                runOnUiThread(() -> {
                    if (json.optBoolean("success")) {
                        fichado = !fichado;
                        actualizarEstadoFichaje(fichado);
                        Toast.makeText(this,
                                fichado ? "Entrada registrada" : "Salida registrada",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, json.optString("message", "Error al fichar"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void actualizarEstadoFichaje(boolean activo) {
        fichado = activo;
        tvEstadoFichaje.setText(activo ? "● Activo" : "● Inactivo");
        tvEstadoFichaje.setTextColor(getResources().getColor(
                activo ? R.color.verde : R.color.amarillo, null));
        btnFichar.setText(activo ? "Registrar Salida" : "Registrar Entrada");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerReloj.removeCallbacks(runnableReloj);
    }
}