package com.example.minhacidade.model;

public class Usuario {
    public long id;
    /** Somente dígitos. */
    public String cpf;
    public String nome;
    public String email;
    /** Opcional; preenchido no perfil. */
    public String telefone;
    /** Endereço de residência (rua, número e bairro), definido pelo cidadão no perfil. Opcional. */
    public String endereco;
    /** Posição do endereço quando ele veio da localização atual; null se foi digitado. */
    public Double latitude;
    public Double longitude;
    public long cidadeId;

    public boolean temEndereco() {
        return endereco != null && !endereco.trim().isEmpty();
    }
}
