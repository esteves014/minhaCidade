package com.example.minhacidade.ui;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.example.minhacidade.data.Cidade;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

/** Lista suspensa com as cidades que contrataram o serviço. */
final class CidadesDropdown {
    private final Context contexto;
    private final MaterialAutoCompleteTextView campo;
    private List<Cidade> cidades = new ArrayList<>();

    CidadesDropdown(Context contexto, MaterialAutoCompleteTextView campo) {
        this.contexto = contexto;
        this.campo = campo;
    }

    /** Define as cidades disponíveis; se inicialId existir na lista, ela já vem escolhida. */
    void definir(List<Cidade> disponiveis, long inicialId) {
        cidades = disponiveis;
        List<String> nomes = new ArrayList<>();
        for (Cidade c : cidades) {
            nomes.add(c.getNomeUf());
            if (c.id == inicialId) {
                campo.setText(c.getNomeUf(), false);
            }
        }
        campo.setAdapter(new ArrayAdapter<>(contexto, android.R.layout.simple_dropdown_item_1line, nomes));
    }

    boolean vazia() {
        return cidades.isEmpty();
    }

    /** Cidade que corresponde ao texto do campo, ou null se nada válido foi escolhido. */
    @Nullable
    Cidade selecionada() {
        String texto = Ui.texto(campo);
        for (Cidade c : cidades) {
            if (c.getNomeUf().equals(texto)) {
                return c;
            }
        }
        return null;
    }
}
