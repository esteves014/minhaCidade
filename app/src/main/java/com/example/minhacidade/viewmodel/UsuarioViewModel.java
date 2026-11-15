package com.example.minhacidade.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.minhacidade.auth.Senha;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.model.Usuario;
import com.example.minhacidade.repository.CidadeRepository;
import com.example.minhacidade.repository.UsuarioRepository;

import java.util.List;

/**
 * Cidadão e cidades. Estas operações são síncronas porque as telas de login, cadastro e perfil as
 * executam fora da thread principal (o hash de senha é lento de propósito).
 */
public class UsuarioViewModel extends AndroidViewModel {
    private final UsuarioRepository usuarios;
    private final CidadeRepository cidades;

    public UsuarioViewModel(@NonNull Application application) {
        super(application);
        usuarios = new UsuarioRepository(application);
        cidades = new CidadeRepository(application);
    }

    public List<Cidade> listarCidadesContratadas() {
        return cidades.listarContratadas();
    }

    public Cidade buscarCidade(long id) {
        return cidades.buscar(id);
    }

    public Usuario autenticar(String cpf, String senha) {
        return usuarios.autenticar(cpf, senha);
    }

    public Usuario cadastrar(String cpf, String nome, String email, long cidadeId, Senha.Hash senha)
            throws UsuarioRepository.CadastroException {
        return usuarios.cadastrar(cpf, nome, email, cidadeId, senha);
    }

    public Usuario atualizar(long id, String nome, String email, String telefone, String endereco,
                             Double latitude, Double longitude, long cidadeId)
            throws UsuarioRepository.CadastroException {
        return usuarios.atualizar(id, nome, email, telefone, endereco, latitude, longitude, cidadeId);
    }

    public void atualizarSenha(long id, Senha.Hash senha) {
        usuarios.atualizarSenha(id, senha);
    }
}
