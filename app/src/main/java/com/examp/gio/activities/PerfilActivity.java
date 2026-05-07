package com.examp.gio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.examp.gio.R;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNombre, tvEmail, tvRol;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvRol = findViewById(R.id.tvRol);
        btnLogout = findViewById(R.id.btnLogout);

        // Botón atrás
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }

        // Obtener datos de sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);

        String nombre = prefs.getString("nombre", "");
        String email = prefs.getString("email", "");
        String rol = prefs.getString("rol", "");

        // Mostrar datos
        tvNombre.setText(nombre);
        tvEmail.setText(email);
        tvRol.setText("Rol: " + rol);

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {

        // Firebase logout (Google)
        FirebaseAuth.getInstance().signOut();

        // Borrar SharedPreferences
        getSharedPreferences("sesion", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Ir al login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}