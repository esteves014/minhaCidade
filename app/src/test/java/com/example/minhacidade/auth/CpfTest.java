package com.example.minhacidade.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CpfTest {
    @Test
    public void aceitaCpfValidoComESemMascara() {
        assertTrue(Cpf.valido("52998224725"));
        assertTrue(Cpf.valido("529.982.247-25"));
        // CPFs dos cidadãos de exemplo do SeedData
        assertTrue(Cpf.valido("11144477735"));
        assertTrue(Cpf.valido("12345678909"));
    }

    @Test
    public void rejeitaDigitoVerificadorErrado() {
        assertFalse(Cpf.valido("52998224726"));
    }

    @Test
    public void rejeitaSequenciasRepetidasETamanhoErrado() {
        assertFalse(Cpf.valido("11111111111"));
        assertFalse(Cpf.valido("1234567890"));
        assertFalse(Cpf.valido(""));
        assertFalse(Cpf.valido(null));
    }

    @Test
    public void formataEnquantoDigita() {
        assertEquals("529", Cpf.formatar("529"));
        assertEquals("529.98", Cpf.formatar("52998"));
        assertEquals("529.982.247-25", Cpf.formatar("52998224725"));
        assertEquals("529.982.247-25", Cpf.formatar("529982247259999"));
    }

    @Test
    public void mascaraSemExporONumeroCompleto() {
        assertEquals("***.982.247-**", Cpf.mascarar("52998224725"));
        assertEquals("", Cpf.mascarar("123"));
    }
}
