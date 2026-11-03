package com.example.minhacidade.auth;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de senha com PBKDF2 e salt aleatório. Lento de propósito: não chame na thread principal. */
public final class Senha {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 100_000;
    private static final int BITS = 256;
    private static final int BYTES_SALT = 16;

    private Senha() {
    }

    public static final class Hash {
        public final String salt;
        public final String hash;

        Hash(String salt, String hash) {
            this.salt = salt;
            this.hash = hash;
        }
    }

    public static Hash criar(String senha) {
        byte[] salt = new byte[BYTES_SALT];
        new SecureRandom().nextBytes(salt);
        return new Hash(codificar(salt), codificar(derivar(senha, salt)));
    }

    public static boolean confere(String senha, String salt, String hashEsperado) {
        byte[] obtido = derivar(senha, Base64.getDecoder().decode(salt));
        return MessageDigest.isEqual(obtido, Base64.getDecoder().decode(hashEsperado));
    }

    private static byte[] derivar(String senha, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, ITERACOES, BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível", e);
        }
    }

    private static String codificar(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
