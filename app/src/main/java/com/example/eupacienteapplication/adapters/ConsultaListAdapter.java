package com.example.eupacienteapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.entities.Consulta;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConsultaListAdapter extends android.widget.BaseAdapter {

    private final Context context;
    private final ArrayList<Consulta> dados;
    private int posicaoSelecionada = -1;

    public ConsultaListAdapter(Context context, ArrayList<Consulta> dados) {
        this.context = context;
        this.dados = dados;
    }

    public void setPosicaoSelecionada(int pos) {
        this.posicaoSelecionada = pos;
        notifyDataSetChanged();
    }

    public int getPosicaoSelecionada() {
        return posicaoSelecionada;
    }

    @Override
    public int getCount() {
        return dados.size();
    }

    @Override
    public Object getItem(int position) {
        return dados.get(position);
    }

    @Override
    public long getItemId(int position) {
        Consulta c = dados.get(position);
        return c.getId() != null ? c.getId() : position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_consulta, parent, false);
        }

        TextView tvData   = convertView.findViewById(R.id.ItemConsulta_TextView_Data);
        TextView tvSint   = convertView.findViewById(R.id.ItemConsulta_TextView_Sintomas);
        TextView tvStatus = convertView.findViewById(R.id.ItemConsulta_TextView_Status);
        TextView tvMed    = convertView.findViewById(R.id.ItemConsulta_TextView_Medico);
        TextView tvEsp    = convertView.findViewById(R.id.ItemConsulta_TextView_Especialidade);

        Consulta c = dados.get(position);

        // Data (string ISO -> dd/MM/yyyy HH:mm)
        String dataFormatada = formatarIsoEmPtBr(c.getDataHoraIso());
        tvData.setText(dataFormatada != null ? dataFormatada : "");

        // Sintomas
        String s = c.getSintomas();
        tvSint.setText(s != null && s.length() > 0 ? "Sintomas: " + s : "Sintomas: —");

        // Médico e especialidade
        tvMed.setText(c.getMedicoNome() != null ? c.getMedicoNome() : "—");
        tvEsp.setText(c.getEspecialidade() != null ? c.getEspecialidade() : "—");

        // Status
        String st = c.getStatusAndamento();
        if (st == null || st.length() == 0) st = "—";
        tvStatus.setText(st);

        // Cor do chip de status
        int cor = 0xFF777777; // default
        if ("CONFIRMADA'".equalsIgnoreCase(st))      cor = 0xFF2E7D32; // verde
        else if ("PENDENTE".equalsIgnoreCase(st)) cor = 0xFFEF6C00; // laranja
        else if ("CANCELADA".equalsIgnoreCase(st)) cor = 0xFFC62828; // vermelho
        tvStatus.setBackgroundColor(cor);

        // Seleção visual (se você mantém)
        if (position == posicaoSelecionada) {
            convertView.setBackgroundColor(0xFFEDE7F6);
        } else {
            convertView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        return convertView;
    }

    private String formatarIsoEmPtBr(String iso) {
        if (iso == null) return "";
        // casa "yyyy-MM-ddTHH:mm" ou "yyyy-MM-dd HH:mm"
        Pattern p = Pattern.compile(
                "^(\\d{4})-(\\d{2})-(\\d{2})[T ](\\d{2}):(\\d{2})"
        );
        Matcher m = p.matcher(iso);
        if (m.find()) {
            String y = m.group(1), mm = m.group(2), d = m.group(3), h = m.group(4), min = m.group(5);
            return d + "/" + mm + "/" + y + " " + h + ":" + min;
        }
        // fallback: retorna como veio
        return iso;
    }
}
