package com.example.eupacienteapplication.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.eupacienteapplication.Permanencia;
import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.adapters.FinanceiroAdapter;
import com.example.eupacienteapplication.entities.Consulta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FinanceiroActivity extends AppCompatActivity {

    private RecyclerView rv;
    private View empty;
    private SwipeRefreshLayout srl;
    private final List<Consulta> listaConsultas = new ArrayList<>();
    private FinanceiroAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_financeiro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rv   = findViewById(R.id.Financeiro_RecyclerView);
        empty= findViewById(R.id.Financeiro_Empty);
        srl  = findViewById(R.id.Financeiro_SwipeRefresh);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FinanceiroAdapter(
                listaConsultas,
                this,
                (consulta, position) -> pagarConsulta(consulta.getId(), position)
        );
        rv.setAdapter(adapter);

        if (srl != null) {
            srl.setOnRefreshListener(this::carregarConsultas);
        }

        carregarConsultas();
    }

    private void carregarConsultas() {
        if (srl != null) srl.setRefreshing(true);

        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String usuarioId = prefs.getString(Permanencia.usuario_id, "");
        String ip = prefs.getString(Permanencia.ip, "");

        String url = "http://" + ip + ":8080/api/consultas/" + usuarioId;
        RequestQueue rq = Volley.newRequestQueue(this);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                resp -> preencherLista(resp),
                erro -> {
                    Toast.makeText(this, "Erro ao carregar consultas", Toast.LENGTH_SHORT).show();
                    if (srl != null) srl.setRefreshing(false);
                    if (empty != null) empty.setVisibility(View.VISIBLE);
                }
        );
        rq.add(req);
    }

    private void preencherLista(JSONArray arr) {
        listaConsultas.clear();
        Log.d("FIN", "JSON recebido: " + arr.length());

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;

            // status do BD (apenas estes existem: PENDENTE | CONFIRMADA | CANCELADA)
            String status = o.optString("statusAndamento", "");
            if (!"CONFIRMADA".equalsIgnoreCase(status)) continue; // só entra CONFIRMADA no RV

            // id
            Long id = o.has("id") ? o.optLong("id") : null;

            // data TIMESTAMP -> ISO "yyyy-MM-ddTHH:mm"
            String iso = normalizarIsoMinutos(o.optString("dataHora", null));

            // valor (float/double) -> BigDecimal
            BigDecimal valor = null;
            if (!o.isNull("valor")) {
                try {
                    valor = new BigDecimal(String.valueOf(o.optDouble("valor")));
                } catch (Exception ignore) {}
            }

            // pago
            boolean pago = o.optBoolean("consultaPaga", false);


            String sintomas       = o.optString("sintomas", null);


            String medicoNome = null, especialidade = null;
            JSONObject medico = o.optJSONObject("medico");
            if (medico != null) {
                medicoNome    = medico.optString("nome", null);
                especialidade = medico.optString("especialidade", null);
            }

            // monta entidade
            Consulta c = new Consulta();
            c.setId(id);
            c.setDataHoraIso(iso);
            c.setStatusAndamento(status);   // CONFIRMADA
            c.setSintomas(sintomas);
            c.setConsultaPaga(pago);
            if (valor != null) c.setValor(valor);
            c.setMedicoNome(medicoNome);
            c.setEspecialidade(especialidade);

            listaConsultas.add(c);
            Log.d("FIN", "statusAndamento=" + o.optString("statusAndamento","?"));
        }

        adapter.notifyDataSetChanged();
        if (srl != null) srl.setRefreshing(false);
        if (empty != null) empty.setVisibility(listaConsultas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void pagarConsulta(Long consultaId, int position) {
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");

        String url = "http://" + ip + ":8080/api/consultas/" + consultaId + "/pago";
        JSONObject body = new JSONObject();
        try { body.put("consultaPaga", true); } catch (Exception ignore) {}

        // TODO - Revisar
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.PUT, url,
                resp -> {
                    Toast.makeText(this, "Pagamento confirmado", Toast.LENGTH_SHORT).show();
                    adapter.marcarComoPagaLocal(position);
                },
                err -> Toast.makeText(this, "Falha ao pagar", Toast.LENGTH_SHORT).show()
        ) {
            @Override public byte[] getBody() {
                return "true".getBytes(StandardCharsets.UTF_8);
            }
            @Override public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        q.add(req);
    }

    // Converte"yyyy-MM-dd'T'HH:mm:ss" para "yyyy-MM-ddTHH:mm".
    private String normalizarIsoMinutos(String raw) {
        if (raw == null || raw.isEmpty()) return "2001-01-01T00:00"; // Um template para não quebrar visualmente
        String s = raw.trim().replace(' ', 'T');
        if (s.length() >= 16) return s.substring(0, 16); // yyyy-MM-ddTHH:mm
        if (s.length() == 10) return s + "T00:00";
        return s;
    }

    public void voltar(View v){
        finish();
    }
}