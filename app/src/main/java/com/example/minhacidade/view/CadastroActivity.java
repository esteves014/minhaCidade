package com.example.minhacidade.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.repository.UsuarioRepository;
import com.example.minhacidade.viewmodel.UsuarioViewModel;
import com.example.minhacidade.auth.Cpf;
import com.example.minhacidade.auth.Senha;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.repository.Sessao;
import com.example.minhacidade.model.Usuario;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CadastroActivity extends AppCompatActivity {
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private UsuarioViewModel viewModel;

    private TextInputLayout nomeLayout;
    private TextInputLayout cpfLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout cidadeLayout;
    private TextInputLayout senhaLayout;
    private TextInputLayout confirmarLayout;
    private TextInputEditText nome;
    private TextInputEditText cpf;
    private TextInputEditText email;
    private TextInputEditText senha;
    private TextInputEditText confirmar;
    private MaterialAutoCompleteTextView cidade;
    private CidadesDropdown cidades;
    private Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        viewModel = new ViewModelProvider(this).get(UsuarioViewModel.class);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        nomeLayout = findViewById(R.id.nome_layout);
        cpfLayout = findViewById(R.id.cpf_layout);
        emailLayout = findViewById(R.id.email_layout);
        cidadeLayout = findViewById(R.id.cidade_layout);
        senhaLayout = findViewById(R.id.senha_layout);
        confirmarLayout = findViewById(R.id.confirmar_layout);
        nome = findViewById(R.id.nome);
        cpf = findViewById(R.id.cpf);
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);
        confirmar = findViewById(R.id.confirmar);
        cidade = findViewById(R.id.cidade);
        btnCadastrar = findViewById(R.id.btn_cadastrar);
        cidades = new CidadesDropdown(this, cidade);

        Ui.mascararCpf(cpf);
        Ui.aoDigitar(nome, () -> nomeLayout.setError(null));
        Ui.aoDigitar(cpf, () -> cpfLayout.setError(null));
        Ui.aoDigitar(email, () -> emailLayout.setError(null));
        Ui.aoDigitar(cidade, () -> cidadeLayout.setError(null));
        Ui.aoDigitar(senha, () -> senhaLayout.setError(null));
        Ui.aoDigitar(confirmar, () -> confirmarLayout.setError(null));
        btnCadastrar.setOnClickListener(v -> cadastrar());

        carregarCidades();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    /** Só as cidades que contrataram o serviço aparecem na lista. */
    private void carregarCidades() {
        executor.execute(() -> {
            java.util.List<Cidade> disponiveis = viewModel.listarCidadesContratadas();
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                cidades.definir(disponiveis, -1);
                boolean semCidades = cidades.vazia();
                ((TextView) findViewById(R.id.aviso_sem_cidade)).setVisibility(semCidades ? View.VISIBLE : View.GONE);
                btnCadastrar.setEnabled(!semCidades);
            });
        });
    }

    private void cadastrar() {
        String nomeDigitado = Ui.texto(nome);
        String digitos = Cpf.somenteDigitos(Ui.texto(cpf));
        String emailDigitado = Ui.texto(email);
        Cidade cidadeEscolhida = cidades.selecionada();
        String senhaDigitada = senha.getText() == null ? "" : senha.getText().toString();
        String confirmacao = confirmar.getText() == null ? "" : confirmar.getText().toString();

        boolean ok = true;
        ok &= verificar(nomeLayout, nomeDigitado.length() >= 3, R.string.erro_nome);
        ok &= verificar(cpfLayout, Cpf.valido(digitos), R.string.erro_cpf_invalido);
        ok &= verificar(emailLayout, Ui.emailValido(emailDigitado), R.string.erro_email);
        ok &= verificar(cidadeLayout, cidadeEscolhida != null, R.string.erro_cidade);
        ok &= verificar(senhaLayout, senhaDigitada.length() >= TAMANHO_MINIMO_SENHA, R.string.erro_senha_curta);
        ok &= verificar(confirmarLayout, senhaDigitada.equals(confirmacao), R.string.erro_senhas_diferentes);
        if (!ok) {
            return;
        }

        definirCarregando(true);
        executor.execute(() -> {
            Usuario criado = null;
            int erro = 0;
            try {
                Senha.Hash hash = Senha.criar(senhaDigitada);
                criado = viewModel.cadastrar(digitos, nomeDigitado, emailDigitado,
                        cidadeEscolhida.id, hash);
            } catch (UsuarioRepository.CadastroException e) {
                erro = e.motivo == UsuarioRepository.Motivo.CPF_JA_CADASTRADO
                        ? R.string.erro_cpf_ja_cadastrado : R.string.erro_cidade_indisponivel;
            }
            Usuario usuario = criado;
            int mensagem = erro;
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                definirCarregando(false);
                if (usuario == null) {
                    TextInputLayout alvo = mensagem == R.string.erro_cpf_ja_cadastrado ? cpfLayout : cidadeLayout;
                    alvo.setError(getString(mensagem));
                } else {
                    Sessao.entrar(this, usuario.id);
                    Ui.abrirPrincipal(this);
                }
            });
        });
    }

    private boolean verificar(TextInputLayout layout, boolean valido, int mensagem) {
        layout.setError(valido ? null : getString(mensagem));
        return valido;
    }

    private void definirCarregando(boolean carregando) {
        btnCadastrar.setEnabled(!carregando);
        btnCadastrar.setText(carregando ? R.string.btn_cadastrando : R.string.btn_cadastrar);
    }
}
