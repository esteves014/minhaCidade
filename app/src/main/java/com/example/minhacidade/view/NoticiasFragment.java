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
import com.example.minhacidade.model.Noticia;
import com.example.minhacidade.view.adapter.ListaAdapter;
import com.example.minhacidade.viewmodel.NoticiaViewModel;

public class NoticiasFragment extends CidadaoFragment {
    private final ListaAdapter<Noticia> adapter = new ListaAdapter<>(R.layout.item_noticia, this::vincular);
    private String jornal;
    private TextView vazio;
    private NoticiaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_noticias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (semSessao()) {
            return;
        }
        ((TextView) view.findViewById(R.id.titulo_tela)).setText(getString(R.string.titulo_noticias, cidade.nome));
        RecyclerView lista = view.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
        vazio = view.findViewById(R.id.vazio);

        viewModel = new ViewModelProvider(this).get(NoticiaViewModel.class);
        viewModel.getNoticias().observe(getViewLifecycleOwner(), noticias -> {
            adapter.setDados(noticias);
            vazio.setVisibility(noticias.isEmpty() ? View.VISIBLE : View.GONE);
        });
        // Só os jornais da cidade do cidadão viram filtro; as notícias também são só dela.
        viewModel.getJornais().observe(getViewLifecycleOwner(),
                jornais -> Ui.montarFiltros(view.findViewById(R.id.filtros), getString(R.string.filtro_todos),
                        jornais, valor -> {
                            jornal = valor;
                            carregar();
                        }));
        viewModel.carregarJornais(cidade.id);
        carregar();
    }

    private void carregar() {
        viewModel.carregarLista(cidade.id, jornal);
    }

    private void vincular(View item, Noticia noticia) {
        ((TextView) item.findViewById(R.id.fonte)).setText(
                getString(R.string.noticia_fonte, noticia.jornal, noticia.categoria));
        ((TextView) item.findViewById(R.id.titulo)).setText(noticia.titulo);
        ((TextView) item.findViewById(R.id.resumo)).setText(noticia.resumo);
        ((TextView) item.findViewById(R.id.data)).setText(Ui.data(noticia.publicadaEm));
        item.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), NoticiaDetalheActivity.class).putExtra(Ui.EXTRA_ID, noticia.id)));
    }
}
