package com.example.minhacidade.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.minhacidade.R;
import com.example.minhacidade.auth.Cpf;
import com.example.minhacidade.data.Sessao;

public class PerfilFragment extends CidadaoFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_editar).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), EditarPerfilActivity.class)));
        view.findViewById(R.id.btn_sair).setOnClickListener(v -> {
            Sessao.sair(requireContext());
            Ui.abrirLogin(requireActivity());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Os dados podem ter mudado na tela de edição.
        carregarCidadao();
        View view = getView();
        if (view == null || semSessao()) {
            return;
        }
        ((TextView) view.findViewById(R.id.nome)).setText(usuario.nome);
        ((TextView) view.findViewById(R.id.email)).setText(usuario.email);
        ((TextView) view.findViewById(R.id.telefone)).setText(
                usuario.telefone == null || usuario.telefone.isEmpty()
                        ? getString(R.string.perfil_telefone_vazio) : usuario.telefone);
        ((TextView) view.findViewById(R.id.endereco)).setText(
                usuario.temEndereco() ? usuario.endereco : getString(R.string.perfil_endereco_vazio));
        ((TextView) view.findViewById(R.id.cpf)).setText(
                getString(R.string.perfil_cpf, Cpf.mascarar(usuario.cpf)));
        ((TextView) view.findViewById(R.id.cidade)).setText(cidade.getNomeUf());
        ((TextView) view.findViewById(R.id.prefeitura)).setText(
                getString(R.string.perfil_prefeitura, cidade.nome));
        ((TextView) view.findViewById(R.id.contato_prefeitura)).setText(
                cidade.emailPrefeitura + "\n" + cidade.telefonePrefeitura);
    }
}
