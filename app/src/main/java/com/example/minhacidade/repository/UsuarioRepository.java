package com.example.minhacidade.repository;

import android.content.Context;
import android.database.Cursor;

import com.example.minhacidade.auth.Senha;
import com.example.minhacidade.database.CursorUtil;
import com.example.minhacidade.database.DatabaseHelper;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.model.Usuario;

public class UsuarioRepository {
    public enum Motivo {CPF_JA_CADASTRADO, CIDADE_INDISPONIVEL}

    public static class CadastroException extends Exception {
        public final Motivo motivo;

        CadastroException(Motivo motivo) {
            super(motivo.name());
            this.motivo = motivo;
        }
    }

    private final DatabaseHelper db;
    private final CidadeRepository cidades;

    public UsuarioRepository(Context context) {
        db = DatabaseHelper.get(context);
        cidades = new CidadeRepository(context);
    }

    /** O cadastro só aceita cidades contratadas e CPFs ainda não usados. */
    public Usuario cadastrar(String cpf, String nome, String email, long cidadeId, Senha.Hash senha)
            throws CadastroException {
        exigirCidadeContratada(cidadeId);
        long id = db.inserirUsuario(cpf, nome, email, cidadeId, senha.salt, senha.hash);
        if (id == -1) {
            throw new CadastroException(Motivo.CPF_JA_CADASTRADO);
        }
        return buscar(id);
    }

    /** Confere CPF e senha. Faz hash de senha, então não deve rodar na thread principal. */
    public Usuario autenticar(String cpf, String senha) {
        try (Cursor c = db.buscarUsuarioPorCpf(cpf)) {
            if (!c.moveToFirst()) {
                return null;
            }
            boolean ok = Senha.confere(senha,
                    CursorUtil.texto(c, DatabaseHelper.COL_SENHA_SALT),
                    CursorUtil.texto(c, DatabaseHelper.COL_SENHA_HASH));
            return ok ? ler(c) : null;
        }
    }

    public Usuario buscar(long id) {
        try (Cursor c = db.buscarUsuario(id)) {
            return c.moveToFirst() ? ler(c) : null;
        }
    }

    /**
     * Atualiza os dados editáveis do perfil (o CPF não muda). A cidade só pode ser trocada
     * por uma que tenha contrato. Endereço e posição são opcionais (null apaga).
     */
    public Usuario atualizar(long id, String nome, String email, String telefone, String endereco,
                             Double latitude, Double longitude, long cidadeId) throws CadastroException {
        exigirCidadeContratada(cidadeId);
        db.atualizarUsuario(id, nome, email, telefone, endereco, latitude, longitude, cidadeId);
        return buscar(id);
    }

    public void atualizarSenha(long id, Senha.Hash senha) {
        db.atualizarSenha(id, senha.salt, senha.hash);
    }

    private void exigirCidadeContratada(long cidadeId) throws CadastroException {
        Cidade cidade = cidades.buscar(cidadeId);
        if (cidade == null || !cidade.contratada) {
            throw new CadastroException(Motivo.CIDADE_INDISPONIVEL);
        }
    }

    private static Usuario ler(Cursor c) {
        Usuario u = new Usuario();
        u.id = CursorUtil.inteiro(c, DatabaseHelper.COL_ID);
        u.cpf = CursorUtil.texto(c, DatabaseHelper.COL_CPF);
        u.nome = CursorUtil.texto(c, DatabaseHelper.COL_NOME);
        u.email = CursorUtil.texto(c, DatabaseHelper.COL_EMAIL);
        u.telefone = CursorUtil.texto(c, DatabaseHelper.COL_TELEFONE);
        u.endereco = CursorUtil.texto(c, DatabaseHelper.COL_ENDERECO);
        u.latitude = CursorUtil.real(c, DatabaseHelper.COL_LATITUDE);
        u.longitude = CursorUtil.real(c, DatabaseHelper.COL_LONGITUDE);
        u.cidadeId = CursorUtil.inteiro(c, DatabaseHelper.COL_CIDADE_ID);
        return u;
    }
}
