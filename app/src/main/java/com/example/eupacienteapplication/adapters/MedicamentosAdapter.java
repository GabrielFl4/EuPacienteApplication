package com.example.eupacienteapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.entities.Medicamento;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MedicamentosAdapter extends RecyclerView.Adapter<MedicamentosAdapter.VH> {

    private final Context context;

    private final List<Medicamento> dados;

    private final Long receitaId; // talvez eu use no futuro, não sei.. tá ai

    public MedicamentosAdapter(List<Medicamento> dados, Context context, Long receitaId){
        this.context = context;
        this.dados = (dados != null) ? dados : new ArrayList<>();
        this.receitaId = receitaId;
        setHasStableIds(true); // isso deve manter ID estável, foi recomendado
    }

    @Override
    public long getItemId(int position) {
        Medicamento m = dados.get(position);
        // se o backend já mandou id, uso ele; senão, uso a posição (estável o suficiente para a nossa lista)
        return (m.getId() != null) ? m.getId() : position;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        Medicamento m = dados.get(position);

        // título do card: nome do remédio (linha de cima, em negrito no layout)
        String nome = m.getNome();
        h.txtNome.setText((nome != null && !nome.isEmpty()) ? nome : "—");

        // linha de baixo: “dosagem” (estou usando o campo que veio do backend)
        // exemplo que temos no mock: "400 mg — 1 comprimido a cada 8h se dor/febre"
        String info = m.getDosagem();
        h.txtInfo.setText((info != null && !info.isEmpty()) ? info : "");
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    // A ideia do VH é guardar referência das views do elemento personalizado que uso
    // no RecycleView.
    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView txtNome, txtInfo;

        VH(View v){
            super(v);
            card = v.findViewById(R.id.ItemMedic_Card);
            txtInfo = v.findViewById(R.id.ItemMedic_Dosagem);
            txtNome = v.findViewById(R.id.ItemMedic_Nome);
        }
    }
}
