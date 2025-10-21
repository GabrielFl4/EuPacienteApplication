package com.example.eupacienteapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // todo: Método para buscar nome de usuário e exibir no TV
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
        Intent i = new Intent(this, FichaActivity.class);
        startActivity(i);
    }

    public void sair(View v){
        finish();
        // todo: Adicionar método para deslogar usuário
    }
}