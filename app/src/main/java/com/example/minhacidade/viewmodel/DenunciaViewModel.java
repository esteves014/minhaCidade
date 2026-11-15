package com.example.minhacidade.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.minhacidade.model.Denuncia;
import com.example.minhacidade.model.Parecer;
import com.example.minhacidade.model.Usuario;
import com.example.minhacidade.repository.DenunciaRepository;

import java.util.List;

public class DenunciaViewModel extends AndroidViewModel {
    private final DenunciaRepository repository;
    private final MutableLiveData<List<Denuncia>> denuncias = new MutableLiveData<>();
    private final MutableLiveData<Denuncia> denuncia = new MutableLiveData<>();
    private final MutableLiveData<List<Parecer>> pareceres = new MutableLiveData<>();
    private final MutableLiveData<Integer> naoLidos = new MutableLiveData<>(0);

    public DenunciaViewModel(@NonNull Application application) {
        super(application);
        repository = new DenunciaRepository(application);
    }

    public LiveData<List<Denuncia>> getDenuncias() {
        return denuncias;
    }

    public LiveData<Denuncia> getDenuncia() {
        return denuncia;
    }

    public LiveData<List<Parecer>> getPareceres() {
        return pareceres;
    }

    /** Pareceres ainda não lidos em todas as denúncias do cidadão (selo da aba). */
    public LiveData<Integer> getNaoLidos() {
        return naoLidos;
    }

    public void carregarLista(long usuarioId) {
        denuncias.setValue(repository.listar(usuarioId));
    }

    /** Publica null quando a denúncia não existe. */
    public void carregarDenuncia(long id) {
        denuncia.setValue(repository.buscar(id));
    }

    public void carregarPareceres(long denunciaId) {
        pareceres.setValue(repository.listarPareceres(denunciaId));
    }

    public void carregarNaoLidos(long usuarioId) {
        naoLidos.setValue(repository.contarPareceresNaoLidos(usuarioId));
    }

    /** Grava a denúncia e devolve a criada (o protocolo é exibido ao cidadão). */
    public Denuncia inserir(Usuario usuario, String categoria, String endereco, String descricao,
                            String fotoPath, Double latitude, Double longitude) {
        Denuncia criada = repository.inserir(usuario, categoria, endereco, descricao, fotoPath, latitude, longitude);
        carregarLista(usuario.id);
        return criada;
    }

    /** Registra o parecer, atualiza a denúncia e as listas observadas; devolve o parecer gravado. */
    public Parecer registrarParecer(long denunciaId, String novoStatus, String texto) {
        Parecer parecer = repository.registrarParecer(denunciaId, novoStatus, texto);
        carregarDenuncia(denunciaId);
        carregarPareceres(denunciaId);
        return parecer;
    }

    /** Marca como lidos sem recarregar a lista (a tela decide quando mostrar o "novo"). */
    public void marcarPareceresLidos(long denunciaId) {
        repository.marcarPareceresLidos(denunciaId);
    }
}
