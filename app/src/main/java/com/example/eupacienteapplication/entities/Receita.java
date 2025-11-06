package com.example.eupacienteapplication.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Receita {
    private Long id;
    private String data;
    private int id_paciente;
    private int id_medico;
    private Medico medico;

    private List<Medicamento> medicamentos = new ArrayList<>();

    private String medicoEspecialidade;

    private String medicoNome;

    public Receita(Long id, String data, int id_paciente, int id_medico, Medico medico, List<Medicamento> medicamentos) {
        this.id = id;
        this.data = data;
        this.id_paciente = id_paciente;
        this.id_medico = id_medico;
        this.medico = medico;
        this.medicamentos = medicamentos;
    }

    public Receita() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Medicamento> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<Medicamento> medicamentos) {
        this.medicamentos = medicamentos;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public int getId_medico() {
        return id_medico;
    }

    public void setId_medico(int id_medico) {
        this.id_medico = id_medico;
    }

    public int getId_paciente() {
        return id_paciente;
    }

    public void setId_paciente(int id_paciente) {
        this.id_paciente = id_paciente;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMedicoEspecialidade() {
        return medicoEspecialidade;
    }
    public void setMedicoEspecialidade(String medicoEspecialidade) {
        this.medicoEspecialidade = medicoEspecialidade;
    }

    public String getMedicoNome() {
        return medicoNome;
    }

    public void setMedicoNome(String medicoNome) {
        this.medicoNome = medicoNome;
    }
}
