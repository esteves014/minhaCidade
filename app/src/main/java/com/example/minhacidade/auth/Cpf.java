package com.example.minhacidade.auth;

public final class Cpf {
    private Cpf() {
    }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static boolean valido(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    /** Dígito verificador calculado sobre os primeiros n dígitos. */
    private static int digito(String d, int n) {
        int soma = 0;
        for (int i = 0; i < n; i++) {
            soma += (d.charAt(i) - '0') * (n + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** 12345678909 -> 123.456.789-09 (aceita CPF parcial enquanto o usuário digita). */
    public static String formatar(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() > 11) {
            d = d.substring(0, 11);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) {
                sb.append('.');
            } else if (i == 9) {
                sb.append('-');
            }
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    /** 12345678909 -> ***.456.789-** (para exibir sem expor o número completo). */
    public static String mascarar(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 11) {
            return "";
        }
        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }
}
