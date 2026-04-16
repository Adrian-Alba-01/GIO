package com.examp.gio.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.examp.gio.R;

public class PerfilActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tu_layout);

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }
}
