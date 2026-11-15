package com.example.minhacidade.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.minhacidade.model.Usuario;

/** Guarda qual cidadão está logado neste aparelho. */
public final class Sessao {
    private static final String ARQUIVO = "sessao";
    private static final String CHAVE_USUARIO = "usuario_id";

    private Sessao() {
    }

    private static SharedPreferences prefs(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public static void entrar(Context contexto, long usuarioId) {
        prefs(contexto).edit().putLong(CHAVE_USUARIO, usuarioId).apply();
    }

    public static void sair(Context contexto) {
        prefs(contexto).edit().remove(CHAVE_USUARIO).apply();
    }

    /** Usuário logado, ou null se não há sessão (ou se a conta deixou de existir). */
    public static Usuario usuario(Context contexto) {
        long id = prefs(contexto).getLong(CHAVE_USUARIO, -1);
        if (id < 0) {
            return null;
        }
        Usuario usuario = new UsuarioRepository(contexto).buscar(id);
        if (usuario == null) {
            sair(contexto);
        }
        return usuario;
    }
}
