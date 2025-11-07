package com.example.eupacienteapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.entities.Medico;
import com.example.eupacienteapplication.entities.Receita;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.divider.MaterialDivider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReceitasAdapter extends RecyclerView.Adapter<ReceitasAdapter.VH> {

    private final Context context;
    private final List<Receita> dados;
    // guarda quais itens estão expandidos (por id estável)
    private final Set<Long> expandidos = new HashSet<>();

    public ReceitasAdapter(List<Receita> dados, Context context) {
        this.context = context;
        this.dados = (dados != null) ? dados : new ArrayList<>();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Receita r = dados.get(position);
        return (r.getId() != null) ? r.getId() : position;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receita, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        Receita r = dados.get(position);

        // badge de data: dd / MMM
        String iso = r.getData(); // esperado "yyyy-MM-dd"
        h.txtDia.setText(extrairDia(iso));
        h.txtMes.setText(extrairMes3(iso));

        // título: dia da semana + especialidade (ou “Receita #id” se não houver)
        String titulo = diaSemanaPtBr(iso) + " — " + especialidadeOuId(r);
        h.txtTitulo.setText(titulo);

        // status simples por enquanto
        h.txtStatus.setText("Emitida");

        // estado de expansão (mostra/oculta ações e gira caret)
        boolean aberto = expandidos.contains(getItemId(position));
        h.actions.setVisibility(aberto ? View.VISIBLE : View.GONE);
        h.divider.setVisibility(aberto ? View.VISIBLE : View.GONE);
        h.caret.setRotation(aberto ? 180f : 0f);

        // clique no card alterna expansão
        h.card.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) h.itemView);
            long key = getItemId(h.getAdapterPosition());
            if (expandidos.contains(key)) expandidos.remove(key); else expandidos.add(key);
            notifyItemChanged(h.getAdapterPosition());
        });

        // ações do item
        h.btnDownload.setOnClickListener(v ->
                Toast.makeText(context, "Download não implementado", Toast.LENGTH_SHORT).show());

        h.btnVisualizar.setOnClickListener(v -> {
            Intent it = new Intent(context, com.example.eupacienteapplication.activities.MedicamentosActivity.class);

            // mando o id da receita pra activity que mostra os medicamentos
            it.putExtra("receitaId", r.getId());

            // monta "Dr. Nome — Especialidade"
            String nome = r.getMedicoNome();
            if ((nome == null || nome.isEmpty()) && r.getMedico() != null) {
                nome = r.getMedico().getNome();
            }

            String esp  = r.getMedicoEspecialidade();
            if ((esp == null || esp.isEmpty()) && r.getMedico() != null) {
                esp = r.getMedico().getEspecialidade();
            }

            String tituloMedic;
            if (nome != null && esp != null)      tituloMedic = nome + " — " + esp;
            else if (nome != null)                tituloMedic = nome;
            else if (esp != null)                 tituloMedic = esp;
            else                                  tituloMedic = "Receita #" + r.getId();

            it.putExtra("medicoTitulo", tituloMedic);

            // data crua (yyyy-MM-dd) — vamos formatar na Activity
            it.putExtra("dataIso", r.getData());

            context.startActivity(it);
        });
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }


    // "yyyy-MM-dd" -> "dd"
    private String extrairDia(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(8, 10);
    }

    // "yyyy-MM-dd" -> "JAN"/"FEV"/...
    private String extrairMes3(String iso) {
        if (iso == null || iso.length() < 7) return "";
        String mm = iso.substring(5, 7);
        switch (mm) {
            case "01": return "JAN";
            case "02": return "FEV";
            case "03": return "MAR";
            case "04": return "ABR";
            case "05": return "MAI";
            case "06": return "JUN";
            case "07": return "JUL";
            case "08": return "AGO";
            case "09": return "SET";
            case "10": return "OUT";
            case "11": return "NOV";
            default: return "DEZ";
        }
    }

    // Descobre o dia da semana usando Calendar (é uma biblioteca do java, alternativa boa)
    private String diaSemanaPtBr(String iso) {
        try {
            if (iso == null || iso.length() < 10) return "—";
            int ano = Integer.parseInt(iso.substring(0, 4));
            int mes = Integer.parseInt(iso.substring(5, 7));   // 1..12
            int dia = Integer.parseInt(iso.substring(8, 10));

            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(ano, mes - 1, dia); // Calendar usa 0..11

            switch (c.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:    return "Segunda";
                case Calendar.TUESDAY:   return "Terça";
                case Calendar.WEDNESDAY: return "Quarta";
                case Calendar.THURSDAY:  return "Quinta";
                case Calendar.FRIDAY:    return "Sexta";
                case Calendar.SATURDAY:  return "Sábado";
                default:                 return "Domingo";
            }
        } catch (Exception e) {
            return "—";
        }
    }

    // usa a especialidade do médico quando existir; senão “Receita #id”
    private String especialidadeOuId(Receita r) {
        // 1) se a especialidade veio gravada direto na Receita, usa ela
        if (r.getMedicoEspecialidade() != null && !r.getMedicoEspecialidade().isEmpty()) {
            return r.getMedicoEspecialidade();
        }

        // 2) senão, pega a do objeto médico
        Medico m = r.getMedico();
        if (m != null && m.getEspecialidade() != null && !m.getEspecialidade().isEmpty()) {
            return m.getEspecialidade();
        }

        // 3) se tudo falhar (o que aconteceu até agora enquanto faço essa bomba), usa esse texto
        Long id = r.getId();
        return (id != null) ? "Receita #" + id : "Receita";
    }

    // ViewHolder
    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView txtDia, txtMes, txtTitulo, txtStatus;
        ImageView caret;
        MaterialDivider divider;
        LinearLayout actions;
        MaterialButton btnDownload, btnVisualizar;

        VH(View v) {
            super(v);
            card          = v.findViewById(R.id.ItemReceita_Card);
            txtDia        = v.findViewById(R.id.ItemReceita_Dia);
            txtMes        = v.findViewById(R.id.ItemReceita_Mes);
            txtTitulo     = v.findViewById(R.id.ItemReceita_Titulo);
            txtStatus     = v.findViewById(R.id.ItemReceita_Status);
            caret         = v.findViewById(R.id.ItemReceita_Caret);
            divider       = v.findViewById(R.id.ItemReceita_Divider);
            actions       = v.findViewById(R.id.ItemReceita_Actions);
            btnDownload   = v.findViewById(R.id.ItemReceita_BtnDownload);
            btnVisualizar = v.findViewById(R.id.ItemReceita_BtnVisualizar);
        }
    }
}
