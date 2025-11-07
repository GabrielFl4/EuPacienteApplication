package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.adapters.MedicamentosAdapter;
import com.example.eupacienteapplication.entities.Medicamento;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MedicamentosActivity extends AppCompatActivity {

    private RecyclerView rv;
    private View empty;
    private final List<Medicamento> listaMedicamentos = new ArrayList<>();
    private MedicamentosAdapter adapter;

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

        // lista/adapter
        rv = findViewById(R.id.Medic_RecyclerView);
        empty = findViewById(R.id.Medic_Empty);

        // Passando o adapter para o Recycle e puxando o GET
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentosAdapter(listaMedicamentos, this, receitaId);
        rv.setAdapter(adapter);

        carregarMedicamentos();
    }

    private void carregarMedicamentos(){
        Intent i = getIntent();
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");

        String url = "http://" + ip + ":8080/api/medicamentos/" + i.getLongExtra("receitaId", -1L);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                resp -> preencherListaAPartirDoJson(resp),
                erro -> {
                    Toast.makeText(this, "Erro ao carregar medicamentos", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }

    private void preencherListaAPartirDoJson(JSONArray json){
        listaMedicamentos.clear();

        for(int i = 0; i < json.length(); i++){
            JSONObject object = json.optJSONObject(i);
            if (object == null) continue;

            // Pega o id do object
            Long id = object.has("id") ? object.optLong("id") : null;

            // Pega o nome do object
            String nome = object.has("nome") ? object.optString("nome") : null;

            // Pega a dosagem do object
            String dosagem = object.has("dosagem") ? object.optString("dosagem") : null;

            Integer id_receita = object.has("id_receita") ? (int) object.optLong("id_receita") : 0;

            // Crio o objeto Medicamento
            Medicamento medicamento = new Medicamento();
            medicamento.setId(id);
            medicamento.setNome(nome);
            medicamento.setDosagem(dosagem);
            medicamento.setId_receita(id_receita);

            listaMedicamentos.add(medicamento);
        }

        // atualiza a UI
        adapter.notifyDataSetChanged();
        if (empty != null) empty.setVisibility(listaMedicamentos.isEmpty() ? View.VISIBLE : View.GONE);
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