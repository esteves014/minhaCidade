package com.example.minhacidade.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.model.Vaga;
import com.example.minhacidade.viewmodel.VagaViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class VagaDetalheActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vaga_detalhe);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        VagaViewModel viewModel = new ViewModelProvider(this).get(VagaViewModel.class);
        viewModel.getVaga().observe(this, vaga -> {
            if (vaga == null) {
                finish();
            } else {
                mostrar(vaga);
            }
        });
        viewModel.carregarVaga(getIntent().getLongExtra(Ui.EXTRA_ID, -1));
    }

    private void mostrar(Vaga vaga) {
        ((TextView) findViewById(R.id.titulo)).setText(vaga.titulo);
        ((TextView) findViewById(R.id.empresa)).setText(vaga.empresa);
        ((TextView) findViewById(R.id.meta)).setText(
                vaga.tipo + " · " + vaga.bairro + " · " + vaga.salario);
        ((TextView) findViewById(R.id.descricao)).setText(vaga.descricao);

        Button email = findViewById(R.id.btn_email);
        if (vaga.email == null || vaga.email.isEmpty()) {
            email.setVisibility(View.GONE);
        } else {
            email.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{vaga.email});
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_vaga_assunto, vaga.titulo));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_vaga_corpo, vaga.titulo));
                Ui.abrir(this, intent);
            });
        }

        Button ligar = findViewById(R.id.btn_ligar);
        if (vaga.telefone == null || vaga.telefone.isEmpty()) {
            ligar.setVisibility(View.GONE);
        } else {
            ligar.setOnClickListener(v -> Ui.abrir(this,
                    new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(vaga.telefone)))));
        }
    }
}
