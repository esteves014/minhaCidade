package com.example.minhacidade.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.minhacidade.data.Cidade;
import com.example.minhacidade.data.Db;
import com.example.minhacidade.data.Sessao;
import com.example.minhacidade.data.Usuario;

/** Base das abas: carrega o cidadão logado e a cidade dele (que define todo o conteúdo exibido). */
abstract class CidadaoFragment extends Fragment {
    @Nullable
    protected Usuario usuario;
    @Nullable
    protected Cidade cidade;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carregarCidadao();
    }

    /** Recarrega usuário e cidade (útil depois de editar o perfil). */
    protected void carregarCidadao() {
        Context contexto = requireContext();
        usuario = Sessao.usuario(contexto);
        cidade = usuario == null ? null : Db.get(contexto).buscarCidade(usuario.cidadeId);
    }

    /** True quando não há sessão válida; a MainActivity cuida de redirecionar para o login. */
    protected boolean semSessao() {
        return usuario == null || cidade == null;
    }
}
