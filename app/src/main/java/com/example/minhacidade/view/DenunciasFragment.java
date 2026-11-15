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
import com.example.minhacidade.model.Denuncia;
import com.example.minhacidade.view.adapter.ListaAdapter;
import com.example.minhacidade.viewmodel.DenunciaViewModel;

public class DenunciasFragment extends CidadaoFragment {
    private final ListaAdapter<Denuncia> adapter = new ListaAdapter<>(R.layout.item_denuncia, this::vincular);
    private TextView vazio;
    private DenunciaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_denuncias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (semSessao()) {
            return;
        }
        ((TextView) view.findViewById(R.id.titulo_tela)).setText(getString(R.string.titulo_denuncias, cidade.nome));
        RecyclerView lista = view.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
        vazio = view.findViewById(R.id.vazio);
        viewModel = new ViewModelProvider(this).get(DenunciaViewModel.class);
        viewModel.getDenuncias().observe(getViewLifecycleOwner(), denuncias -> {
            adapter.setDados(denuncias);
            vazio.setVisibility(denuncias.isEmpty() ? View.VISIBLE : View.GONE);
        });
        view.findViewById(R.id.fab_nova).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), NovaDenunciaActivity.class)));
        view.findViewById(R.id.fab_mapa).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), MapaActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (semSessao() || vazio == null) {
            return;
        }
        // Recarrega ao voltar: o status e os pareceres novos podem ter mudado.
        viewModel.carregarLista(usuario.id);
    }

    private void vincular(View item, Denuncia denuncia) {
        ((TextView) item.findViewById(R.id.titulo)).setText(denuncia.categoria);
        ((TextView) item.findViewById(R.id.endereco)).setText(denuncia.endereco);
        ((TextView) item.findViewById(R.id.protocolo)).setText(
                getString(R.string.protocolo_rotulo, denuncia.getProtocolo()));
        ((TextView) item.findViewById(R.id.data)).setText(Ui.data(denuncia.criadaEm));
        item.findViewById(R.id.novo_parecer).setVisibility(denuncia.naoLidos > 0 ? View.VISIBLE : View.GONE);
        Ui.aplicarStatus(item.findViewById(R.id.status), denuncia.status);
        item.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), DenunciaDetalheActivity.class).putExtra(Ui.EXTRA_ID, denuncia.id)));
    }
}
