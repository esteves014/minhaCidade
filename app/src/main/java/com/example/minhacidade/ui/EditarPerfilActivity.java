package com.example.minhacidade.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minhacidade.R;
import com.example.minhacidade.auth.Cpf;
import com.example.minhacidade.auth.Senha;
import com.example.minhacidade.data.Cidade;
import com.example.minhacidade.data.Db;
import com.example.minhacidade.data.Sessao;
import com.example.minhacidade.data.Usuario;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Edição dos dados do cidadão. O CPF (login) não muda; a senha só muda se a nova for preenchida. */
public class EditarPerfilActivity extends AppCompatActivity {
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Usuario usuario;
    private CidadesDropdown cidades;
    private Button btnSalvar;

    private TextInputLayout nomeLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout cidadeLayout;
    private TextInputLayout senhaAtualLayout;
    private TextInputLayout senhaNovaLayout;
    private TextInputLayout senhaConfirmarLayout;
    private TextInputEditText nome;
    private TextInputEditText email;
    private TextInputEditText telefone;
    private TextInputEditText endereco;
    private Button btnLocalizacao;
    private LocalizadorDeEndereco localizador;
    // Posição do endereço: só vale enquanto o texto do campo for o que veio da localização.
    private Double latitude;
    private Double longitude;
    private String textoDaPosicao;
    private TextInputEditText senhaAtual;
    private TextInputEditText senhaNova;
    private TextInputEditText senhaConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        usuario = Sessao.usuario(this);
        if (usuario == null) {
            finish();
            return;
        }

        nomeLayout = findViewById(R.id.nome_layout);
        emailLayout = findViewById(R.id.email_layout);
        cidadeLayout = findViewById(R.id.cidade_layout);
        senhaAtualLayout = findViewById(R.id.senha_atual_layout);
        senhaNovaLayout = findViewById(R.id.senha_nova_layout);
        senhaConfirmarLayout = findViewById(R.id.senha_confirmar_layout);
        nome = findViewById(R.id.nome);
        email = findViewById(R.id.email);
        telefone = findViewById(R.id.telefone);
        senhaAtual = findViewById(R.id.senha_atual);
        senhaNova = findViewById(R.id.senha_nova);
        senhaConfirmar = findViewById(R.id.senha_confirmar);
        btnSalvar = findViewById(R.id.btn_salvar);
        MaterialAutoCompleteTextView campoCidade = findViewById(R.id.cidade);
        cidades = new CidadesDropdown(this, campoCidade);

        endereco = findViewById(R.id.endereco);
        btnLocalizacao = findViewById(R.id.btn_localizacao);
        latitude = usuario.latitude;
        longitude = usuario.longitude;
        textoDaPosicao = usuario.endereco;
        if (savedInstanceState == null) {
            nome.setText(usuario.nome);
            email.setText(usuario.email);
            telefone.setText(usuario.telefone);
            endereco.setText(usuario.endereco);
        }
        localizador = new LocalizadorDeEndereco(this, executor, new LocalizadorDeEndereco.Ouvinte() {
            @Override
            public void aoIniciar() {
                btnLocalizacao.setEnabled(false);
                btnLocalizacao.setText(R.string.btn_localizando);
            }

            @Override
            public void aoEncontrar(String texto, double lat, double lng) {
                restaurarBotaoLocalizacao();
                // Define a posição antes do texto: o aviso de "texto mudou" não pode descartá-la.
                latitude = lat;
                longitude = lng;
                textoDaPosicao = texto;
                endereco.setText(texto);
            }

            @Override
            public void aoFalhar(int mensagem) {
                restaurarBotaoLocalizacao();
                Toast.makeText(EditarPerfilActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
        btnLocalizacao.setOnClickListener(v -> localizador.buscar());
        Ui.aoDigitar(endereco, () -> {
            // Se o cidadão editou o texto, a posição guardada deixou de descrevê-lo.
            if (latitude != null && !Ui.texto(endereco).equals(textoDaPosicao)) {
                latitude = null;
                longitude = null;
            }
        });
        ((TextInputEditText) findViewById(R.id.cpf)).setText(Cpf.formatar(usuario.cpf));

        Ui.aoDigitar(nome, () -> nomeLayout.setError(null));
        Ui.aoDigitar(email, () -> emailLayout.setError(null));
        Ui.aoDigitar(campoCidade, () -> cidadeLayout.setError(null));
        Ui.aoDigitar(senhaAtual, () -> senhaAtualLayout.setError(null));
        Ui.aoDigitar(senhaNova, () -> senhaNovaLayout.setError(null));
        Ui.aoDigitar(senhaConfirmar, () -> senhaConfirmarLayout.setError(null));
        btnSalvar.setOnClickListener(v -> salvar());

        executor.execute(() -> {
            List<Cidade> disponiveis = Db.get(this).listarCidadesContratadas();
            runOnUiThread(() -> {
                if (!isDestroyed()) {
                    boolean primeiraVez = savedInstanceState == null;
                    cidades.definir(disponiveis, primeiraVez ? usuario.cidadeId : -1);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localizador != null) {
            localizador.cancelar();
        }
        executor.shutdown();
    }

    private void restaurarBotaoLocalizacao() {
        btnLocalizacao.setEnabled(true);
        btnLocalizacao.setText(R.string.btn_localizacao_atual);
    }

    private void salvar() {
        String nomeDigitado = Ui.texto(nome);
        String emailDigitado = Ui.texto(email);
        String telefoneDigitado = Ui.texto(telefone);
        String enderecoDigitado = Ui.texto(endereco);
        Double latitudeSalva = enderecoDigitado.isEmpty() ? null : latitude;
        Double longitudeSalva = enderecoDigitado.isEmpty() ? null : longitude;
        Cidade cidadeEscolhida = cidades.selecionada();
        String atual = valor(senhaAtual);
        String nova = valor(senhaNova);
        String confirmacao = valor(senhaConfirmar);
        boolean trocaSenha = !nova.isEmpty() || !confirmacao.isEmpty();

        boolean ok = true;
        ok &= verificar(nomeLayout, nomeDigitado.length() >= 3, R.string.erro_nome);
        ok &= verificar(emailLayout, Ui.emailValido(emailDigitado), R.string.erro_email);
        ok &= verificar(cidadeLayout, cidadeEscolhida != null, R.string.erro_cidade);
        if (trocaSenha) {
            ok &= verificar(senhaAtualLayout, !atual.isEmpty(), R.string.erro_senha_atual_obrigatoria);
            ok &= verificar(senhaNovaLayout, nova.length() >= TAMANHO_MINIMO_SENHA, R.string.erro_senha_curta);
            ok &= verificar(senhaConfirmarLayout, nova.equals(confirmacao), R.string.erro_senhas_diferentes);
        }
        if (!ok) {
            return;
        }

        definirCarregando(true);
        executor.execute(() -> {
            Db db = Db.get(this);
            int erro = 0;
            try {
                if (trocaSenha && db.autenticar(usuario.cpf, atual) == null) {
                    erro = R.string.erro_senha_atual;
                } else {
                    db.atualizarUsuario(usuario.id, nomeDigitado, emailDigitado, telefoneDigitado,
                            enderecoDigitado, latitudeSalva, longitudeSalva, cidadeEscolhida.id);
                    if (trocaSenha) {
                        db.atualizarSenha(usuario.id, Senha.criar(nova));
                    }
                }
            } catch (Db.CadastroException e) {
                erro = R.string.erro_cidade_indisponivel;
            }
            int mensagem = erro;
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                definirCarregando(false);
                if (mensagem == R.string.erro_senha_atual) {
                    senhaAtualLayout.setError(getString(mensagem));
                } else if (mensagem != 0) {
                    cidadeLayout.setError(getString(mensagem));
                } else {
                    Toast.makeText(this, R.string.dados_atualizados, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private static String valor(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString();
    }

    private boolean verificar(TextInputLayout layout, boolean valido, int mensagem) {
        layout.setError(valido ? null : getString(mensagem));
        return valido;
    }

    private void definirCarregando(boolean carregando) {
        btnSalvar.setEnabled(!carregando);
        btnSalvar.setText(carregando ? R.string.btn_salvando : R.string.btn_salvar);
    }
}
