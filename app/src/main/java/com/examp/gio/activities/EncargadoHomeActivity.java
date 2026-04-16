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
import com.examp.gio.adapters.EmpleadoEstadoAdapter;
import com.examp.gio.models.EmpleadoEstado;
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

public class EncargadoHomeActivity extends AppCompatActivity {

    TextView tvBienvenido, tvDireccion, tvReloj, tvEstadoFichaje;
    Button btnFichar, btnHorasMensuales, btnVerEquipo;
    RecyclerView rvEquipo;

    EmpleadoEstadoAdapter adapterEquipo;
    List<EmpleadoEstado> equipo = new ArrayList<>();

    Handler handlerReloj = new Handler(Looper.getMainLooper());
    Runnable runnableReloj;

    int idUsuario;
    String nombreUsuario;
    boolean fichado = false;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encargado_home);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", 0);
        nombreUsuario = prefs.getString("nombre", "Usuario");

        initViews();
        setupReloj();
        cargarDatos();

        btnFichar.setOnClickListener(v -> fichar());
        btnHorasMensuales.setOnClickListener(v ->
                startActivity(new Intent(this, HorasMensualesActivity.class)));
        btnVerEquipo.setOnClickListener(v ->
                startActivity(new Intent(this, EquipoActivity.class)));

        // Bottom nav
        findViewById(R.id.navInicio).setOnClickListener(v -> { /* ya estamos */ });
        findViewById(R.id.navTareas).setOnClickListener(v ->
                startActivity(new Intent(this, TareasActivity.class)));
        findViewById(R.id.navTareasEquipo).setOnClickListener(v ->
                startActivity(new Intent(this, TareasEquipoActivity.class)));
        findViewById(R.id.navIncidencias).setOnClickListener(v ->
                startActivity(new Intent(this, IncidenciasActivity.class)));
        findViewById(R.id.navCalendario).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarioActivity.class)));
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
        btnVerEquipo = findViewById(R.id.btnVerEquipo);
        rvEquipo = findViewById(R.id.rvEquipo);

        tvBienvenido.setText("¡Bienvenido " + nombreUsuario + "!");

        adapterEquipo = new EmpleadoEstadoAdapter(equipo);
        rvEquipo.setLayoutManager(new LinearLayoutManager(this));
        rvEquipo.setAdapter(adapterEquipo);
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

                // Obra actual
                String resObra = ApiClient.post("/empleado/obra_actual.php", body.toString());
                JSONObject jsonObra = new JSONObject(resObra);
                if (jsonObra.getBoolean("success")) {
                    String nombre = jsonObra.getJSONObject("obra").getString("nombre");
                    runOnUiThread(() -> tvDireccion.setText(nombre));
                }

                // Estado equipo
                String resEquipo = ApiClient.post("/encargado/estado_equipo.php", body.toString());
                JSONObject jsonEquipo = new JSONObject(resEquipo);
                if (jsonEquipo.getBoolean("success")) {
                    JSONArray arr = jsonEquipo.getJSONArray("equipo");
                    List<EmpleadoEstado> lista = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject e = arr.getJSONObject(i);
                        lista.add(new EmpleadoEstado(
                                e.getInt("id_usuario"),
                                e.getString("nombre"),
                                e.getString("estado") // "activo", "descansando", "ausente"
                        ));
                    }
                    runOnUiThread(() -> {
                        equipo.clear();
                        equipo.addAll(lista);
                        adapterEquipo.notifyDataSetChanged();
                    });
                }

                // Estado propio fichaje
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
                        Toast.makeText(this, json.optString("message", "Error"),
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