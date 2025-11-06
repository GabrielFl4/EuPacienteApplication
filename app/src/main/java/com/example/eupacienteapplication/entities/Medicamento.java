package com.example.eupacienteapplication.entities;

public class Medicamento {

    private Long id;

    private String nome;

    private String dosagem;

    private int id_receita;

    public Medicamento() {
    }

    public Medicamento(Long id, String nome, String dosagem, int id_receita) {
        this.id = id;
        this.nome = nome;
        this.dosagem = dosagem;
        this.id_receita = id_receita;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public int getId_receita() {
        return id_receita;
    }

    public void setId_receita(int id_receita) {
        this.id_receita = id_receita;
    }
}
