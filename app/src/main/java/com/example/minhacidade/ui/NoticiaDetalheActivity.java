package com.example.minhacidade.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minhacidade.R;
import com.example.minhacidade.data.Db;
import com.example.minhacidade.data.Noticia;
import com.google.android.material.appbar.MaterialToolbar;

public class NoticiaDetalheActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_noticia_detalhe);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        Noticia noticia = Db.get(this).buscarNoticia(getIntent().getLongExtra(Ui.EXTRA_ID, -1));
        if (noticia == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.fonte)).setText(
                getString(R.string.noticia_fonte, noticia.jornal, noticia.categoria));
        ((TextView) findViewById(R.id.titulo)).setText(noticia.titulo);
        ((TextView) findViewById(R.id.data)).setText(Ui.data(noticia.publicadaEm));
        ((TextView) findViewById(R.id.corpo)).setText(noticia.corpo);
        findViewById(R.id.btn_site).setOnClickListener(v ->
                Ui.abrir(this, new Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url))));
    }
}
