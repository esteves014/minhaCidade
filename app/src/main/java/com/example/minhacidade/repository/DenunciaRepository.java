package com.example.minhacidade.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.minhacidade.database.CursorUtil;
import com.example.minhacidade.database.DatabaseHelper;
import com.example.minhacidade.model.Denuncia;
import com.example.minhacidade.model.Parecer;
import com.example.minhacidade.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/** Denúncias do cidadão e os pareceres que a prefeitura registra sobre elas. */
public class DenunciaRepository {
    private final DatabaseHelper db;

    public DenunciaRepository(Context context) {
        db = DatabaseHelper.get(context);
    }

    /** Denúncias feitas pelo cidadão, da mais recente para a mais antiga. */
    public List<Denuncia> listar(long usuarioId) {
        List<Denuncia> lista = new ArrayList<>();
        try (Cursor c = db.listarDenuncias(usuarioId)) {
            while (c.moveToNext()) {
                lista.add(lerDenuncia(c));
            }
        }
        return lista;
    }

    public Denuncia buscar(long id) {
        try (Cursor c = db.buscarDenuncia(id)) {
            return c.moveToFirst() ? lerDenuncia(c) : null;
        }
    }

    /** A denúncia nasce ABERTA, na cidade do cidadão. Latitude e longitude são opcionais. */
    public Denuncia inserir(Usuario usuario, String categoria, String endereco, String descricao,
                            String fotoPath, Double latitude, Double longitude) {
        long id = db.inserirDenuncia(usuario.cidadeId, usuario.id, categoria, endereco, descricao,
                fotoPath, latitude, longitude, Denuncia.ABERTA);
        if (id == -1) {
            throw new IllegalStateException("Não foi possível gravar a denúncia");
        }
        return buscar(id);
    }

    // ---- Pareceres da prefeitura ----

    /** Pareceres de uma denúncia, do mais recente para o mais antigo. */
    public List<Parecer> listarPareceres(long denunciaId) {
        List<Parecer> lista = new ArrayList<>();
        try (Cursor c = db.listarPareceres(denunciaId)) {
            while (c.moveToNext()) {
                lista.add(lerParecer(c));
            }
        }
        return lista;
    }

    /**
     * Registra a resposta da prefeitura e atualiza o status da denúncia. Hoje é chamado pelo
     * simulador; com o site/API, será o ponto em que as respostas reais entram.
     */
    public Parecer registrarParecer(long denunciaId, String novoStatus, String texto) {
        long id = db.inserirParecer(denunciaId, novoStatus, texto);
        if (id == -1) {
            return null;
        }
        for (Parecer p : listarPareceres(denunciaId)) {
            if (p.id == id) {
                return p;
            }
        }
        return null;
    }

    public void marcarPareceresLidos(long denunciaId) {
        db.marcarPareceresLidos(denunciaId);
    }

    /** Quantidade de pareceres novos nas denúncias do cidadão (usada no selo da aba). */
    public int contarPareceresNaoLidos(long usuarioId) {
        return db.contarPareceresNaoLidos(usuarioId);
    }

    private static Denuncia lerDenuncia(Cursor c) {
        Denuncia d = new Denuncia();
        d.id = CursorUtil.inteiro(c, DatabaseHelper.COL_ID);
        d.cidadeId = CursorUtil.inteiro(c, DatabaseHelper.COL_CIDADE_ID);
        d.usuarioId = CursorUtil.inteiro(c, DatabaseHelper.COL_USUARIO_ID);
        d.categoria = CursorUtil.texto(c, DatabaseHelper.COL_CATEGORIA);
        d.endereco = CursorUtil.texto(c, DatabaseHelper.COL_ENDERECO);
        d.descricao = CursorUtil.texto(c, DatabaseHelper.COL_DESCRICAO);
        d.fotoPath = CursorUtil.texto(c, DatabaseHelper.COL_FOTO_PATH);
        d.latitude = CursorUtil.real(c, DatabaseHelper.COL_LATITUDE);
        d.longitude = CursorUtil.real(c, DatabaseHelper.COL_LONGITUDE);
        d.status = CursorUtil.texto(c, DatabaseHelper.COL_STATUS);
        d.criadaEm = CursorUtil.inteiro(c, DatabaseHelper.COL_CRIADA_EM);
        int colunaNaoLidos = c.getColumnIndex(DatabaseHelper.COL_NAO_LIDOS);
        d.naoLidos = colunaNaoLidos >= 0 ? c.getInt(colunaNaoLidos) : 0;
        return d;
    }

    private static Parecer lerParecer(Cursor c) {
        Parecer p = new Parecer();
        p.id = CursorUtil.inteiro(c, DatabaseHelper.COL_ID);
        p.denunciaId = CursorUtil.inteiro(c, DatabaseHelper.COL_DENUNCIA_ID);
        p.status = CursorUtil.texto(c, DatabaseHelper.COL_STATUS);
        p.texto = CursorUtil.texto(c, DatabaseHelper.COL_TEXTO);
        p.criadoEm = CursorUtil.inteiro(c, DatabaseHelper.COL_CRIADO_EM);
        p.lido = CursorUtil.inteiro(c, DatabaseHelper.COL_LIDO) == 1;
        return p;
    }
}
