package com.example.minhacidade.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.minhacidade.R;
import com.example.minhacidade.model.Vaga;
import com.example.minhacidade.view.adapter.ListaAdapter;
import com.example.minhacidade.viewmodel.VagaViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;

public class VagasFragment extends CidadaoFragment {
    private final ListaAdapter<Vaga> adapter = new ListaAdapter<>(R.layout.item_vaga, this::vincular);
    private String tipo;
    private TextInputEditText busca;
    private TextView vazio;
    private VagaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vagas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (semSessao()) {
            return;
        }
        ((TextView) view.findViewById(R.id.titulo_tela)).setText(getString(R.string.titulo_vagas, cidade.nome));
        RecyclerView lista = view.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
        vazio = view.findViewById(R.id.vazio);
        busca = view.findViewById(R.id.busca);

        viewModel = new ViewModelProvider(this).get(VagaViewModel.class);
        viewModel.getVagas().observe(getViewLifecycleOwner(), vagas -> {
            adapter.setDados(vagas);
            vazio.setVisibility(vagas.isEmpty() ? View.VISIBLE : View.GONE);
        });

        Ui.montarFiltros(view.findViewById(R.id.filtros), getString(R.string.filtro_todas),
                Arrays.asList(Vaga.TIPOS), valor -> {
                    tipo = valor;
                    carregar();
                });
        Ui.aoDigitar(busca, this::carregar);
        carregar();
    }

    private void carregar() {
        String texto = busca.getText() == null ? null : busca.getText().toString();
        viewModel.carregarLista(cidade.id, texto, tipo);
    }

    private void vincular(View item, Vaga vaga) {
        ((TextView) item.findViewById(R.id.titulo)).setText(vaga.titulo);
        ((TextView) item.findViewById(R.id.empresa)).setText(vaga.empresa);
        ((TextView) item.findViewById(R.id.meta)).setText(
                vaga.tipo + " · " + vaga.bairro + " · " + vaga.salario);
        ((TextView) item.findViewById(R.id.data)).setText(Ui.data(vaga.publicadaEm));
        item.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), VagaDetalheActivity.class).putExtra(Ui.EXTRA_ID, vaga.id)));
    }
}
