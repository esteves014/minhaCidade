package com.example.minhacidade.data;

/** Resposta da prefeitura a uma denúncia. */
public class Parecer {
    public long id;
    public long denunciaId;
    /** Status da denúncia depois deste parecer (ver {@link Denuncia}). */
    public String status;
    public String texto;
    public long criadoEm;
    public boolean lido;
}
