package com.example.eupacienteapplication.util;

import android.widget.ArrayAdapter;
import android.content.Context;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Calendario {

    private Calendario() {}

    // Função para pegar timezone do cara que usa o app onde ele estiver
    public static TimeZone tzBr() {
        return TimeZone.getTimeZone("America/Sao_Paulo");
    }

    // Passo uma timezone para instanciar um calendário no dia e momento 0 0 0 0
    public static Calendar startOfToday(TimeZone tz) {
        Calendar c = Calendar.getInstance(tz);
        zeroTime(c);
        return c;
    }

    // Essa função é a primeira a ser chamada para passar as outras funções e, por fim, colocar o tempo atual
    public static long startOfTodayMillis(TimeZone tz) {
        return startOfToday(tz).getTimeInMillis();
    }

    // Função pra jogar o horário do calendário pra 0
    public static void zeroTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    // Comparação básica para pegar entre 2 datas se são o mesmo dia e ano
    public static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    // ----- Formatações simples -----
    public static String formatarDataPtBr(Calendar c, TimeZone tz) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        df.setTimeZone(tz);
        return df.format(c.getTime());
    }

    // Função complexa da internet para formatar horário por REGEX. Acho que ainda não usaremos no projeto.
    public static String formatarIsoEmPtBr(String iso) {
        if (iso == null) return "";
        Pattern p = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})[T ](\\d{2}):(\\d{2})");
        Matcher m = p.matcher(iso);
        if (m.find()) {
            String y = m.group(1), mm = m.group(2), d = m.group(3), h = m.group(4), min = m.group(5);
            return d + "/" + mm + "/" + y + " " + h + ":" + min;
        }
        return iso;
    }

    // Horários (HH:mm) -> Essa função pega a String e passa para horário em inteiro
    public static int[] parseHourMinute(String hhmm) {
        try {
            int sep = hhmm.indexOf(':');
            if (sep <= 0) return null;
            int h = Integer.parseInt(hhmm.substring(0, sep));
            int m = Integer.parseInt(hhmm.substring(sep + 1));
            if (h < 0 || h > 23 || m < 0 || m > 59) return null;
            return new int[]{h, m};
        } catch (Exception e) {
            return null;
        }
    }

    // Usa a de cima para montar em minutos o horário
    public static int toMinutes(String hhmm) {
        int[] hm = parseHourMinute(hhmm);
        return hm == null ? -1 : (hm[0] * 60 + hm[1]);
    }

    /** Filtra pool de horários removendo os que já passaram, apenas se a data selecionada for “hoje”. */
    public static List<String> filtrarSlotsDisponiveis(Calendar selectedDate, TimeZone tz, String[] poolHhmm) {
        Calendar now = Calendar.getInstance(tz);
        boolean isToday = isSameDay(selectedDate, now);
        int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        List<String> out = new ArrayList<>();
        for (String slot : poolHhmm) {
            int slotMinutes = toMinutes(slot);
            boolean keep = !isToday || slotMinutes >= nowMinutes;
            if (keep) out.add(slot); // Essa função é linda, quando tu entender a mirabolância pro trás disso...
        }
        return out;
    }

    /** Monta ISO yyyy-MM-dd'T'HH:mm:ss a partir da data (zerada em H/M/S) + hora "HH:mm". */
    public static String montarIso(Calendar selectedDate, String hhmm, TimeZone tz) {
        if (selectedDate == null || hhmm == null || hhmm.trim().isEmpty()) return "";
        int[] hm = parseHourMinute(hhmm.trim());
        if (hm == null) return "";

        Calendar out = (Calendar) selectedDate.clone();
        out.setTimeZone(tz);
        out.set(Calendar.HOUR_OF_DAY, hm[0]);
        out.set(Calendar.MINUTE, hm[1]);
        out.set(Calendar.SECOND, 0);
        out.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        iso.setTimeZone(tz);
        return iso.format(out.getTime());
    }

    // Aplica o adapter no AutoComplete
    public static void aplicarSlotsNoDropdown(Context ctx, MaterialAutoCompleteTextView view, List<String> slots) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_list_item_1, slots);
        view.setAdapter(adapter);
        view.setText("", false);
        view.setEnabled(!slots.isEmpty());
    }
}
