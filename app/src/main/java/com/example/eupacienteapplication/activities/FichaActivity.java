package com.example.eupacienteapplication.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FichaActivity extends AppCompatActivity {

    private TextView tvNome, tvEmail, tvData;
    private EditText etTelefone, etComplemento;
    private MaterialButton btnAtualizar;  // <-- era Button
    private ProgressBar progress;
    private String usuarioId;
    private String ip;
    private static final int COMP_MAX = 250;
    private TextView tvComplementoCount;
    private String originalTel = "";
    private String originalComp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ficha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvNome = findViewById(R.id.tvNome);
        tvEmail = findViewById(R.id.tvEmail);
        tvData = findViewById(R.id.tvData);
        etTelefone = findViewById(R.id.etTelefone);
        etComplemento = findViewById(R.id.etComplemento);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        progress = findViewById(R.id.progress);
        tvComplementoCount = findViewById(R.id.tvComplementoCount);

        etComplemento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // atualiza contador e lógica do botão
                updateComplementoCount(s.length());
                avaliarMudanca();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        usuarioId = prefs.getString(Permanencia.usuario_id, "");
        ip = prefs.getString(Permanencia.ip, "");

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { avaliarMudanca(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etTelefone.addTextChangedListener(watcher);
        etComplemento.addTextChangedListener(watcher);

        // estado inicial do botão
        setModoVoltar();

        carregarFicha();
    }

    private void carregarFicha(){
        String url = "http://" + ip + ":8080/api/pacientes/" + usuarioId;

        setLoading(true);
        RequestQueue rq = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    preencherCamposAPartirDoJson(resp);
                    setLoading(false);
                },
                err -> {
                    Toast.makeText(this, "Erro ao carregar ficha", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                }
        );
        rq.add(req);
    }

    private void preencherCamposAPartirDoJson(JSONObject o){
        String nome = o.optString("nome", "");
        String email = o.optString("email", "");
        String dataIso = o.optString("data_nasc", "");
        String tel = o.optString("telefone", "");
        String comp = o.optString("complemento", "");

        tvNome.setText(nome);
        tvEmail.setText(email);
        tvData.setText(isoToBr(dataIso));
        etTelefone.setText(tel);
        etComplemento.setText(comp);

        originalTel = tel == null ? "" : tel.trim();
        originalComp = comp == null ? "" : comp.trim();
        updateComplementoCount(etComplemento.getText().length());
        avaliarMudanca(); // garante o estado certo na primeira carregada
    }

    private void atualizar(){
        // simples validação do limite
        String comp = etComplemento.getText().toString();
        if (comp.length() > 250){
            Toast.makeText(this, "Complemento deve ter no máximo 250 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://" + ip + ":8080/api/pacientes/" + usuarioId;

        JSONObject body = new JSONObject();
        try {
            body.put("telefone", etTelefone.getText().toString());
            body.put("complemento", comp);
        } catch (JSONException ignored) {}

        setLoading(true);
        RequestQueue rq = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                resp -> {
                    Toast.makeText(this, "Atualizado com sucesso", Toast.LENGTH_SHORT).show();
                    originalTel = etTelefone.getText().toString().trim();
                    originalComp = etComplemento.getText().toString().trim();
                    setModoVoltar();
                    setLoading(false);
                },
                err -> {
                    Toast.makeText(this, "Não foi possível atualizar", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                }
        );
        rq.add(req);
    }

    private void setLoading(boolean l){
        progress.setVisibility(l ? View.VISIBLE : View.GONE);
        btnAtualizar.setEnabled(!l);
        etTelefone.setEnabled(!l);
        etComplemento.setEnabled(!l);
    }

    private String isoToBr(String iso){
        try {
            // entrada: yyyy-MM-dd (do Spring vem assim)
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            // saída: dd/MM/yyyy Formatinho cheiroso para mostrar na ficha
            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            return out.format(in.parse(iso));
        } catch (Exception e){
            return iso;
        }
    }

    private void avaliarMudanca(){
        String t = etTelefone.getText().toString().trim();
        String c = etComplemento.getText().toString().trim();
        boolean mudou = (!t.equals(originalTel)) || (!c.equals(originalComp));
        boolean dentroLimite = c.length() <= 250;
        if (mudou && dentroLimite) setModoAtualizar(); else setModoVoltar();
    }

    private void setModoVoltar(){
        btnAtualizar.setEnabled(true);
        btnAtualizar.setText("Voltar");
        btnAtualizar.setIconResource(R.drawable.ic_arrow_back_24);
        btnAtualizar.setTextColor(ContextCompat.getColor(this, R.color.rc_blue_primary));
        btnAtualizar.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.rc_blue_container)));
        btnAtualizar.setOnClickListener(v -> finish());
    }

    private void setModoAtualizar(){
        btnAtualizar.setEnabled(true);
        btnAtualizar.setText("Atualizar");
        btnAtualizar.setIconResource(R.drawable.ic_check_24);
        btnAtualizar.setTextColor(ContextCompat.getColor(this, R.color.rc_update_primary));
        btnAtualizar.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.rc_update_container)));
        btnAtualizar.setOnClickListener(v -> atualizar());
    }

    private void updateComplementoCount(int len){
        if (tvComplementoCount != null){
            tvComplementoCount.setText(len + "/" + COMP_MAX);
        }
    }

    public void voltar(View v){
        finish();
    }
}
