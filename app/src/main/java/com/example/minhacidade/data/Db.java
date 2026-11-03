package com.example.minhacidade.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.minhacidade.auth.Senha;

import java.util.ArrayList;
import java.util.List;

/**
 * Banco local com dados fictícios. Toda consulta de conteúdo é filtrada pela cidade, e o cadastro
 * só aceita cidades contratadas: o mesmo contrato que uma API do site deverá seguir.
 */
public class Db extends SQLiteOpenHelper {
    private static final String NOME = "minhacidade.db";
    private static final int VERSAO = 4;

    private static Db instancia;

    public enum Motivo {CPF_JA_CADASTRADO, CIDADE_INDISPONIVEL}

    public static class CadastroException extends Exception {
        public final Motivo motivo;

        CadastroException(Motivo motivo) {
            super(motivo.name());
            this.motivo = motivo;
        }
    }

    public static synchronized Db get(Context contexto) {
        if (instancia == null) {
            instancia = new Db(contexto.getApplicationContext());
        }
        return instancia;
    }

    private Db(Context contexto) {
        super(contexto, NOME, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE cidades (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL,"
                + " uf TEXT NOT NULL, contratada INTEGER NOT NULL, email_prefeitura TEXT,"
                + " telefone_prefeitura TEXT, latitude REAL, longitude REAL)");
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, cpf TEXT NOT NULL UNIQUE,"
                + " nome TEXT NOT NULL, email TEXT NOT NULL, telefone TEXT,"
                + " endereco TEXT, latitude REAL, longitude REAL,"
                + " cidade_id INTEGER NOT NULL REFERENCES cidades(id),"
                + " senha_salt TEXT NOT NULL, senha_hash TEXT NOT NULL, criado_em INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE vagas (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " cidade_id INTEGER NOT NULL REFERENCES cidades(id), titulo TEXT NOT NULL,"
                + " empresa TEXT NOT NULL, tipo TEXT NOT NULL, bairro TEXT, salario TEXT, descricao TEXT,"
                + " telefone TEXT, email TEXT, publicada_em INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE noticias (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " cidade_id INTEGER NOT NULL REFERENCES cidades(id), titulo TEXT NOT NULL,"
                + " resumo TEXT, corpo TEXT, jornal TEXT NOT NULL, categoria TEXT, url TEXT,"
                + " publicada_em INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE denuncias (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " cidade_id INTEGER NOT NULL REFERENCES cidades(id),"
                + " usuario_id INTEGER NOT NULL REFERENCES usuarios(id), categoria TEXT NOT NULL,"
                + " endereco TEXT NOT NULL, descricao TEXT NOT NULL, foto_path TEXT,"
                + " latitude REAL, longitude REAL,"
                + " status TEXT NOT NULL, criada_em INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE pareceres (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " denuncia_id INTEGER NOT NULL REFERENCES denuncias(id), status TEXT NOT NULL,"
                + " texto TEXT NOT NULL, criado_em INTEGER NOT NULL, lido INTEGER NOT NULL DEFAULT 0)");
        SeedData.popular(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int antiga, int nova) {
        // Fase de protótipo: só há dados fictícios, então recriamos tudo.
        db.execSQL("DROP TABLE IF EXISTS pareceres");
        db.execSQL("DROP TABLE IF EXISTS denuncias");
        db.execSQL("DROP TABLE IF EXISTS noticias");
        db.execSQL("DROP TABLE IF EXISTS vagas");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS cidades");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ---- Cidades ----

    /** Cidades que contrataram o serviço: as únicas que o cidadão pode escolher no cadastro. */
    public List<Cidade> listarCidadesContratadas() {
        List<Cidade> resultado = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("cidades", null, "contratada = 1", null,
                null, null, "nome")) {
            while (c.moveToNext()) {
                resultado.add(lerCidade(c));
            }
        }
        return resultado;
    }

    public Cidade buscarCidade(long id) {
        try (Cursor c = getReadableDatabase().query("cidades", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? lerCidade(c) : null;
        }
    }

    private static Cidade lerCidade(Cursor c) {
        Cidade cidade = new Cidade();
        cidade.id = lng(c, "id");
        cidade.nome = str(c, "nome");
        cidade.uf = str(c, "uf");
        cidade.contratada = lng(c, "contratada") == 1;
        cidade.emailPrefeitura = str(c, "email_prefeitura");
        cidade.telefonePrefeitura = str(c, "telefone_prefeitura");
        cidade.latitude = dbl(c, "latitude");
        cidade.longitude = dbl(c, "longitude");
        return cidade;
    }

    // ---- Usuários ----

    public Usuario cadastrarUsuario(String cpf, String nome, String email, long cidadeId, Senha.Hash senha)
            throws CadastroException {
        Cidade cidade = buscarCidade(cidadeId);
        if (cidade == null || !cidade.contratada) {
            throw new CadastroException(Motivo.CIDADE_INDISPONIVEL);
        }
        ContentValues v = new ContentValues();
        v.put("cpf", cpf);
        v.put("nome", nome);
        v.put("email", email);
        v.put("cidade_id", cidadeId);
        v.put("senha_salt", senha.salt);
        v.put("senha_hash", senha.hash);
        v.put("criado_em", System.currentTimeMillis());
        try {
            long id = getWritableDatabase().insertOrThrow("usuarios", null, v);
            return buscarUsuario(id);
        } catch (SQLiteConstraintException e) {
            throw new CadastroException(Motivo.CPF_JA_CADASTRADO);
        }
    }

    /** Confere CPF e senha. Faz hash de senha, então não deve rodar na thread principal. */
    public Usuario autenticar(String cpf, String senha) {
        try (Cursor c = getReadableDatabase().query("usuarios", null, "cpf = ?",
                new String[]{cpf}, null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            boolean ok = Senha.confere(senha, str(c, "senha_salt"), str(c, "senha_hash"));
            return ok ? lerUsuario(c) : null;
        }
    }

    public Usuario buscarUsuario(long id) {
        try (Cursor c = getReadableDatabase().query("usuarios", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? lerUsuario(c) : null;
        }
    }

    private static Usuario lerUsuario(Cursor c) {
        Usuario u = new Usuario();
        u.id = lng(c, "id");
        u.cpf = str(c, "cpf");
        u.nome = str(c, "nome");
        u.email = str(c, "email");
        u.telefone = str(c, "telefone");
        u.endereco = str(c, "endereco");
        u.latitude = dbl(c, "latitude");
        u.longitude = dbl(c, "longitude");
        u.cidadeId = lng(c, "cidade_id");
        return u;
    }

    /**
     * Atualiza os dados editáveis do perfil (o CPF não muda). A cidade só pode ser trocada
     * por uma que tenha contrato. Endereço e posição são opcionais (null apaga).
     */
    public Usuario atualizarUsuario(long id, String nome, String email, String telefone, String endereco,
                                    Double latitude, Double longitude, long cidadeId)
            throws CadastroException {
        Cidade cidade = buscarCidade(cidadeId);
        if (cidade == null || !cidade.contratada) {
            throw new CadastroException(Motivo.CIDADE_INDISPONIVEL);
        }
        ContentValues v = new ContentValues();
        v.put("nome", nome);
        v.put("email", email);
        v.put("telefone", telefone);
        v.put("endereco", endereco);
        if (latitude != null && longitude != null) {
            v.put("latitude", latitude);
            v.put("longitude", longitude);
        } else {
            v.putNull("latitude");
            v.putNull("longitude");
        }
        v.put("cidade_id", cidadeId);
        getWritableDatabase().update("usuarios", v, "id = ?", new String[]{String.valueOf(id)});
        return buscarUsuario(id);
    }

    public void atualizarSenha(long id, Senha.Hash senha) {
        ContentValues v = new ContentValues();
        v.put("senha_salt", senha.salt);
        v.put("senha_hash", senha.hash);
        getWritableDatabase().update("usuarios", v, "id = ?", new String[]{String.valueOf(id)});
    }

    // ---- Vagas ----

    public List<Vaga> listarVagas(long cidadeId, String busca, String tipo) {
        StringBuilder where = new StringBuilder("cidade_id = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(cidadeId));
        if (tipo != null) {
            where.append(" AND tipo = ?");
            args.add(tipo);
        }
        if (busca != null && !busca.trim().isEmpty()) {
            where.append(" AND (titulo LIKE ? OR empresa LIKE ? OR bairro LIKE ?)");
            String q = "%" + busca.trim() + "%";
            args.add(q);
            args.add(q);
            args.add(q);
        }
        List<Vaga> resultado = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("vagas", null, where.toString(),
                args.toArray(new String[0]), null, null, "publicada_em DESC")) {
            while (c.moveToNext()) {
                resultado.add(lerVaga(c));
            }
        }
        return resultado;
    }

    public Vaga buscarVaga(long id) {
        try (Cursor c = getReadableDatabase().query("vagas", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? lerVaga(c) : null;
        }
    }

    private static Vaga lerVaga(Cursor c) {
        Vaga v = new Vaga();
        v.id = lng(c, "id");
        v.cidadeId = lng(c, "cidade_id");
        v.titulo = str(c, "titulo");
        v.empresa = str(c, "empresa");
        v.tipo = str(c, "tipo");
        v.bairro = str(c, "bairro");
        v.salario = str(c, "salario");
        v.descricao = str(c, "descricao");
        v.telefone = str(c, "telefone");
        v.email = str(c, "email");
        v.publicadaEm = lng(c, "publicada_em");
        return v;
    }

    // ---- Notícias ----

    public List<String> listarJornais(long cidadeId) {
        List<String> jornais = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT jornal FROM noticias WHERE cidade_id = ? ORDER BY jornal",
                new String[]{String.valueOf(cidadeId)})) {
            while (c.moveToNext()) {
                jornais.add(c.getString(0));
            }
        }
        return jornais;
    }

    public List<Noticia> listarNoticias(long cidadeId, String jornal) {
        String where = jornal == null ? "cidade_id = ?" : "cidade_id = ? AND jornal = ?";
        String[] args = jornal == null
                ? new String[]{String.valueOf(cidadeId)}
                : new String[]{String.valueOf(cidadeId), jornal};
        List<Noticia> resultado = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("noticias", null, where, args,
                null, null, "publicada_em DESC")) {
            while (c.moveToNext()) {
                resultado.add(lerNoticia(c));
            }
        }
        return resultado;
    }

    public Noticia buscarNoticia(long id) {
        try (Cursor c = getReadableDatabase().query("noticias", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? lerNoticia(c) : null;
        }
    }

    private static Noticia lerNoticia(Cursor c) {
        Noticia n = new Noticia();
        n.id = lng(c, "id");
        n.cidadeId = lng(c, "cidade_id");
        n.titulo = str(c, "titulo");
        n.resumo = str(c, "resumo");
        n.corpo = str(c, "corpo");
        n.jornal = str(c, "jornal");
        n.categoria = str(c, "categoria");
        n.url = str(c, "url");
        n.publicadaEm = lng(c, "publicada_em");
        return n;
    }

    // ---- Denúncias ----

    /** Denúncias feitas pelo cidadão. */
    public List<Denuncia> listarDenuncias(long usuarioId) {
        List<Denuncia> resultado = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT d.*, (SELECT COUNT(*) FROM pareceres p WHERE p.denuncia_id = d.id AND p.lido = 0)"
                        + " AS nao_lidos FROM denuncias d WHERE d.usuario_id = ? ORDER BY d.criada_em DESC",
                new String[]{String.valueOf(usuarioId)})) {
            while (c.moveToNext()) {
                resultado.add(lerDenuncia(c));
            }
        }
        return resultado;
    }

    public Denuncia buscarDenuncia(long id) {
        try (Cursor c = getReadableDatabase().query("denuncias", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? lerDenuncia(c) : null;
        }
    }

    /** latitude e longitude são opcionais: só são gravadas se o cidadão informou a posição. */
    public Denuncia inserirDenuncia(Usuario usuario, String categoria, String endereco, String descricao,
                                    String fotoPath, Double latitude, Double longitude) {
        ContentValues v = new ContentValues();
        v.put("cidade_id", usuario.cidadeId);
        v.put("usuario_id", usuario.id);
        v.put("categoria", categoria);
        v.put("endereco", endereco);
        v.put("descricao", descricao);
        v.put("foto_path", fotoPath);
        if (latitude != null && longitude != null) {
            v.put("latitude", latitude);
            v.put("longitude", longitude);
        }
        v.put("status", Denuncia.ABERTA);
        v.put("criada_em", System.currentTimeMillis());
        long id = getWritableDatabase().insertOrThrow("denuncias", null, v);
        return buscarDenuncia(id);
    }

    private static Denuncia lerDenuncia(Cursor c) {
        Denuncia d = new Denuncia();
        d.id = lng(c, "id");
        d.cidadeId = lng(c, "cidade_id");
        d.usuarioId = lng(c, "usuario_id");
        d.categoria = str(c, "categoria");
        d.endereco = str(c, "endereco");
        d.descricao = str(c, "descricao");
        d.fotoPath = str(c, "foto_path");
        d.latitude = dbl(c, "latitude");
        d.longitude = dbl(c, "longitude");
        d.status = str(c, "status");
        d.criadaEm = lng(c, "criada_em");
        int colunaNaoLidos = c.getColumnIndex("nao_lidos");
        d.naoLidos = colunaNaoLidos >= 0 ? c.getInt(colunaNaoLidos) : 0;
        return d;
    }

    // ---- Pareceres da prefeitura ----

    /** Pareceres de uma denúncia, do mais recente para o mais antigo. */
    public List<Parecer> listarPareceres(long denunciaId) {
        List<Parecer> resultado = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("pareceres", null, "denuncia_id = ?",
                new String[]{String.valueOf(denunciaId)}, null, null, "criado_em DESC, id DESC")) {
            while (c.moveToNext()) {
                Parecer p = new Parecer();
                p.id = lng(c, "id");
                p.denunciaId = lng(c, "denuncia_id");
                p.status = str(c, "status");
                p.texto = str(c, "texto");
                p.criadoEm = lng(c, "criado_em");
                p.lido = lng(c, "lido") == 1;
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Registra a resposta da prefeitura e atualiza o status da denúncia. Hoje é chamado pelo
     * simulador; com o site/API, será o ponto em que as respostas reais entram.
     */
    public Parecer registrarParecer(long denunciaId, String novoStatus, String texto) {
        SQLiteDatabase db = getWritableDatabase();
        long id;
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("denuncia_id", denunciaId);
            v.put("status", novoStatus);
            v.put("texto", texto);
            v.put("criado_em", System.currentTimeMillis());
            v.put("lido", 0);
            id = db.insertOrThrow("pareceres", null, v);

            ContentValues status = new ContentValues();
            status.put("status", novoStatus);
            db.update("denuncias", status, "id = ?", new String[]{String.valueOf(denunciaId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        for (Parecer p : listarPareceres(denunciaId)) {
            if (p.id == id) {
                return p;
            }
        }
        return null;
    }

    public void marcarPareceresLidos(long denunciaId) {
        ContentValues v = new ContentValues();
        v.put("lido", 1);
        getWritableDatabase().update("pareceres", v, "denuncia_id = ? AND lido = 0",
                new String[]{String.valueOf(denunciaId)});
    }

    /** Quantidade de pareceres novos nas denúncias do cidadão (usada no selo da aba). */
    public int contarPareceresNaoLidos(long usuarioId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM pareceres p JOIN denuncias d ON d.id = p.denuncia_id"
                        + " WHERE d.usuario_id = ? AND p.lido = 0",
                new String[]{String.valueOf(usuarioId)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // ---- Utilitários de cursor ----

    private static String str(Cursor c, String coluna) {
        return c.getString(c.getColumnIndexOrThrow(coluna));
    }

    private static long lng(Cursor c, String coluna) {
        return c.getLong(c.getColumnIndexOrThrow(coluna));
    }

    /** Coluna REAL que pode ser nula. */
    private static Double dbl(Cursor c, String coluna) {
        int i = c.getColumnIndexOrThrow(coluna);
        return c.isNull(i) ? null : c.getDouble(i);
    }
}
