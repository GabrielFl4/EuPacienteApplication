package com.example.eupacienteapplication.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
import com.example.eupacienteapplication.adapters.ReceitasAdapter;
import com.example.eupacienteapplication.entities.Medicamento;
import com.example.eupacienteapplication.entities.Medico;
import com.example.eupacienteapplication.entities.Receita;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReceitasActivity extends AppCompatActivity {

    private RecyclerView rv;
    private View empty;
    private final List<Receita> listaReceitas = new ArrayList<>();
    private ReceitasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receitas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rv = findViewById(R.id.Receitas_RecyclerView);
        empty = findViewById(R.id.Receitas_Empty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReceitasAdapter(listaReceitas, this);
        rv.setAdapter(adapter);

        carregarReceitas();
    }

    private void carregarReceitas(){
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String usuarioId = prefs.getString(Permanencia.usuario_id, "");
        String ip = prefs.getString(Permanencia.ip, "");

        String url = "http://" + ip + ":8080/api/receitas/" + usuarioId;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                resp -> preencherListaAPartirDoJson(resp),
                erro -> {
                    Toast.makeText(this, "Erro ao carregar receitas", Toast.LENGTH_SHORT).show();
                    empty.setVisibility(View.VISIBLE);
                }
        );
        requestQueue.add(req);
    }

    private void preencherListaAPartirDoJson(JSONArray json) {
        // limpa a lista antes de popular
        listaReceitas.clear();

        for (int i = 0; i < json.length(); i++) {
            JSONObject o = json.optJSONObject(i);
            if (o == null) continue;

            // id da receita
            Long id = o.has("id") ? o.optLong("id") : null;

            // data como string ISO (yyyy-MM-dd)
            String dataStr = o.optString("data", null);

            // vínculos (paciente e médico)
            Integer idPaciente = o.has("id_paciente") ? (int )o.optLong("id_paciente") : null;
            Integer idMedico = (o.has("id_medico") && !o.isNull("id_medico")) ? o.optInt("id_medico") : null;

            // médico (se vier no JSON)
            Medico medObj = null;
            String medicoNome = null;
            String medicoEspecialidade = null;
            JSONObject medJson = o.optJSONObject("medico");
            if (medJson != null) {
                String nome = medJson.optString("nome", null);
                String esp  = medJson.optString("especialidade", null);
                medObj = new Medico(nome, esp);
                medicoEspecialidade = esp;
                medicoNome = nome;
            }

            // medicamentos (se vierem no JSON)
            ArrayList<Medicamento> meds = new ArrayList<>();
            JSONArray medsArr = o.optJSONArray("medicamentos");
            if (medsArr != null) {
                for (int j = 0; j < medsArr.length(); j++) {
                    JSONObject mj = medsArr.optJSONObject(j);
                    if (mj == null) continue;

                    Long medId = mj.has("id") ? mj.optLong("id") : null;
                    String nome = mj.optString("nome", null);
                    String dos  = mj.optString("dosagem", null);
                    Integer idRec = mj.has("id_receita") ? mj.optInt("id_receita") : null;

                    Medicamento m = new Medicamento();
                    m.setId(medId);
                    m.setNome(nome);
                    m.setDosagem(dos);
                    if (idRec != null) m.setId_receita(idRec);
                    meds.add(m);
                }
            }

            // monta a receita
            Receita r = new Receita();
            r.setId(id);
            r.setData(dataStr);
            if (idPaciente != null) r.setId_paciente(idPaciente);
            if (idMedico != null)   r.setId_medico(idMedico);
            if (medObj != null)     r.setMedico(medObj);
            r.setMedicamentos(meds);
            r.setMedicoEspecialidade(medicoEspecialidade);
            r.setMedicoNome(medicoNome);

            // adiciona na lista final
            listaReceitas.add(r);
        }

        // atualiza a UI
        adapter.notifyDataSetChanged();
        if (empty != null) empty.setVisibility(listaReceitas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void voltar(View v){
        finish();
    }
}