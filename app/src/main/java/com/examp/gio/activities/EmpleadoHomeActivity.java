package com.examp.gio.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.adapters.TareaAdapter;
import com.examp.gio.models.Tarea;
import com.examp.gio.network.ApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmpleadoHomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    TextView tvBienvenido, tvDireccion, tvReloj, tvEstadoFichaje;

    ImageView ivNotificaciones,
            ivFotos,
            ivUbicacion,
            ivPerfil;

    ImageView btnFichar;
    Button btnHorasMensuales,
            btnVerTodas;

    RecyclerView rvTareaActual,
            rvTareasPendientes;

    LinearLayout llFichaje;

    TareaAdapter adapterActual,
            adapterPendientes;

    List<Tarea> tareasActuales = new ArrayList<>();
    List<Tarea> tareasPendientes = new ArrayList<>();

    ExecutorService executor = Executors.newSingleThreadExecutor();

    int idUsuario;
    String nombreUsuario;

    boolean fichado = false;
    boolean pausaActiva = false;

    // =========================
    // CRONÓMETRO
    // =========================

    private long tiempoInicio = 0;
    private long tiempoPausa = 0;
    private long tiempoAcumulado = 0;

    private Handler handlerCronometro = new Handler(Looper.getMainLooper());
    private Runnable runnableCronometro;

    // =========================
    // GPS
    // =========================

    private FusedLocationProviderClient fusedLocationClient;

    private double latitudActual = 0;
    private double longitudActual = 0;

    private double obraLatitud = 0;
    private double obraLongitud = 0;

    private int radioPermitido = 100;

    // =========================
    // AUTO GEOLOCALIZACIÓN
    // =========================

    private Handler handlerUbicacion = new Handler(Looper.getMainLooper());
    private Runnable runnableUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empleado_home);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", 0);
        nombreUsuario = prefs.getString("nombre", "Usuario");

        initViews();
        setupCronometro();
        solicitarPermisosUbicacion();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cargarDatos();

        btnFichar.setOnClickListener(v -> fichar());

        btnHorasMensuales.setOnClickListener(v ->
                startActivity(new Intent(this, HorasMensualesActivity.class))
        );

        btnVerTodas.setOnClickListener(v ->
                startActivity(new Intent(this, TareasActivity.class))
        );

        // =========================
        // BOTÓN PAUSA
        // =========================

        ivFotos.setOnClickListener(v -> {
            if (!fichado) {
                Toast.makeText(this, "Debes fichar primero", Toast.LENGTH_SHORT).show();
                return;
            }
            pausarOReanudar();
        });

        // =========================
        // BOTTOM NAV
        // =========================

        findViewById(R.id.navInicio).setOnClickListener(v -> {});

        findViewById(R.id.navUbicacion).setOnClickListener(v ->
                startActivity(new Intent(this, ObrasActivity.class))
        );

        findViewById(R.id.navTareas).setOnClickListener(v ->
                startActivity(new Intent(this, TareasActivity.class))
        );

        findViewById(R.id.navPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class))
        );
    }

    // ==========================================
    // INIT VIEWS
    // ==========================================

    private void initViews() {

        tvBienvenido      = findViewById(R.id.tvBienvenido);
        tvDireccion       = findViewById(R.id.tvDireccion);
        tvReloj           = findViewById(R.id.tvReloj);
        tvEstadoFichaje   = findViewById(R.id.tvEstadoFichaje);
        btnFichar         = findViewById(R.id.btnFichar);
        btnHorasMensuales = findViewById(R.id.btnHorasMensuales);
        btnVerTodas       = findViewById(R.id.btnVerTodas);
        rvTareaActual     = findViewById(R.id.rvTareaActual);
        rvTareasPendientes= findViewById(R.id.rvTareasPendientes);
        ivFotos           = findViewById(R.id.ivFotos);

        tvBienvenido.setText("¡Bienvenido " + nombreUsuario + "!");

        adapterActual = new TareaAdapter(tareasActuales, false);
        rvTareaActual.setLayoutManager(new LinearLayoutManager(this));
        rvTareaActual.setAdapter(adapterActual);

        adapterPendientes = new TareaAdapter(tareasPendientes, false);
        rvTareasPendientes.setLayoutManager(new LinearLayoutManager(this));
        rvTareasPendientes.setAdapter(adapterPendientes);

        adapterActual.setOnCompletarListener((idTarea, position) ->
                completarTarea(idTarea, position, tareasActuales, adapterActual));

        adapterPendientes.setOnCompletarListener((idTarea, position) ->
                completarTarea(idTarea, position, tareasPendientes, adapterPendientes));
    }

    // ==========================================
    // CRONÓMETRO
    // ==========================================

    private void setupCronometro() {

        runnableCronometro = new Runnable() {
            @Override
            public void run() {
                if (fichado && !pausaActiva) {
                    long tiempoActual = System.currentTimeMillis();
                    long tiempoTranscurrido = tiempoAcumulado + (tiempoActual - tiempoInicio);

                    int segundos = (int) (tiempoTranscurrido / 1000);
                    int horas    = segundos / 3600;
                    int minutos  = (segundos % 3600) / 60;
                    int seg      = segundos % 60;

                    tvReloj.setText(String.format(
                            Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, seg
                    ));
                }
                handlerCronometro.postDelayed(this, 1000);
            }
        };

        handlerCronometro.post(runnableCronometro);
    }

    // ==========================================
    // CARGAR DATOS
    // ==========================================

    private void cargarDatos() {

        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_usuario", idUsuario);

                // =========================
                // OBRA ACTUAL
                // =========================

                String resObra = ApiClient.post("/shared/obra_actual.php", body.toString());
                System.out.println("RESPUESTA OBRA RAW: [" + resObra + "]");

                JSONObject jsonObra = new JSONObject(resObra);

                if (jsonObra.optBoolean("success")) {

                    JSONObject obra = jsonObra.getJSONObject("obra");

                    String nombreObra = obra.optString("nombre", "Sin obra");
                    String direccion  = obra.optString("direccion", "Sin dirección");
                    if (direccion.isEmpty()) direccion = "Sin dirección";

                    obraLatitud    = obra.optDouble("latitud", 0);
                    obraLongitud   = obra.optDouble("longitud", 0);
                    radioPermitido = obra.optInt("radio_permitido", 100);

                    final String textoObra = nombreObra + " - " + direccion;

                    runOnUiThread(() -> tvDireccion.setText(textoObra));

                } else {
                    String msg = jsonObra.optString("message", "Error cargando obra");
                    runOnUiThread(() -> tvDireccion.setText(msg));
                }

                // =========================
                // TAREAS
                // =========================

                String resTareas = ApiClient.post("/empleado/mis_tareas.php", body.toString());
                JSONObject jsonTareas = new JSONObject(resTareas);

                if (jsonTareas.getBoolean("success")) {

                    JSONArray todas = jsonTareas.getJSONArray("tareas");

                    List<Tarea> actuales   = new ArrayList<>();
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

                // =========================
                // ESTADO FICHAJE
                // =========================

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

    // ==========================================
    // FICHAR — corregido: lógica dentro del callback GPS
    // ==========================================

    @SuppressLint("MissingPermission")
    private void fichar() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Sin permisos de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                latitudActual  = location.getLatitude();
                longitudActual = location.getLongitude();
            }

            if (!estaDentroDeLaObra()) {
                Toast.makeText(this,
                        "No estás dentro del perímetro permitido",
                        Toast.LENGTH_LONG).show();
                return;
            }

            executor.execute(() -> {
                try {
                    JSONObject body = new JSONObject();
                    body.put("id_usuario", idUsuario);
                    body.put("latitud", latitudActual);
                    body.put("longitud", longitudActual);

                    String endpoint = fichado
                            ? "/empleado/salida.php"
                            : "/empleado/entrada.php";

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
                            Toast.makeText(this,
                                    json.optString("message", "Error al fichar"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    // ==========================================
    // PAUSAR / REANUDAR
    // ==========================================

    private void pausarOReanudar() {

        if (!pausaActiva) {
            tiempoPausa = System.currentTimeMillis();
            pausaActiva = true;
            ivFotos.setImageResource(android.R.drawable.ic_media_play);
            Toast.makeText(this, "Contador pausado", Toast.LENGTH_SHORT).show();
        } else {
            long tiempoReanudado = System.currentTimeMillis();
            tiempoInicio += (tiempoReanudado - tiempoPausa);
            pausaActiva = false;
            ivFotos.setImageResource(android.R.drawable.ic_media_pause);
            Toast.makeText(this, "Contador reanudado", Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // ACTUALIZAR UI FICHAJE
    // ==========================================

    private void actualizarEstadoFichaje(boolean activo) {

        fichado = activo;

        tvEstadoFichaje.setText(activo ? "● Activo" : "● Inactivo");
        tvEstadoFichaje.setTextColor(getResources().getColor(
                activo ? R.color.verde : R.color.amarillo, null
        ));

        if (activo) {
            // Rojo suave = "puedo salir"
            btnFichar.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            btnFichar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#CC2222")
                )
            );
            btnFichar.setColorFilter(android.graphics.Color.WHITE);
        } else {
            // Azul = "puedo entrar"
            btnFichar.setImageResource(android.R.drawable.ic_menu_directions);
            btnFichar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#FFFFFF")
                )
            );
            btnFichar.setColorFilter(android.graphics.Color.parseColor("#013276"));
        }
    }

    // ==========================================
    // VALIDAR DISTANCIA
    // ==========================================

    private boolean estaDentroDeLaObra() {

        float[] resultados = new float[1];

        Location.distanceBetween(
                latitudActual, longitudActual,
                obraLatitud, obraLongitud,
                resultados
        );

        return resultados[0] <= radioPermitido;
    }

    // ==========================================
    // PERMISOS
    // ==========================================

    private void solicitarPermisosUbicacion() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos, ya se usarán al fichar
            }
        }
    }

    // ==========================================
    // DESTROY
    // ==========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerCronometro.removeCallbacks(runnableCronometro);
        if (runnableUbicacion != null) {
            handlerUbicacion.removeCallbacks(runnableUbicacion);
        }
    }
    private void completarTarea(int idTarea, int position,
                                List<Tarea> lista, TareaAdapter adapter) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id_tarea", idTarea);
                body.put("id_usuario", idUsuario);

                String res = ApiClient.post("/empleado/completar_tarea.php", body.toString());
                JSONObject json = new JSONObject(res);

                runOnUiThread(() -> {
                    if (json.optBoolean("success")) {
                        lista.get(position).estado = "completada";
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Tarea completada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                json.optString("message", "Error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}