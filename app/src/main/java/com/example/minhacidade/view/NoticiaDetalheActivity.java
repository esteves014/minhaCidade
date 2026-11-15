package com.example.minhacidade.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.model.Noticia;
import com.example.minhacidade.viewmodel.NoticiaViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class NoticiaDetalheActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_noticia_detalhe);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        NoticiaViewModel viewModel = new ViewModelProvider(this).get(NoticiaViewModel.class);
        viewModel.getNoticia().observe(this, noticia -> {
            if (noticia == null) {
                finish();
            } else {
                mostrar(noticia);
            }
        });
        viewModel.carregarNoticia(getIntent().getLongExtra(Ui.EXTRA_ID, -1));
    }

    private void mostrar(Noticia noticia) {
        ((TextView) findViewById(R.id.fonte)).setText(
                getString(R.string.noticia_fonte, noticia.jornal, noticia.categoria));
        ((TextView) findViewById(R.id.titulo)).setText(noticia.titulo);
        ((TextView) findViewById(R.id.data)).setText(Ui.data(noticia.publicadaEm));
        ((TextView) findViewById(R.id.corpo)).setText(noticia.corpo);
        findViewById(R.id.btn_site).setOnClickListener(v ->
                Ui.abrir(this, new Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url))));
    }
}
