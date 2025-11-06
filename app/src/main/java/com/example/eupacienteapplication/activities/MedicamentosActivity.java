package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eupacienteapplication.R;

public class MedicamentosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicamentos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tituloTv = findViewById(R.id.Medic_TextView_Titulo);
        TextView dataTv   = findViewById(R.id.Medic_TextView_Data);

        // extras
        Intent it = getIntent();
        long receitaId       = it.getLongExtra("receitaId", -1L);
        String medicoTitulo  = it.getStringExtra("medicoTitulo");
        String dataIso       = it.getStringExtra("dataIso");

        // aplica no cabeçalho
        if (medicoTitulo != null && !medicoTitulo.isEmpty()) {
            tituloTv.setText(medicoTitulo);
        } else {
            tituloTv.setText("Receita");
        }

        if (dataIso != null && dataIso.length() >= 10) {
            dataTv.setText("| " + isoToBr(dataIso) + " |");
        } else {
            dataTv.setText("");
        }
    }

    public void voltar(View v){
        finish();
    }











    private String isoToBr(String iso) {
        try {
            String y = iso.substring(0, 4);
            String m = iso.substring(5, 7);
            String d = iso.substring(8, 10);
            return d + "/" + m + "/" + y;
        } catch (Exception e) {
            return iso; // se algo vier fora do padrão, mostra como veio
        }
    }
}