package com.example.minhacidade.database;

import android.database.Cursor;

/** Leitura de colunas pelo nome, usada pelos repositórios ao converter um Cursor em objeto do model. */
public final class CursorUtil {
    private CursorUtil() {
    }

    public static String texto(Cursor c, String coluna) {
        return c.getString(c.getColumnIndexOrThrow(coluna));
    }

    public static long inteiro(Cursor c, String coluna) {
        return c.getLong(c.getColumnIndexOrThrow(coluna));
    }

    /** Coluna REAL que pode ser nula. */
    public static Double real(Cursor c, String coluna) {
        int i = c.getColumnIndexOrThrow(coluna);
        return c.isNull(i) ? null : c.getDouble(i);
    }
}
