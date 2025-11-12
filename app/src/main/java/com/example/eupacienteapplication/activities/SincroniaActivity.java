package com.example.eupacienteapplication.activities;

import static android.content.Context.AUDIO_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.util.SyncSseClient;
import com.google.android.material.button.MaterialButton;

public class SincroniaActivity extends AppCompatActivity {

    private static final String TOPIC = "apresentacao";

    private SyncSseClient sse;
    private volatile boolean isActive = false; // <- só reage em FG

    // UI
    private TextView statusTxt;
    private FrameLayout okOverlay;
    private View successCircle;
    private ImageView successIcon;
    private Animation popIn, fadeOut;

    // Áudio
    private SoundPool soundPool;
    private int soundOkId = 0;
    private AudioManager audioManager;

    private long lastSyncMs = 0;
    private boolean dropBurst() {
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastSyncMs < 400) return true;
        lastSyncMs = now;
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronia);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(status.left, status.top, status.right, 0);
            return insets;
        });

        MaterialButton btVoltar = findViewById(R.id.Sincronia_Button_Voltar);
        btVoltar.setOnClickListener(v -> {
            if (sse != null) sse.stop();
            finish();
        });

        statusTxt = findViewById(R.id.sync_status_text);
        okOverlay = findViewById(R.id.sync_ok_overlay);
        successCircle = findViewById(R.id.success_circle);
        successIcon = findViewById(R.id.success_icon);

        popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // URL do SSE (pegando IP salvo, como no resto do app)
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");

        // gera e persiste clientId
        String clientId = prefs.getString("SYNC_CLIENT_ID", null);
        if (clientId == null || clientId.isEmpty()) {
            clientId = java.util.UUID.randomUUID().toString();
            prefs.edit().putString("SYNC_CLIENT_ID", clientId).apply();
        }

        String url = "http://" + ip + ":8080/sync/subscribe/" + TOPIC + "?clientId=" + clientId;
        sse = new SyncSseClient(url, (event, data) -> runOnUiThread(() -> {
            if (!isActive) return;
            if ("connected".equals(event)) {
                statusTxt.setText("Conectado");
            } else if ("sync".equals(event)) {
                if (dropBurst()) return;
                tocarSom();
                mostrarOkRapido();
            }
        }));


        sse = new SyncSseClient(url, (event, data) -> runOnUiThread(() -> {
            // Se a tela não está ativa (FG), ignora o evento
            if (!isActive) return;

            if ("connected".equals(event)) {
                if (statusTxt != null) statusTxt.setText("Conectado");
            } else if ("sync".equals(event)) {
                tocarSom();
                mostrarOkRapido();
            }
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;

        // (Re)inicia áudio aqui (só quando tela ativa)
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build();
        soundOkId = soundPool.load(this, R.raw.ding, 1);

        // Conecta SSE
        if (sse != null) sse.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;

        // Para SSE e não reconecta
        if (sse != null) sse.stop();

        // Libera áudio
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundOkId = 0;
    }

    private void tocarSom() {
        if (!isActive || soundPool == null || soundOkId == 0) return;

        if (audioManager != null) {
            audioManager.requestAudioFocus(
                    null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            );
        }

        soundPool.play(soundOkId, 1f, 1f, 1, 0, 1f);
    }

    private void mostrarOkRapido() {
        if (!isActive || okOverlay == null) return;

        okOverlay.setVisibility(View.VISIBLE);
        okOverlay.clearAnimation();
        okOverlay.startAnimation(popIn);

        okOverlay.animate().scaleX(1.03f).scaleY(1.03f).setDuration(140).withEndAction(() ->
                okOverlay.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        ).start();

        okOverlay.postDelayed(() -> {
            okOverlay.startAnimation(fadeOut);
            okOverlay.postDelayed(() -> {
                if (okOverlay != null) okOverlay.setVisibility(View.GONE);
            }, 200);
        }, 800);
    }
}