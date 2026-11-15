package com.example.minhacidade.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.minhacidade.model.Noticia;
import com.example.minhacidade.repository.NoticiaRepository;

import java.util.List;

public class NoticiaViewModel extends AndroidViewModel {
    private final NoticiaRepository repository;
    private final MutableLiveData<List<Noticia>> noticias = new MutableLiveData<>();
    private final MutableLiveData<List<String>> jornais = new MutableLiveData<>();
    private final MutableLiveData<Noticia> noticia = new MutableLiveData<>();

    public NoticiaViewModel(@NonNull Application application) {
        super(application);
        repository = new NoticiaRepository(application);
    }

    public LiveData<List<Noticia>> getNoticias() {
        return noticias;
    }

    public LiveData<List<String>> getJornais() {
        return jornais;
    }

    public LiveData<Noticia> getNoticia() {
        return noticia;
    }

    public void carregarJornais(long cidadeId) {
        jornais.setValue(repository.listarJornais(cidadeId));
    }

    public void carregarLista(long cidadeId, String jornal) {
        noticias.setValue(repository.listar(cidadeId, jornal));
    }

    /** Publica null quando a notícia não existe. */
    public void carregarNoticia(long id) {
        noticia.setValue(repository.buscar(id));
    }
}
