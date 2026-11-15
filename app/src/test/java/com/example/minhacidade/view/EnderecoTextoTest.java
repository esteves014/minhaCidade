package com.example.minhacidade.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EnderecoTextoTest {
    @Test
    public void montaRuaNumeroEBairro() {
        assertEquals("Rua das Flores, 100 - Centro",
                EnderecoTexto.formatar("Rua das Flores", "100", "Centro", "linha completa"));
    }

    @Test
    public void omiteNumeroEBairroQuandoFaltam() {
        assertEquals("Rua das Flores", EnderecoTexto.formatar(" Rua das Flores ", null, " ", null));
        assertEquals("Rua das Flores, 100", EnderecoTexto.formatar("Rua das Flores", "100", null, null));
    }

    @Test
    public void semRuaUsaALinhaCompleta() {
        assertEquals("Centro, Lins", EnderecoTexto.formatar(null, "100", "Centro", " Centro, Lins "));
    }

    @Test
    public void semNadaDevolveNull() {
        assertNull(EnderecoTexto.formatar(null, null, null, null));
        assertNull(EnderecoTexto.formatar("", "1", "B", ""));
    }

    @Test
    public void coordenadasUsamPontoDecimal() {
        assertEquals("-21.67860, -49.74250", EnderecoTexto.coordenadas(-21.6786, -49.7425));
    }
}
