package com.example.minhacidade.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.minhacidade.database.CursorUtil;
import com.example.minhacidade.database.DatabaseHelper;
import com.example.minhacidade.model.Vaga;

import java.util.ArrayList;
import java.util.List;

public class VagaRepository {
    private final DatabaseHelper db;

    public VagaRepository(Context context) {
        db = DatabaseHelper.get(context);
    }

    /** Vagas da cidade, da mais recente para a mais antiga; busca e tipo são opcionais. */
    public List<Vaga> listar(long cidadeId, String busca, String tipo) {
        List<Vaga> lista = new ArrayList<>();
        try (Cursor c = db.listarVagas(cidadeId, busca, tipo)) {
            while (c.moveToNext()) {
                lista.add(ler(c));
            }
        }
        return lista;
    }

    public Vaga buscar(long id) {
        try (Cursor c = db.buscarVaga(id)) {
            return c.moveToFirst() ? ler(c) : null;
        }
    }

    private static Vaga ler(Cursor c) {
        Vaga v = new Vaga();
        v.id = CursorUtil.inteiro(c, DatabaseHelper.COL_ID);
        v.cidadeId = CursorUtil.inteiro(c, DatabaseHelper.COL_CIDADE_ID);
        v.titulo = CursorUtil.texto(c, DatabaseHelper.COL_TITULO);
        v.empresa = CursorUtil.texto(c, DatabaseHelper.COL_EMPRESA);
        v.tipo = CursorUtil.texto(c, DatabaseHelper.COL_TIPO);
        v.bairro = CursorUtil.texto(c, DatabaseHelper.COL_BAIRRO);
        v.salario = CursorUtil.texto(c, DatabaseHelper.COL_SALARIO);
        v.descricao = CursorUtil.texto(c, DatabaseHelper.COL_DESCRICAO);
        v.telefone = CursorUtil.texto(c, DatabaseHelper.COL_TELEFONE);
        v.email = CursorUtil.texto(c, DatabaseHelper.COL_EMAIL);
        v.publicadaEm = CursorUtil.inteiro(c, DatabaseHelper.COL_PUBLICADA_EM);
        return v;
    }
}
