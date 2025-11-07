package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eupacienteapplication.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets s = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(s.left, s.top, s.right, s.bottom);
            return insets;
        });

        // pega a logo
        logo = findViewById(R.id.Splash_Logo);

        // pequena espera + animação (logo sobe) → vai pro login
        // (mantive simples, sem libs)
        final long delay = 0;      // 0.2s antes de animar (sensação de “apareceu”)
        final long dur   = 800;      // 0.5s de animação

        logo.postDelayed(() -> {
            float dy = -dp(300); // sobe ~120dp; ajuste se quiser mais alto
            logo.animate()
                    .translationY(dy)
                    .setDuration(dur)
                    .withEndAction(() -> {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    })
                    .start();
        }, delay);
    }

    // helper simples pra dp → px
    private float dp(int value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return value * dm.density;
    }
}
