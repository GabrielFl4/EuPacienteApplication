package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eupacienteapplication.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo;
    private boolean navegou = false;
    private Runnable animAndGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            // mantém o topo; zera o bottom pra não empurrar conteúdo
            v.setPadding(status.left, status.top, status.right, 0);
            return insets;
        });

        logo = findViewById(R.id.Splash_Logo);

        final long delay = 800;
        final long dur   = 800;

        animAndGo = () -> {
            float dy = -dp(240);
            logo.animate()
                    .translationY(dy)
                    .setDuration(dur)
                    .withEndAction(this::goLoginOnce)
                    .start();
        };

        logo.postDelayed(animAndGo, delay);
    }

    private void goLoginOnce() {
        if (navegou) return;
        navegou = true;
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logo != null && animAndGo != null) {
            logo.removeCallbacks(animAndGo);
            logo.animate().cancel();
        }
    }

    private float dp(int value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return value * dm.density;
    }
}
