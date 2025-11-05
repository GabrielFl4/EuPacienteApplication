package com.example.eupacienteapplication.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.adapters.ConsultaListAdapter;
import com.example.eupacienteapplication.entities.Consulta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ConsultasActivity extends AppCompatActivity {
    private ArrayList<Consulta> listaConsultas;
    private ListView listView;
    private ConsultaListAdapter adapter;
    private int posicaoSelecionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_consultas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.Consultar_ListView_Consultas);

        listaConsultas = new java.util.ArrayList<>();
        adapter = new ConsultaListAdapter(this, listaConsultas);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            posicaoSelecionada = position;
            adapter.setPosicaoSelecionada(position);
        });


        //todo : Código do listview e que chama a função no onCreate para buscar do SPRING as consultas marcadas para o usuário com id salvo no sharedPref (usuario_id)
        // Carrega ao abrir
        carregarConsultasDoUsuario();
    }
    public void verAgendar(View v){
        Intent i = new Intent(this, AgendarActivity.class);
        startActivity(i);
        finish();
    }

    public void desmarcar(View v) throws Exception {
        // todo: Função que atualiza a consulta selecionada para pendente, put no spring BACKEND.
        // Se não tiver selecionado nada, pede para selecionar
        if (posicaoSelecionada < 0 || posicaoSelecionada >= listaConsultas.size()) {
            Toast.makeText(this, "Selecione uma consulta", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        //Se alguma consulta selecionada, manda para atualizar o status (put)
        Consulta selecionada = listaConsultas.get(posicaoSelecionada);
        if (selecionada.getId() == null) {
            Toast.makeText(this, "Consulta inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        atualizarStatusParaPendente(selecionada.getId());
    }

    public void voltar(View v){
        finish();
    }

    private void carregarConsultasDoUsuario() {
        //Pega o ID do usuário no sharedPref
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String usuarioId = prefs.getString(Permanencia.usuario_id, "");
        String ip = prefs.getString(Permanencia.ip, "");


        String url = "http://" + ip + ":8080/api/consultas/" + usuarioId;
        RequestQueue requestQueue = Volley.newRequestQueue(this);


        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                resp -> preencherListaAPartirDoJson(resp),
                erro -> {
                    Toast.makeText(this, "Erro ao carregar consultas", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }


    private void preencherListaAPartirDoJson(JSONArray json) {
        listaConsultas.clear();

        for (int i = 0; i < json.length(); i++) {
            JSONObject o = json.optJSONObject(i);
            if (o == null) continue;

            Long id = o.has("id") ? o.optLong("id") : null;

            // data/hora como string ISO
            String dataStr = o.optString("dataHora", null);

            // sintomas e se paga
            String sintomas = o.optString("sintomas", null);
            boolean consultaPaga = o.optBoolean("consultaPaga", false);

            // valor
            BigDecimal valor;
            String valorStr = o.optString("valor", "0");
            try { valor = new BigDecimal(valorStr); } catch (Exception e) { valor = java.math.BigDecimal.ZERO; }

            // cria a consulta
            Consulta c = new Consulta(id, consultaPaga, valor, sintomas, null);

            // guarda a ISO para usar depois na conversão de volta
            c.setDataHoraIso(dataStr);

            // status andamento (enum no backend vira string)
            String stAnd = o.optString("statusAndamento", null);
            c.setStatusAndamento(stAnd);

            // medico (se vem com o json do backend)
            org.json.JSONObject med = o.optJSONObject("medico");
            if (med != null) {
                c.setMedicoNome(med.optString("nome", null));
                // se existir no JSON:
                c.setEspecialidade(med.optString("especialidade", null));
            }

            listaConsultas.add(c);
        }

        posicaoSelecionada = -1;
        adapter.setPosicaoSelecionada(-1);
        adapter.notifyDataSetChanged();
    }

    //Função da internet que formata o horário para algo mais legível ao usuário
    /*private String formatarIsoEmPtBr(String iso) {
        // Aceita "yyyy-MM-ddTHH:mm:ss" ou "yyyy-MM-ddTHH:mm"
        if (iso == null || iso.length() < 16) return iso;

        String yyyy = iso.substring(0, 4);
        String mm   = iso.substring(5, 7);
        String dd   = iso.substring(8, 10);
        String hh   = iso.substring(11, 13);
        String mi   = iso.substring(14, 16);

        return dd + "/" + mm + "/" + yyyy + " " + hh + ":" + mi;
    }*/

    private void atualizarStatusParaPendente(Long consultaId) throws Exception{
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");

        String url = "http://" + ip + ":8080/api/consultas/" + consultaId + "/status";

        JSONObject body = new JSONObject();
        body.put("status", "CANCELADA");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Consulta pendente para desmarcar", Toast.LENGTH_SHORT).show();
                    carregarConsultasDoUsuario();
                },
                error -> {
                    Toast.makeText(this, "Falha ao desmarcar", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }
}