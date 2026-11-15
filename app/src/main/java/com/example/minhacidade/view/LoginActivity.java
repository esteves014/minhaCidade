package com.example.minhacidade.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.viewmodel.UsuarioViewModel;
import com.example.minhacidade.auth.Cpf;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.repository.Sessao;
import com.example.minhacidade.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private UsuarioViewModel viewModel;

    private TextInputLayout cpfLayout;
    private TextInputLayout senhaLayout;
    private TextInputEditText cpf;
    private TextInputEditText senha;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        viewModel = new ViewModelProvider(this).get(UsuarioViewModel.class);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);

        cpfLayout = findViewById(R.id.cpf_layout);
        senhaLayout = findViewById(R.id.senha_layout);
        cpf = findViewById(R.id.cpf);
        senha = findViewById(R.id.senha);
        btnEntrar = findViewById(R.id.btn_entrar);

        Ui.mascararCpf(cpf);
        Ui.aoDigitar(cpf, () -> cpfLayout.setError(null));
        Ui.aoDigitar(senha, () -> senhaLayout.setError(null));
        btnEntrar.setOnClickListener(v -> entrar());
        findViewById(R.id.btn_cadastro).setOnClickListener(
                v -> startActivity(new Intent(this, CadastroActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void entrar() {
        String digitos = Cpf.somenteDigitos(Ui.texto(cpf));
        String senhaDigitada = senha.getText() == null ? "" : senha.getText().toString();

        boolean cpfOk = Cpf.valido(digitos);
        cpfLayout.setError(cpfOk ? null : getString(R.string.erro_cpf_invalido));
        senhaLayout.setError(senhaDigitada.isEmpty() ? getString(R.string.erro_senha_obrigatoria) : null);
        if (!cpfOk || senhaDigitada.isEmpty()) {
            return;
        }

        definirCarregando(true);
        // A conferência de senha é lenta de propósito (PBKDF2), então roda fora da thread principal.
        executor.execute(() -> {
            Usuario usuario = viewModel.autenticar(digitos, senhaDigitada);
            Cidade cidade = usuario == null ? null : viewModel.buscarCidade(usuario.cidadeId);
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                definirCarregando(false);
                if (usuario == null) {
                    senhaLayout.setError(getString(R.string.login_falhou));
                } else if (cidade == null || !cidade.contratada) {
                    Toast.makeText(this, R.string.login_cidade_sem_contrato, Toast.LENGTH_LONG).show();
                } else {
                    Sessao.entrar(this, usuario.id);
                    Ui.abrirPrincipal(this);
                }
            });
        });
    }

    private void definirCarregando(boolean carregando) {
        btnEntrar.setEnabled(!carregando);
        btnEntrar.setText(carregando ? R.string.btn_entrando : R.string.btn_entrar);
    }
}
