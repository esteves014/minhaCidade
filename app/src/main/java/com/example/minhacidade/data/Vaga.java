package com.example.minhacidade.data;

public class Vaga {
    public static final String[] TIPOS = {"CLT", "Estágio", "Temporário", "PJ", "Aprendiz"};

    public long id;
    public long cidadeId;
    public String titulo;
    public String empresa;
    public String tipo;
    public String bairro;
    public String salario;
    public String descricao;
    public String telefone;
    public String email;
    public long publicadaEm;
}
