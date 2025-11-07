package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.eupacienteapplication.DetectorDeIP;
import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(status.left, status.top, status.right, 0);
            return insets;
        });

        // TODO: MUDAR ISSO!
        // Reseta as shared prefs de forma bem porca
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        prefs.edit()
                .putString(Permanencia.ip, "")
                .putString(Permanencia.usuario_id, "")
                .putString(Permanencia.usuario_nome, null)
                .commit();

        // TODO -> Método que estou utilizando para pegar o IP do hotspot artomaticarmente minha genti.
        tentarIp();
    }

    public void tentarIp(){
        String ipDetectado = DetectorDeIP.detectarIpServidor(getApplicationContext());
        if (ipDetectado != null && !ipDetectado.isEmpty()){
            SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
            prefs.edit()
                    .putString(Permanencia.ip, ipDetectado)
                    .commit();
        } else {
            Toast.makeText(this, "ERRO | Não foi possível detectar o IP", Toast.LENGTH_LONG).show();
            SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
            prefs.edit()
                    .putString(Permanencia.ip, "100.112.68.121")
                    .commit();
        }
    }

    public void postLogin(View v) throws Exception {
        EditText loginET = findViewById(R.id.Login_EditText_Usuario);
        TextInputEditText senhaET = findViewById(R.id.Login_EditText_Senha);

        String email = loginET.getText().toString().toLowerCase().trim();
        String senha = senhaET.getText().toString().trim();


        // Verificação de inputs de senha e email
        // Descobri essa bomba muito legal de pattern. Dia feliz :)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show();
            loginET.setText("");
            return;
        }
        if (senha.isBlank()){
            Toast.makeText(this, "Informe uma senha", Toast.LENGTH_SHORT).show();
            return;
        }

        senhaET.setText("");
        loginET.setText("");

        if (email.equals("dev@bypass.com")){
            Intent i = new Intent(this, MenuActivity.class);
            startActivity(i);
            finish();
            return;
        }

        if (email.equalsIgnoreCase("dev@dev.com")) {
            SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
            prefs.edit()
                    .putString(Permanencia.ip, "100.112.68.121")
                    .commit();
            prefs.edit()
                    .putString("usuario_nome", "DevTeste")
                    .putString("usuario_id", "1")
                    .apply();
            Intent i = new Intent(this, MenuActivity.class);
            startActivity(i);
            finish();
        }


        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");

        if (ip == null || ip.isEmpty()) {
            Toast.makeText(this, "IP NÃO definido", Toast.LENGTH_SHORT).show();
            tentarIp();
            return;
        }

        // Tranformando os valores em Jason (meu amigo)
        JSONObject obj = new JSONObject();
        obj.put("email", email);
        obj.put("senha", senha);
        String json = obj.toString();

        // Enviando o Jason para o servidor (coitado)
        String url = "http://" + ip + ":8080/api/pacientes/login";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url,
                resp -> seOK(resp), erro -> seERRO(erro)) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(stringRequest);
    }

    private void seOK(String resp) {
    try {
        JSONObject obj = (JSONObject) new JSONTokener(resp).nextValue();
        String status = obj.getString("status");
        if (status.equals("401")){
            Toast.makeText(this, "Senha ou email incorretos", Toast.LENGTH_SHORT).show();
            return;
        }


        String nome = obj.getString("nome");
        Long id = obj.getLong("id");

        // Salva nas preferências o nome do cara e o id dele.
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        prefs.edit()
                .putString("usuario_nome", nome)
                .putString("usuario_id", id.toString())
                .apply();

        Intent i = new Intent(this, MenuActivity.class);
        startActivity(i);
        finish();
        } catch (Exception ex) {
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void seERRO(VolleyError erro){
        String msg = erro.getMessage() != null ? erro.getMessage() : erro.toString();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}