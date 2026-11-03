package com.example.minhacidade.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.minhacidade.auth.Senha;

import org.junit.Test;

public class SeedDataTest {
    @Test
    public void hashDosCidadaosDeExemploConfereComASenhaDemo() {
        assertTrue(Senha.confere(SeedData.DEMO_SENHA, SeedData.DEMO_SALT, SeedData.DEMO_HASH));
        assertFalse(Senha.confere("outra-senha", SeedData.DEMO_SALT, SeedData.DEMO_HASH));
    }

    @Test
    public void senhasIguaisGeramHashesDiferentesPorCausaDoSalt() {
        Senha.Hash a = Senha.criar("segredo123");
        Senha.Hash b = Senha.criar("segredo123");
        assertFalse(a.hash.equals(b.hash));
        assertTrue(Senha.confere("segredo123", a.salt, a.hash));
    }
}
