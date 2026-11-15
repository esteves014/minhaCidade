package com.example.minhacidade.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Camada de banco (padrão do projeto CRUD): cria o esquema SQLite e expõe as operações de
 * inserir, listar/buscar, atualizar e deletar de cada tabela. Os métodos de leitura devolvem
 * {@link Cursor} (quem chama fecha); a conversão para objetos e as regras de negócio ficam nos
 * repositórios. Toda consulta de conteúdo é filtrada pela cidade, o mesmo contrato que uma API do
 * site deverá seguir.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "minhacidade.db";
    private static final int DATABASE_VERSION = 5;

    // Tabelas
    public static final String TABLE_CIDADES = "cidades";
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String TABLE_VAGAS = "vagas";
    public static final String TABLE_NOTICIAS = "noticias";
    public static final String TABLE_DENUNCIAS = "denuncias";
    public static final String TABLE_PARECERES = "pareceres";

    // Colunas (várias se repetem entre as tabelas)
    public static final String COL_ID = "id";
    public static final String COL_CIDADE_ID = "cidade_id";
    public static final String COL_USUARIO_ID = "usuario_id";
    public static final String COL_DENUNCIA_ID = "denuncia_id";
    public static final String COL_NOME = "nome";
    public static final String COL_UF = "uf";
    public static final String COL_CONTRATADA = "contratada";
    public static final String COL_EMAIL_PREFEITURA = "email_prefeitura";
    public static final String COL_TELEFONE_PREFEITURA = "telefone_prefeitura";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_CPF = "cpf";
    public static final String COL_EMAIL = "email";
    public static final String COL_TELEFONE = "telefone";
    public static final String COL_ENDERECO = "endereco";
    public static final String COL_SENHA_SALT = "senha_salt";
    public static final String COL_SENHA_HASH = "senha_hash";
    public static final String COL_CRIADO_EM = "criado_em";
    public static final String COL_TITULO = "titulo";
    public static final String COL_EMPRESA = "empresa";
    public static final String COL_TIPO = "tipo";
    public static final String COL_BAIRRO = "bairro";
    public static final String COL_SALARIO = "salario";
    public static final String COL_DESCRICAO = "descricao";
    public static final String COL_PUBLICADA_EM = "publicada_em";
    public static final String COL_RESUMO = "resumo";
    public static final String COL_CORPO = "corpo";
    public static final String COL_JORNAL = "jornal";
    public static final String COL_CATEGORIA = "categoria";
    public static final String COL_URL = "url";
    public static final String COL_FOTO_PATH = "foto_path";
    public static final String COL_STATUS = "status";
    public static final String COL_CRIADA_EM = "criada_em";
    public static final String COL_TEXTO = "texto";
    public static final String COL_LIDO = "lido";
    /** Coluna calculada por {@link #listarDenuncias(long)}. */
    public static final String COL_NAO_LIDOS = "nao_lidos";

    private static DatabaseHelper instancia;

    /** Uma única instância por processo: evita vários helpers abrindo o mesmo arquivo. */
    public static synchronized DatabaseHelper get(Context contexto) {
        if (instancia == null) {
            instancia = new DatabaseHelper(contexto.getApplicationContext());
        }
        return instancia;
    }

    private DatabaseHelper(Context contexto) {
        super(contexto, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CIDADES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOME + " TEXT NOT NULL, "
                + COL_UF + " TEXT NOT NULL, "
                + COL_CONTRATADA + " INTEGER NOT NULL, "
                + COL_EMAIL_PREFEITURA + " TEXT, "
                + COL_TELEFONE_PREFEITURA + " TEXT, "
                + COL_LATITUDE + " REAL, "
                + COL_LONGITUDE + " REAL)");
        db.execSQL("CREATE TABLE " + TABLE_USUARIOS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CPF + " TEXT NOT NULL UNIQUE, "
                + COL_NOME + " TEXT NOT NULL, "
                + COL_EMAIL + " TEXT NOT NULL, "
                + COL_TELEFONE + " TEXT, "
                + COL_ENDERECO + " TEXT, "
                + COL_LATITUDE + " REAL, "
                + COL_LONGITUDE + " REAL, "
                + COL_CIDADE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_CIDADES + "(" + COL_ID + "), "
                + COL_SENHA_SALT + " TEXT NOT NULL, "
                + COL_SENHA_HASH + " TEXT NOT NULL, "
                + COL_CRIADO_EM + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_VAGAS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CIDADE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_CIDADES + "(" + COL_ID + "), "
                + COL_TITULO + " TEXT NOT NULL, "
                + COL_EMPRESA + " TEXT NOT NULL, "
                + COL_TIPO + " TEXT NOT NULL, "
                + COL_BAIRRO + " TEXT, "
                + COL_SALARIO + " TEXT, "
                + COL_DESCRICAO + " TEXT, "
                + COL_TELEFONE + " TEXT, "
                + COL_EMAIL + " TEXT, "
                + COL_PUBLICADA_EM + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_NOTICIAS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CIDADE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_CIDADES + "(" + COL_ID + "), "
                + COL_TITULO + " TEXT NOT NULL, "
                + COL_RESUMO + " TEXT, "
                + COL_CORPO + " TEXT, "
                + COL_JORNAL + " TEXT NOT NULL, "
                + COL_CATEGORIA + " TEXT, "
                + COL_URL + " TEXT, "
                + COL_PUBLICADA_EM + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_DENUNCIAS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CIDADE_ID + " INTEGER NOT NULL REFERENCES " + TABLE_CIDADES + "(" + COL_ID + "), "
                + COL_USUARIO_ID + " INTEGER NOT NULL REFERENCES " + TABLE_USUARIOS + "(" + COL_ID + "), "
                + COL_CATEGORIA + " TEXT NOT NULL, "
                + COL_ENDERECO + " TEXT NOT NULL, "
                + COL_DESCRICAO + " TEXT NOT NULL, "
                + COL_FOTO_PATH + " TEXT, "
                + COL_LATITUDE + " REAL, "
                + COL_LONGITUDE + " REAL, "
                + COL_STATUS + " TEXT NOT NULL, "
                + COL_CRIADA_EM + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_PARECERES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DENUNCIA_ID + " INTEGER NOT NULL REFERENCES " + TABLE_DENUNCIAS + "(" + COL_ID + "), "
                + COL_STATUS + " TEXT NOT NULL, "
                + COL_TEXTO + " TEXT NOT NULL, "
                + COL_CRIADO_EM + " INTEGER NOT NULL, "
                + COL_LIDO + " INTEGER NOT NULL DEFAULT 0)");
        SeedData.popular(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int antiga, int nova) {
        // Fase de protótipo: só há dados fictícios, então recriamos tudo.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARECERES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DENUNCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTICIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VAGAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CIDADES);
        onCreate(db);
    }

    // ---- Cidades (somente leitura: o cadastro de cidades vem do site/contrato) ----

    /** Cidades que contrataram o serviço, em ordem alfabética. */
    public Cursor listarCidadesContratadas() {
        return getReadableDatabase().query(TABLE_CIDADES, null, COL_CONTRATADA + " = 1", null,
                null, null, COL_NOME);
    }

    public Cursor buscarCidade(long id) {
        return buscarPorId(TABLE_CIDADES, id);
    }

    // ---- Usuários ----

    /** Devolve o id criado ou -1 se o CPF já existe. */
    public long inserirUsuario(String cpf, String nome, String email, long cidadeId,
                               String senhaSalt, String senhaHash) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CPF, cpf);
        cv.put(COL_NOME, nome);
        cv.put(COL_EMAIL, email);
        cv.put(COL_CIDADE_ID, cidadeId);
        cv.put(COL_SENHA_SALT, senhaSalt);
        cv.put(COL_SENHA_HASH, senhaHash);
        cv.put(COL_CRIADO_EM, System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE_USUARIOS, null, cv);
    }

    public Cursor buscarUsuario(long id) {
        return buscarPorId(TABLE_USUARIOS, id);
    }

    public Cursor buscarUsuarioPorCpf(String cpf) {
        return getReadableDatabase().query(TABLE_USUARIOS, null, COL_CPF + " = ?",
                new String[]{cpf}, null, null, null);
    }

    /** O CPF não muda. Endereço e posição são opcionais (null apaga). */
    public boolean atualizarUsuario(long id, String nome, String email, String telefone, String endereco,
                                    Double latitude, Double longitude, long cidadeId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOME, nome);
        cv.put(COL_EMAIL, email);
        cv.put(COL_TELEFONE, telefone);
        cv.put(COL_ENDERECO, endereco);
        if (latitude != null && longitude != null) {
            cv.put(COL_LATITUDE, latitude);
            cv.put(COL_LONGITUDE, longitude);
        } else {
            cv.putNull(COL_LATITUDE);
            cv.putNull(COL_LONGITUDE);
        }
        cv.put(COL_CIDADE_ID, cidadeId);
        return atualizarPorId(TABLE_USUARIOS, id, cv);
    }

    public boolean atualizarSenha(long id, String senhaSalt, String senhaHash) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SENHA_SALT, senhaSalt);
        cv.put(COL_SENHA_HASH, senhaHash);
        return atualizarPorId(TABLE_USUARIOS, id, cv);
    }

    // ---- Vagas (somente leitura no app: o conteúdo vem das prefeituras/empresas) ----

    public Cursor listarVagas(long cidadeId, String busca, String tipo) {
        StringBuilder where = new StringBuilder(COL_CIDADE_ID + " = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(cidadeId));
        if (tipo != null) {
            where.append(" AND ").append(COL_TIPO).append(" = ?");
            args.add(tipo);
        }
        if (busca != null && !busca.trim().isEmpty()) {
            where.append(" AND (").append(COL_TITULO).append(" LIKE ? OR ").append(COL_EMPRESA)
                    .append(" LIKE ? OR ").append(COL_BAIRRO).append(" LIKE ?)");
            String q = "%" + busca.trim() + "%";
            args.add(q);
            args.add(q);
            args.add(q);
        }
        return getReadableDatabase().query(TABLE_VAGAS, null, where.toString(),
                args.toArray(new String[0]), null, null, COL_PUBLICADA_EM + " DESC");
    }

    public Cursor buscarVaga(long id) {
        return buscarPorId(TABLE_VAGAS, id);
    }

    // ---- Notícias (somente leitura no app) ----

    public Cursor listarJornais(long cidadeId) {
        return getReadableDatabase().rawQuery(
                "SELECT DISTINCT " + COL_JORNAL + " FROM " + TABLE_NOTICIAS
                        + " WHERE " + COL_CIDADE_ID + " = ? ORDER BY " + COL_JORNAL,
                new String[]{String.valueOf(cidadeId)});
    }

    public Cursor listarNoticias(long cidadeId, String jornal) {
        String where = jornal == null
                ? COL_CIDADE_ID + " = ?"
                : COL_CIDADE_ID + " = ? AND " + COL_JORNAL + " = ?";
        String[] args = jornal == null
                ? new String[]{String.valueOf(cidadeId)}
                : new String[]{String.valueOf(cidadeId), jornal};
        return getReadableDatabase().query(TABLE_NOTICIAS, null, where, args,
                null, null, COL_PUBLICADA_EM + " DESC");
    }

    public Cursor buscarNoticia(long id) {
        return buscarPorId(TABLE_NOTICIAS, id);
    }

    // ---- Denúncias ----

    /** Denúncias do cidadão, com a contagem de pareceres ainda não lidos em {@link #COL_NAO_LIDOS}. */
    public Cursor listarDenuncias(long usuarioId) {
        return getReadableDatabase().rawQuery(
                "SELECT d.*, (SELECT COUNT(*) FROM " + TABLE_PARECERES + " p WHERE p." + COL_DENUNCIA_ID
                        + " = d." + COL_ID + " AND p." + COL_LIDO + " = 0) AS " + COL_NAO_LIDOS
                        + " FROM " + TABLE_DENUNCIAS + " d WHERE d." + COL_USUARIO_ID
                        + " = ? ORDER BY d." + COL_CRIADA_EM + " DESC",
                new String[]{String.valueOf(usuarioId)});
    }

    public Cursor buscarDenuncia(long id) {
        return buscarPorId(TABLE_DENUNCIAS, id);
    }

    /** latitude e longitude são opcionais: só são gravadas se o cidadão informou a posição. */
    public long inserirDenuncia(long cidadeId, long usuarioId, String categoria, String endereco,
                                String descricao, String fotoPath, Double latitude, Double longitude,
                                String status) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CIDADE_ID, cidadeId);
        cv.put(COL_USUARIO_ID, usuarioId);
        cv.put(COL_CATEGORIA, categoria);
        cv.put(COL_ENDERECO, endereco);
        cv.put(COL_DESCRICAO, descricao);
        cv.put(COL_FOTO_PATH, fotoPath);
        if (latitude != null && longitude != null) {
            cv.put(COL_LATITUDE, latitude);
            cv.put(COL_LONGITUDE, longitude);
        }
        cv.put(COL_STATUS, status);
        cv.put(COL_CRIADA_EM, System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE_DENUNCIAS, null, cv);
    }

    // ---- Pareceres da prefeitura ----

    /** Pareceres de uma denúncia, do mais recente para o mais antigo. */
    public Cursor listarPareceres(long denunciaId) {
        return getReadableDatabase().query(TABLE_PARECERES, null, COL_DENUNCIA_ID + " = ?",
                new String[]{String.valueOf(denunciaId)}, null, null,
                COL_CRIADO_EM + " DESC, " + COL_ID + " DESC");
    }

    /**
     * Grava o parecer e atualiza o status da denúncia na mesma transação. Devolve o id do parecer
     * ou -1 se falhou.
     */
    public long inserirParecer(long denunciaId, String novoStatus, String texto) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_DENUNCIA_ID, denunciaId);
            cv.put(COL_STATUS, novoStatus);
            cv.put(COL_TEXTO, texto);
            cv.put(COL_CRIADO_EM, System.currentTimeMillis());
            cv.put(COL_LIDO, 0);
            long id = db.insert(TABLE_PARECERES, null, cv);
            if (id == -1) {
                return -1;
            }
            ContentValues status = new ContentValues();
            status.put(COL_STATUS, novoStatus);
            db.update(TABLE_DENUNCIAS, status, COL_ID + " = ?", new String[]{String.valueOf(denunciaId)});
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public boolean marcarPareceresLidos(long denunciaId) {
        ContentValues cv = new ContentValues();
        cv.put(COL_LIDO, 1);
        return getWritableDatabase().update(TABLE_PARECERES, cv,
                COL_DENUNCIA_ID + " = ? AND " + COL_LIDO + " = 0",
                new String[]{String.valueOf(denunciaId)}) > 0;
    }

    /** Quantidade de pareceres novos nas denúncias do cidadão (usada no selo da aba). */
    public int contarPareceresNaoLidos(long usuarioId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PARECERES + " p JOIN " + TABLE_DENUNCIAS + " d ON d."
                        + COL_ID + " = p." + COL_DENUNCIA_ID + " WHERE d." + COL_USUARIO_ID
                        + " = ? AND p." + COL_LIDO + " = 0",
                new String[]{String.valueOf(usuarioId)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // ---- Auxiliares ----

    private Cursor buscarPorId(String tabela, long id) {
        return getReadableDatabase().query(tabela, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    private boolean atualizarPorId(String tabela, long id, ContentValues cv) {
        return getWritableDatabase().update(tabela, cv, COL_ID + " = ?",
                new String[]{String.valueOf(id)}) > 0;
    }
}
