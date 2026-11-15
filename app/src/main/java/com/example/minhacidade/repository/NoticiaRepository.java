package com.example.minhacidade.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.minhacidade.database.CursorUtil;
import com.example.minhacidade.database.DatabaseHelper;
import com.example.minhacidade.model.Noticia;

import java.util.ArrayList;
import java.util.List;

public class NoticiaRepository {
    private final DatabaseHelper db;

    public NoticiaRepository(Context context) {
        db = DatabaseHelper.get(context);
    }

    /** Jornais que têm notícias na cidade (viram filtros na tela). */
    public List<String> listarJornais(long cidadeId) {
        List<String> jornais = new ArrayList<>();
        try (Cursor c = db.listarJornais(cidadeId)) {
            while (c.moveToNext()) {
                jornais.add(c.getString(0));
            }
        }
        return jornais;
    }

    /** Notícias da cidade, da mais recente para a mais antiga; jornal é opcional. */
    public List<Noticia> listar(long cidadeId, String jornal) {
        List<Noticia> lista = new ArrayList<>();
        try (Cursor c = db.listarNoticias(cidadeId, jornal)) {
            while (c.moveToNext()) {
                lista.add(ler(c));
            }
        }
        return lista;
    }

    public Noticia buscar(long id) {
        try (Cursor c = db.buscarNoticia(id)) {
            return c.moveToFirst() ? ler(c) : null;
        }
    }

    private static Noticia ler(Cursor c) {
        Noticia n = new Noticia();
        n.id = CursorUtil.inteiro(c, DatabaseHelper.COL_ID);
        n.cidadeId = CursorUtil.inteiro(c, DatabaseHelper.COL_CIDADE_ID);
        n.titulo = CursorUtil.texto(c, DatabaseHelper.COL_TITULO);
        n.resumo = CursorUtil.texto(c, DatabaseHelper.COL_RESUMO);
        n.corpo = CursorUtil.texto(c, DatabaseHelper.COL_CORPO);
        n.jornal = CursorUtil.texto(c, DatabaseHelper.COL_JORNAL);
        n.categoria = CursorUtil.texto(c, DatabaseHelper.COL_CATEGORIA);
        n.url = CursorUtil.texto(c, DatabaseHelper.COL_URL);
        n.publicadaEm = CursorUtil.inteiro(c, DatabaseHelper.COL_PUBLICADA_EM);
        return n;
    }
}
