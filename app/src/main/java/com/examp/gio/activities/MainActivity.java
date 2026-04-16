package com.examp.gio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String rol = prefs.getString("rol", "empleado");

        Intent intent;
        switch (rol) {
            case "encargado":
                intent = new Intent(this, EncargadoHomeActivity.class);
                break;
            case "jefe":
            case "admin":
                // Por ahora redirigir a encargado hasta implementar esas vistas
                intent = new Intent(this, EncargadoHomeActivity.class);
                break;
            default:
                intent = new Intent(this, EmpleadoHomeActivity.class);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}