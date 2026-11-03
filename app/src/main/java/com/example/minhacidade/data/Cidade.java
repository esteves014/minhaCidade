package com.example.minhacidade.data;

public class Cidade {
    public long id;
    public String nome;
    public String uf;
    /** Só cidades que contrataram o serviço aparecem no cadastro. */
    public boolean contratada;
    public String emailPrefeitura;
    public String telefonePrefeitura;
    /** Centro aproximado da cidade, usado para abrir o mapa. Pode faltar. */
    public Double latitude;
    public Double longitude;

    public String getNomeUf() {
        return nome + " - " + uf;
    }
}
