package com.example.minhacidade.view;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.model.Denuncia;
import com.example.minhacidade.model.Parecer;
import com.example.minhacidade.model.Usuario;
import com.example.minhacidade.repository.Sessao;
import com.example.minhacidade.viewmodel.DenunciaViewModel;
import com.example.minhacidade.viewmodel.UsuarioViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class DenunciaDetalheActivity extends AppCompatActivity {
    private DenunciaViewModel viewModel;
    private Denuncia denuncia;
    private Cidade cidade;
    private Button btnSimular;
    private boolean debuggable;
    private boolean pareceresMarcadosComoLidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_denuncia_detalhe);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(DenunciaViewModel.class);
        Usuario usuario = Sessao.usuario(this);
        viewModel.carregarDenuncia(getIntent().getLongExtra(Ui.EXTRA_ID, -1));
        denuncia = viewModel.getDenuncia().getValue();
        // Cada cidadão só abre as próprias denúncias.
        if (usuario == null || denuncia == null || denuncia.usuarioId != usuario.id) {
            finish();
            return;
        }
        cidade = new ViewModelProvider(this).get(UsuarioViewModel.class).buscarCidade(denuncia.cidadeId);

        ((TextView) findViewById(R.id.titulo)).setText(denuncia.categoria);
        ((TextView) findViewById(R.id.protocolo)).setText(
                getString(R.string.protocolo_rotulo, denuncia.getProtocolo()));
        ((TextView) findViewById(R.id.endereco)).setText(denuncia.endereco);
        ((TextView) findViewById(R.id.descricao)).setText(denuncia.descricao);
        ((TextView) findViewById(R.id.data)).setText(Ui.data(denuncia.criadaEm));

        if (denuncia.fotoPath != null) {
            Bitmap foto = Ui.carregarBitmap(denuncia.fotoPath, 1200);
            if (foto != null) {
                ImageView imagem = findViewById(R.id.foto);
                imagem.setImageBitmap(foto);
                imagem.setVisibility(View.VISIBLE);
            }
        }

        if (denuncia.temLocalizacao()) {
            Button verNoMapa = findViewById(R.id.btn_mapa);
            verNoMapa.setVisibility(View.VISIBLE);
            verNoMapa.setOnClickListener(v -> startActivity(
                    new Intent(this, MapaActivity.class).putExtra(Ui.EXTRA_ID, denuncia.id)));
        }

        findViewById(R.id.btn_email).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{cidade.emailPrefeitura});
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.email_denuncia_assunto, denuncia.getProtocolo(), denuncia.categoria));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_denuncia_corpo,
                    denuncia.getProtocolo(), denuncia.categoria, denuncia.endereco, denuncia.descricao,
                    usuario.nome, usuario.email));
            Ui.abrir(this, intent);
        });
        findViewById(R.id.btn_ligar).setOnClickListener(v -> Ui.abrir(this,
                new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(cidade.telefonePrefeitura)))));

        btnSimular = findViewById(R.id.btn_simular);
        debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        btnSimular.setOnClickListener(v -> simularParecer());

        // A denúncia e os pareceres são observados: registrar um parecer atualiza a tela sozinho.
        viewModel.getDenuncia().observe(this, atual -> {
            if (atual != null) {
                denuncia = atual;
                Ui.aplicarStatus(findViewById(R.id.status), denuncia.status);
                atualizarSimulador();
            }
        });
        viewModel.getPareceres().observe(this, this::mostrarPareceres);
        viewModel.carregarPareceres(denuncia.id);
    }

    /**
     * Lista os pareceres. Na primeira exibição eles passam a contar como lidos; um parecer que chega
     * depois (simulador) fica como "novo" até o cidadão abrir a denúncia de novo, como um aviso real.
     */
    private void mostrarPareceres(List<Parecer> pareceres) {
        ViewGroup lista = findViewById(R.id.pareceres);
        lista.removeAllViews();
        findViewById(R.id.pareceres_vazio).setVisibility(pareceres.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Parecer p : pareceres) {
            View item = inflater.inflate(R.layout.item_parecer, lista, false);
            Ui.aplicarStatus(item.findViewById(R.id.status), p.status);
            ((TextView) item.findViewById(R.id.data)).setText(Ui.data(p.criadoEm));
            ((TextView) item.findViewById(R.id.texto)).setText(p.texto);
            item.findViewById(R.id.novo).setVisibility(p.lido ? View.GONE : View.VISIBLE);
            lista.addView(item);
        }
        if (!pareceresMarcadosComoLidos) {
            pareceresMarcadosComoLidos = true;
            viewModel.marcarPareceresLidos(denuncia.id);
        }
    }

    /** O botão de teste só existe em builds de debug e some quando a denúncia já foi resolvida. */
    private void atualizarSimulador() {
        boolean podeAvancar = !Denuncia.RESOLVIDA.equals(denuncia.status);
        btnSimular.setVisibility(debuggable && podeAvancar ? View.VISIBLE : View.GONE);
    }

    /**
     * Simula a prefeitura respondendo: avança o status, grava o parecer e dispara a notificação.
     * Com a API do site, o mesmo caminho (registrarParecer + notificar) recebe respostas reais.
     */
    private void simularParecer() {
        boolean emAnalise = Denuncia.ABERTA.equals(denuncia.status);
        String novoStatus = emAnalise ? Denuncia.EM_ANALISE : Denuncia.RESOLVIDA;
        String texto = getString(emAnalise
                ? R.string.parecer_simulado_em_analise : R.string.parecer_simulado_resolvida);

        // Atualiza a denúncia e a lista de pareceres observadas (a tela se redesenha).
        Parecer parecer = viewModel.registrarParecer(denuncia.id, novoStatus, texto);
        Notificador.notificarParecer(this, denuncia, parecer, cidade);
    }
}
