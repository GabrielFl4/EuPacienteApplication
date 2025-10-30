package com.example.eupacienteapplication.entities;

import java.math.BigDecimal;

public class Consulta {

    public Consulta(Long id, boolean consultaPaga, java.math.BigDecimal valor, String sintomas, String dataHoraIso) {
        this.id = id;
        this.consultaPaga = consultaPaga;
        this.valor = valor;
        this.sintomas = sintomas;
        this.dataHoraIso = dataHoraIso;
    }

    public Consulta() { }

    private Long id;
    private String dataHoraIso;              // "yyyy-MM-ddTHH:mm[:ss]"
    private String sintomas;
    private BigDecimal valor;
    private boolean consultaPaga;

    private String statusAndamento;          // "MARCADA" | "PENDENTE" | "CANCELADA"
    private String statusMotivo;             // "ROTINA" | "RETORNO" | "URGENCIA"

    private Long medicoId;
    private String medicoNome;
    private String especialidade;
    private Long pacienteId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDataHoraIso() { return dataHoraIso; }
    public void setDataHoraIso(String dataHoraIso) { this.dataHoraIso = dataHoraIso; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public java.math.BigDecimal getValor() { return valor; }
    public void setValor(java.math.BigDecimal valor) { this.valor = valor; }

    public boolean isConsultaPaga() { return consultaPaga; }
    public void setConsultaPaga(boolean consultaPaga) { this.consultaPaga = consultaPaga; }

    public String getStatusAndamento() { return statusAndamento; }
    public void setStatusAndamento(String statusAndamento) { this.statusAndamento = statusAndamento; }

    public String getStatusMotivo() { return statusMotivo; }
    public void setStatusMotivo(String statusMotivo) { this.statusMotivo = statusMotivo; }

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public String getMedicoNome() { return medicoNome; }
    public void setMedicoNome(String medicoNome) { this.medicoNome = medicoNome; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
}