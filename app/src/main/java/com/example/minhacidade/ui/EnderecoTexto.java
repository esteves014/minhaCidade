package com.example.minhacidade.ui;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** Transforma uma posição (latitude/longitude) em um endereço legível: "Rua X, 123 - Bairro". */
final class EnderecoTexto {
    private EnderecoTexto() {
    }

    /**
     * Monta o texto a partir dos pedaços que o geocoder devolve. Sem rua, usa a linha completa;
     * sem nada, devolve null.
     */
    static String formatar(String rua, String numero, String bairro, String linhaCompleta) {
        if (vazio(rua)) {
            return vazio(linhaCompleta) ? null : linhaCompleta.trim();
        }
        StringBuilder sb = new StringBuilder(rua.trim());
        if (!vazio(numero)) {
            sb.append(", ").append(numero.trim());
        }
        if (!vazio(bairro)) {
            sb.append(" - ").append(bairro.trim());
        }
        return sb.toString();
    }

    /** Usado quando o aparelho não consegue traduzir a posição em endereço. */
    static String coordenadas(double latitude, double longitude) {
        return String.format(Locale.ROOT, "%.5f, %.5f", latitude, longitude);
    }

    /**
     * Pergunta ao geocoder do aparelho qual é o endereço da posição. Bloqueia (rede), então não
     * chame na thread principal. Devolve null se não houver geocoder ou resultado.
     */
    @SuppressWarnings("deprecation") // A versão síncrona funciona em todas as APIs que suportamos.
    static String resolver(Context contexto, double latitude, double longitude) {
        if (!Geocoder.isPresent()) {
            return null;
        }
        try {
            List<Address> encontrados = new Geocoder(contexto, Locale.forLanguageTag("pt-BR"))
                    .getFromLocation(latitude, longitude, 1);
            if (encontrados == null || encontrados.isEmpty()) {
                return null;
            }
            Address a = encontrados.get(0);
            return formatar(a.getThoroughfare(), a.getSubThoroughfare(), a.getSubLocality(),
                    a.getMaxAddressLineIndex() >= 0 ? a.getAddressLine(0) : null);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
