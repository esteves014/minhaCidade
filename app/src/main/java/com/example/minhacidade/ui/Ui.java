package com.example.minhacidade.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.minhacidade.R;
import com.example.minhacidade.auth.Cpf;
import com.example.minhacidade.data.Denuncia;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.function.Consumer;

public final class Ui {
    static final String EXTRA_ID = "id";

    private Ui() {
    }

    /** Aplica o espaço das barras do sistema como padding (modo edge-to-edge). */
    public static void aplicarInsets(View view, boolean topo, boolean base) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(barras.left, topo ? barras.top : 0, barras.right, base ? barras.bottom : 0);
            return insets;
        });
    }

    static CharSequence data(long millis) {
        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
    }

    static void abrir(Context contexto, Intent intent) {
        try {
            contexto.startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(contexto, R.string.nenhum_app, Toast.LENGTH_SHORT).show();
        }
    }

    static void aoDigitar(EditText campo, Runnable acao) {
        campo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                acao.run();
            }
        });
    }

    /** Formata o campo como CPF (000.000.000-00) enquanto o usuário digita. */
    static void mascararCpf(EditText campo) {
        campo.addTextChangedListener(new TextWatcher() {
            private boolean ajustando;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ajustando) {
                    return;
                }
                ajustando = true;
                String formatado = Cpf.formatar(s.toString());
                if (!formatado.contentEquals(s)) {
                    s.replace(0, s.length(), formatado);
                }
                ajustando = false;
            }
        });
    }

    /** Abre a tela principal descartando as telas de login/cadastro da pilha. */
    static void abrirPrincipal(android.app.Activity origem) {
        origem.startActivity(new Intent(origem, com.example.minhacidade.MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        origem.finish();
    }

    /** Abre o login descartando o resto da pilha (usado ao sair da conta). */
    static void abrirLogin(android.app.Activity origem) {
        origem.startActivity(new Intent(origem, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        origem.finish();
    }

    static String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    static boolean emailValido(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Preenche o grupo com chips de seleção única. A primeira opção ("Todas") chama o
     * callback com null; as demais, com o próprio texto. O callback não é chamado na montagem.
     */
    static void montarFiltros(ChipGroup grupo, String rotuloTodas, List<String> opcoes,
                              Consumer<String> aoMudar) {
        grupo.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(grupo.getContext());
        adicionarChip(inflater, grupo, rotuloTodas, null).setChecked(true);
        for (String opcao : opcoes) {
            adicionarChip(inflater, grupo, opcao, opcao);
        }
        grupo.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) {
                return;
            }
            Chip escolhido = g.findViewById(ids.get(0));
            aoMudar.accept((String) escolhido.getTag());
        });
    }

    private static Chip adicionarChip(LayoutInflater inflater, ChipGroup grupo, String texto,
                                      @Nullable String valor) {
        Chip chip = (Chip) inflater.inflate(R.layout.chip_filtro, grupo, false);
        chip.setId(View.generateViewId());
        chip.setText(texto);
        chip.setTag(valor);
        grupo.addView(chip);
        return chip;
    }

    /** Ajusta texto e cor do selo de status de uma denúncia. */
    static void aplicarStatus(TextView selo, String status) {
        int texto;
        int cor;
        if (Denuncia.RESOLVIDA.equals(status)) {
            texto = R.string.status_resolvida;
            cor = R.color.status_resolvida;
        } else if (Denuncia.EM_ANALISE.equals(status)) {
            texto = R.string.status_em_analise;
            cor = R.color.status_em_analise;
        } else {
            texto = R.string.status_aberta;
            cor = R.color.status_aberta;
        }
        selo.setText(texto);
        selo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(selo.getContext(), cor)));
    }

    /** Carrega uma imagem reduzida (lado maior limitado a maxLado) para não estourar a memória. */
    @Nullable
    static Bitmap carregarBitmap(String caminho, int maxLado) {
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(caminho, limites);
        int maior = Math.max(limites.outWidth, limites.outHeight);
        int amostra = 1;
        while (maior / (amostra * 2) >= maxLado) {
            amostra *= 2;
        }
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = amostra;
        return BitmapFactory.decodeFile(caminho, opcoes);
    }
}
