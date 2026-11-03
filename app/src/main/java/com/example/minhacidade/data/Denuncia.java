package com.example.minhacidade.data;

import java.util.Calendar;
import java.util.Locale;

public class Denuncia {
    public static final String ABERTA = "ABERTA";
    public static final String EM_ANALISE = "EM_ANALISE";
    public static final String RESOLVIDA = "RESOLVIDA";

    public long id;
    public long cidadeId;
    public long usuarioId;
    public String categoria;
    public String endereco;
    public String descricao;
    public String fotoPath;
    /** Posição informada pelo cidadão (GPS ou ponto escolhido no mapa); null se não informou. */
    public Double latitude;
    public Double longitude;
    public String status;
    public long criadaEm;
    /** Pareceres da prefeitura que o cidadão ainda não abriu (só preenchido em listarDenuncias). */
    public int naoLidos;

    public boolean temLocalizacao() {
        return latitude != null && longitude != null;
    }

    /** Protocolo derivado do ano e do id, ex.: 2026-00012. */
    public String getProtocolo() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(criadaEm);
        return String.format(Locale.ROOT, "%d-%05d", c.get(Calendar.YEAR), id);
    }
}
