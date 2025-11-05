package com.example.eupacienteapplication.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.eupacienteapplication.entities.Medico;
import com.example.eupacienteapplication.util.Calendario;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class AgendarActivity extends AppCompatActivity {

    private TextInputEditText etData;
    private MaterialAutoCompleteTextView acHora;

    private ArrayList<Medico> listaMedicos = new ArrayList<>();

    // Zona do Timezone e estado
    private final TimeZone ZONE = Calendario.tzBr();
    private Calendar selectedDate;

    // Pool fixa de horários das consultas
    private final String[] SLOT_POOL = new String[]{
            "08:00", "08:45", "09:30", "10:15", "11:00", "11:45",
            "13:30", "14:15", "15:00", "15:45", "16:30", "17:15"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agendar);

        View main = findViewById(R.id.main);
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        // GET de médicos
        puxarMedicos();

        MaterialAutoCompleteTextView mactvRotina = findViewById(R.id.Agendar_AutoComplete_Rotina);
        etData = findViewById(R.id.Agendar_TextInputEditText_Data);
        acHora = findViewById(R.id.Agendar_AutoComplete_Hora);


        // Estado inicial (apenas para filtrar slots de hoje; não preenche o campo visual)
        selectedDate = Calendario.startOfToday(ZONE);
        atualizarHorarios(selectedDate);

        // Adapter da rotina
        String[] opcoes = new String[]{"Consulta Inicial", "Retorno", "Exames"};
        ArrayAdapter<String> adapterRotina = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opcoes);
        mactvRotina.setAdapter(adapterRotina);
        mactvRotina.setText("", false);
    }

    public void enviarConsulta(View v) throws Exception {
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");
        String id_paciente = prefs.getString(Permanencia.usuario_id, "");


        MaterialAutoCompleteTextView mactvRotina = findViewById(R.id.Agendar_AutoComplete_Rotina);
        TextInputEditText tietSintomas = findViewById(R.id.Agendar_TextInputEditText_Sintomas);
        MaterialAutoCompleteTextView mactvMedico = findViewById(R.id.Agendar_AutoComplete_Medico);

        // Verifica data e Hora
        String dataHoraIso = obterDataHoraIsoSelecionada();
        if (dataHoraIso == null || dataHoraIso.isBlank()){
            Toast.makeText(this, "Selecione data e horário", Toast.LENGTH_SHORT).show();
            return;
        }

        String rotina = (mactvRotina != null && mactvRotina.getText() != null) ? mactvRotina.getText().toString().trim() : "";
            String saida;
            switch (rotina) {
                case "Consulta Inicial":
                    rotina = "CONSULTA_INICIAL";
                    break;
                case "Retorno":
                    rotina = "RETORNO";
                    break;
                case "Exames":
                    rotina = "EXAMES";
                    break;
                default:
                    Toast.makeText(this, "Selecione o tipo de rotina", Toast.LENGTH_SHORT).show();
                    return;
            }


        String sintomas = (tietSintomas != null && tietSintomas.getText() != null) ? tietSintomas.getText().toString().trim() : "";
        if (sintomas.isBlank()) {
            Toast.makeText(this, "Descreva algum sintomas.", Toast.LENGTH_SHORT).show();
            return;
        }


        Long id_medico = obterIdMedicoSelecionado(mactvMedico);
        if (id_medico == null){
            Toast.makeText(this, "Selecione uma especialidade", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://" + ip + ":8080/api/consultas/";
        JSONObject body = new JSONObject();
        // Corpo da consulta que tenho que enviar.
        body.put("dataHora", dataHoraIso);
        body.put("statusMotivo", rotina);
        body.put("sintomas", sintomas);
        body.put("id_paciente", id_paciente);
        body.put("id_medico", id_medico);


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Consulta pendente de aprovação", Toast.LENGTH_SHORT).show();
                    finalizar();
                    },
                error -> {
                    int code = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    String msg;
                    if (code == 409) {
                        msg = "Já existe uma consulta nesse horário com esse médico.";
                    } else if (code > 0) {
                        msg = "Falha ao agendar (HTTP " + code + ").";
                    } else {
                        msg = "Falha de rede ao agendar.";
                    }

                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
                );
            requestQueue.add(req);
    }



    private void finalizar(){
        // Função para limpar completamente os dados inseridos na página
        AutoCompleteTextView actvRotina = findViewById(R.id.Agendar_AutoComplete_Rotina);
        TextInputEditText tietSintomas = findViewById(R.id.Agendar_TextInputEditText_Sintomas);
        acHora.setText("");
        etData.setText("");
        actvRotina.setText("");
        tietSintomas.setText("");

        Intent i = new Intent(this, ConsultasActivity.class);
        startActivity(i);

        finish();
    }

    private void puxarMedicos(){
        SharedPreferences prefs = getSharedPreferences(Permanencia.arquivo, MODE_PRIVATE);
        String ip = prefs.getString(Permanencia.ip, "");

        String url = "http://" + ip + ":8080/api/medicos";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> preencherListaMedicos(response),
                error -> {
                    Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }

    private void preencherListaMedicos(JSONArray json){
        MaterialAutoCompleteTextView mactvMedico = findViewById(R.id.Agendar_AutoComplete_Medico);
        listaMedicos.clear();

        for (int i = 0; i < json.length(); i++) {
            JSONObject obj = json.optJSONObject(i);
            if (obj == null) continue;

            Long id = obj.has("id") ? obj.optLong("id") : null;

            String nome = obj.optString("nome", null);

            String especialidade = obj.optString("especialidade", null);

            Medico med = new Medico(id, nome, especialidade);
            listaMedicos.add(med);
        }

        // Preenche o adapter de médicos
        ArrayList<String> titulos = gerarStringMedicos();
        ArrayAdapter<String> adapterMedicos = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titulos);
        mactvMedico.setAdapter(adapterMedicos);
        mactvMedico.setText("", null);
        mactvMedico.setEnabled(!titulos.isEmpty());
    }

    private ArrayList<String> gerarStringMedicos(){
        ArrayList<String> titulos = new ArrayList<>();
        if (listaMedicos == null) return titulos;

        for (Medico m : listaMedicos) {
            if (m == null) continue;
            titulos.add(montarTituloMedico(m));
        }
        return titulos;
    }

    // Função usada pela acima para montar o titulo do médico específico da lista.
    // Fiz separado para usar novamente em outra função
    private String montarTituloMedico(Medico m){
        String esp  = m.getEspecialidade() != null ? m.getEspecialidade().trim() : "";
        String nome = m.getNome() != null ? m.getNome().trim() : "";
        if (!esp.isEmpty() && !nome.isEmpty()) return (esp + " - " + nome).trim();
        if (!esp.isEmpty())                    return esp;
        return nome;
    }

    private Long obterIdMedicoSelecionado(MaterialAutoCompleteTextView mactvMedico){
        String selecionado = (mactvMedico.getText() != null) ? mactvMedico.getText().toString().trim() : "";
        if (selecionado.isEmpty() || listaMedicos == null) return null;

        // Percorre todos os titulos de todos os médicos para achar qual o correspondente no objeto
        for (Medico m : listaMedicos) {
            if (m == null) continue;
            if (montarTituloMedico(m).equals(selecionado)) {
                return m.getId();
            }
        }
        return null; // se não encontrar
    }

    public void abrirDatePicker(View v) {
        Calendar today = Calendario.startOfToday(ZONE);

        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    Calendar c = Calendar.getInstance(ZONE);
                    c.set(Calendar.YEAR, y);
                    c.set(Calendar.MONTH, m); // 0..11
                    c.set(Calendar.DAY_OF_MONTH, d);
                    Calendario.zeroTime(c);

                    selectedDate = c;
                    etData.setText(Calendario.formatarDataPtBr(c, ZONE));
                    atualizarHorarios(selectedDate);
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        dp.getDatePicker().setMinDate(Calendario.startOfTodayMillis(ZONE));
        dp.show();
    }

    private void atualizarHorarios(Calendar date) {
        List<String> slots = Calendario.filtrarSlotsDisponiveis(date, ZONE, SLOT_POOL);
        Calendario.aplicarSlotsNoDropdown(this, acHora, slots);
    }

    public String obterDataHoraIsoSelecionada() {
        String horaStr = acHora.getText() == null ? "" : acHora.getText().toString().trim();
        return Calendario.montarIso(selectedDate, horaStr, ZONE);
    }

    public void cancelar(View v) {
        finish();
    }
}
