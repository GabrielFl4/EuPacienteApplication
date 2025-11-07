package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(status.left, status.top, status.right, 0);
            return insets;
        });

        // Método para buscar nome de usuário e exibir no TV
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String nome_usuario = prefs.getString(Permanencia.usuario_nome, "");


        nome_usuario = (nome_usuario.split(" ")[0]);
        TextView tituloTV = findViewById(R.id.Menu_TextView_BemVindo);
        String concatenada = "Bem vindo, " + nome_usuario;
        tituloTV.setText(concatenada);

    }

    public void verConsultas(View v){
        Intent i = new Intent(this, ConsultasActivity.class);
        startActivity(i);
    }

    public void verReceitas(View v){
        Intent i = new Intent(this, ReceitasActivity.class);
        startActivity(i);
    }

    public void verFicha(View v){
        Intent i = new Intent(this, FichaActivity.class);
        startActivity(i);
    }

    public void financeiro(View v){
        Intent i = new Intent(this, FinanceiroActivity.class);
        startActivity(i);
    }

    public void sair(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
        // todo: Adicionar método para deslogar usuário
    }
}