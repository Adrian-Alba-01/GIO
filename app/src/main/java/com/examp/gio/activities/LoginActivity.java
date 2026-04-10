package com.examp.gio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.examp.gio.R;
import com.examp.gio.network.ApiClient;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoogle;
    ProgressBar progressBar;
    TextView tvError, tvForgot;

    // Google Sign-In
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;

    ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    autenticarConFirebase(account.getIdToken());
                } catch (ApiException e) {
                    mostrarError("Error con Google: " + e.getStatusCode());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnGoogle   = findViewById(R.id.btnGoogle);
        progressBar = findViewById(R.id.progressBar);
        tvError     = findViewById(R.id.tvError);
        tvForgot    = findViewById(R.id.tvForgot);

        firebaseAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Saltar login si ya hay sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        if (prefs.contains("id_usuario")) {
            irAMain(prefs.getString("rol", "empleado"));
            return;
        }

        // Login normal
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                mostrarError("Completa todos los campos");
                return;
            }
            setLoading(true);
            hacerLogin(email, pass);
        });

        // Login con Google
        btnGoogle.setOnClickListener(v -> {
            setLoading(true);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Recuperación próximamente", Toast.LENGTH_SHORT).show()
        );
    }

    // ── Login normal ──────────────────────────────────────────
    private void hacerLogin(String email, String contrasena) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            JSONObject respuesta = null;
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("contrasena", contrasena);
                String result = ApiClient.post("/auth/login.php", body.toString());
                respuesta = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject finalRespuesta = respuesta;
            runOnUiThread(() -> procesarRespuesta(finalRespuesta));
        });
    }

    private void procesarRespuesta(JSONObject respuesta) {
        setLoading(false);
        if (respuesta == null) {
            mostrarError("No se pudo conectar con el servidor");
            return;
        }
        try {
            if (respuesta.getBoolean("success")) {
                JSONObject usuario = respuesta.getJSONObject("usuario");
                guardarSesion(
                        usuario.getInt("id_usuario"),
                        usuario.getString("nombre"),
                        usuario.getString("email"),
                        usuario.getString("rol")
                );
                irAMain(usuario.getString("rol"));
            } else {
                mostrarError(respuesta.getString("message"));
            }
        } catch (Exception e) {
            mostrarError("Error inesperado");
        }
    }

    // ── Login con Google ──────────────────────────────────────
    private void autenticarConFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Registrar/buscar usuario en tu BD por email
                            buscarUsuarioGoogle(user.getEmail(), user.getDisplayName());
                        }
                    } else {
                        setLoading(false);
                        mostrarError("Fallo autenticación Google");
                    }
                });
    }

    private void buscarUsuarioGoogle(String email, String nombre) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            JSONObject respuesta = null;
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("nombre", nombre);
                // Endpoint que busca o crea el usuario por email de Google
                String result = ApiClient.post("/auth/login_google.php", body.toString());
                respuesta = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject finalRespuesta = respuesta;
            runOnUiThread(() -> procesarRespuesta(finalRespuesta));
        });
    }

    // ── Helpers ───────────────────────────────────────────────
    private void guardarSesion(int id, String nombre, String email, String rol) {
        getSharedPreferences("sesion", MODE_PRIVATE).edit()
                .putInt("id_usuario", id)
                .putString("nombre", nombre)
                .putString("email", email)
                .putString("rol", rol)
                .apply();
    }

    private void irAMain(String rol) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("rol", rol);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void mostrarError(String mensaje) {
        setLoading(false);
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnGoogle.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvError.setVisibility(View.GONE);
    }
}