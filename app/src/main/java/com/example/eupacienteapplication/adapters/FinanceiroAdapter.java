package com.example.eupacienteapplication.adapters;

import android.content.Context;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eupacienteapplication.R;
import com.example.eupacienteapplication.entities.Consulta;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.textview.MaterialTextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FinanceiroAdapter extends RecyclerView.Adapter<FinanceiroAdapter.VH> {

    private final Context context;
    private final List<Consulta> dados;
    // quais itens estão expandidos (por id estável)
    private final Set<Long> expandidos = new HashSet<>();

    public interface AcaoPagamento { void onPagar(Consulta c, int position); }
    private final AcaoPagamento acaoPagamento;

    public FinanceiroAdapter(List<Consulta> dados, Context ctx, AcaoPagamento acaoPagamento) {
        this.context = ctx;
        this.dados = (dados != null) ? dados : new ArrayList<>();
        this.acaoPagamento = acaoPagamento;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Consulta c = dados.get(position);
        return (c.getId() != null) ? c.getId() : position;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consulta_financeiro, parent, false);
        return new VH(v);
    }


    @Override
    public void onBindViewHolder(VH h, int position) {
        Consulta c = dados.get(position);

        // data/hora em ISO (aceito "yyyy-MM-dd" ou "yyyy-MM-ddTHH:mm")
        String iso = c.getDataHoraIso();

        // badge de data (dd / MES)
        h.dia.setText(extrairDia(iso));
        h.mes.setText(extrairMes3(iso));

        // especialidade (se não existir no objeto, mostro “Consulta”)
        String esp = c.getEspecialidade();
        h.especialidade.setText((esp != null && !esp.isEmpty()) ? esp : "Consulta");

        // dia da semana
        h.diaSemana.setText(diaSemanaPtBr(iso));

        // valor (BigDecimal -> “R$ 0,00”)
        h.valor.setText(formatarValor(c.getValor()));

        // horário “HH:mm - HH:mm”
        h.horario.setText(faixaHorario(iso, 45)); // padrão 45 min

        // status de pagamento
        boolean paga = c.isConsultaPaga();
        h.chipStatus.setText(paga ? "PAGA" : "ABERTO");
        h.chipStatus.setChipBackgroundColorResource(paga ? R.color.consulta_financeiro_paga : R.color.consulta_financeiro_aberto);

        // expansão
        boolean aberto = expandidos.contains(getItemId(position));
        h.actions.setVisibility(aberto ? View.VISIBLE : View.GONE);
        h.divider.setVisibility(aberto ? View.VISIBLE : View.GONE);
        h.caret.setRotation(aberto ? 180f : 0f);

        // // botão: texto + estado
        h.btnPagar.setEnabled(!paga);
        h.btnPagar.setText(paga ? "Consulta já paga" : "Realizar Pagamento");

        // clique no card alterna a expansão
        h.card.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) h.itemView);
            long key = getItemId(h.getAdapterPosition());
            if (expandidos.contains(key)) expandidos.remove(key); else expandidos.add(key);
            notifyItemChanged(h.getAdapterPosition());
        });


        h.btnPagar.setOnClickListener(v -> {
            if (!paga && acaoPagamento != null) {
                acaoPagamento.onPagar(c, h.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    // ===== helpers =====

    private String extrairDia(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(8, 10);
    }

    public void marcarComoPagaLocal(int position) {
        if (position < 0 || position >= dados.size()) return;
        Consulta c = dados.get(position);
        c.setConsultaPaga(true);
        notifyItemChanged(position);
    }

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

    private String diaSemanaPtBr(String iso) {
        try {
            if (iso == null || iso.length() < 10) return "—";
            int ano = Integer.parseInt(iso.substring(0, 4));
            int mes = Integer.parseInt(iso.substring(5, 7));
            int dia = Integer.parseInt(iso.substring(8, 10));
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(ano, mes - 1, dia);
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

    private String faixaHorario(String iso, int addMin) {
        // pega HH:mm do início; se não vier hora, retorna vazio
        String ini = (iso != null && iso.length() >= 16) ? iso.substring(11, 16) : "";
        if (ini.isEmpty()) return "";
        int h = Integer.parseInt(ini.substring(0, 2));
        int m = Integer.parseInt(ini.substring(3, 5));
        int total = h * 60 + m + addMin;
        int hf = (total / 60) % 24;
        int mf = total % 60;
        String fim = String.format(Locale.getDefault(), "%02d:%02d", hf, mf);
        return ini + " - " + fim;
    }

    private String formatarValor(BigDecimal v) {
        if (v == null) return "R$ 0,00";
        // formato simples (sem NumberFormat pra manter o padrão do resto do app)
        String s = v.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        s = s.replace(".", ",");
        return "R$ " + s;
    }

    // ViewHolder
    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView dia, mes, especialidade, diaSemana, valor, horario;
        Chip chipStatus;
        MaterialDivider divider;
        LinearLayout actions;
        View caret;
        MaterialButton btnPagar;

        VH(View v) {
            super(v);
            card          = v.findViewById(R.id.Financeiro_Item_Card);
            dia           = v.findViewById(R.id.Financeiro_Item_Dia);
            mes           = v.findViewById(R.id.Financeiro_Item_Mes);
            especialidade = v.findViewById(R.id.Financeiro_Item_Especialidade);
            diaSemana     = v.findViewById(R.id.Financeiro_Item_DiaSemana);
            valor         = v.findViewById(R.id.Financeiro_Item_Valor);
            horario       = v.findViewById(R.id.Financeiro_Item_Horario);
            chipStatus    = v.findViewById(R.id.Financeiro_Item_ChipStatus);
            divider       = v.findViewById(R.id.Financeiro_Item_Divider);
            actions       = v.findViewById(R.id.Financeiro_Item_Actions);
            btnPagar      = v.findViewById(R.id.Financeiro_Item_BtnPagar);
            caret         = v.findViewById(R.id.Financeiro_Item_Caret);
        }
    }
}
