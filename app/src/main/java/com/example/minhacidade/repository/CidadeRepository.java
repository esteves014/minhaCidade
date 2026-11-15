package com.example.minhacidade.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.minhacidade.database.CursorUtil;
import com.example.minhacidade.database.DatabaseHelper;
import com.example.minhacidade.model.Cidade;

import java.util.ArrayList;
import java.util.List;

public class CidadeRepository {
    private final DatabaseHelper db;

    public CidadeRepository(Context context) {
        db = DatabaseHelper.get(context);
    }

    /** Cidades que contrataram o serviço: as únicas que o cidadão pode escolher no cadastro. */
    public List<Cidade> listarContratadas() {
        List<Cidade> lista = new ArrayList<>();
        try (Cursor c = db.listarCidadesContratadas()) {
            while (c.moveToNext()) {
                lista.add(ler(c));
            }
        }
        return lista;
    }

    public Cidade buscar(long id) {
        try (Cursor c = db.buscarCidade(id)) {
            return c.moveToFirst() ? ler(c) : null;
        }
    }

    private static Cidade ler(Cursor c) {
        Cidade cidade = new Cidade();
        cidade.id = CursorUtil.inteiro(c, DatabaseHelper.COL_ID);
        cidade.nome = CursorUtil.texto(c, DatabaseHelper.COL_NOME);
        cidade.uf = CursorUtil.texto(c, DatabaseHelper.COL_UF);
        cidade.contratada = CursorUtil.inteiro(c, DatabaseHelper.COL_CONTRATADA) == 1;
        cidade.emailPrefeitura = CursorUtil.texto(c, DatabaseHelper.COL_EMAIL_PREFEITURA);
        cidade.telefonePrefeitura = CursorUtil.texto(c, DatabaseHelper.COL_TELEFONE_PREFEITURA);
        cidade.latitude = CursorUtil.real(c, DatabaseHelper.COL_LATITUDE);
        cidade.longitude = CursorUtil.real(c, DatabaseHelper.COL_LONGITUDE);
        return cidade;
    }
}
