package com.example.minhacidade.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.minhacidade.model.Vaga;
import com.example.minhacidade.repository.VagaRepository;

import java.util.List;

public class VagaViewModel extends AndroidViewModel {
    private final VagaRepository repository;
    private final MutableLiveData<List<Vaga>> vagas = new MutableLiveData<>();
    private final MutableLiveData<Vaga> vaga = new MutableLiveData<>();

    public VagaViewModel(@NonNull Application application) {
        super(application);
        repository = new VagaRepository(application);
    }

    public LiveData<List<Vaga>> getVagas() {
        return vagas;
    }

    public LiveData<Vaga> getVaga() {
        return vaga;
    }

    public void carregarLista(long cidadeId, String busca, String tipo) {
        vagas.setValue(repository.listar(cidadeId, busca, tipo));
    }

    /** Publica null quando a vaga não existe. */
    public void carregarVaga(long id) {
        vaga.setValue(repository.buscar(id));
    }
}
