package com.celectronix.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Lit un CSV place dans src/test/resources (la 1re ligne est l'en-tete). "<EMPTY>" = chaine vide. */
public final class CsvReader {

    private CsvReader() { }

    public static Object[][] read(String resourcePath) {
        List<Object[]> rows = new ArrayList<>();
        try (InputStream in = CsvReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Fichier de donnees introuvable : " + resourcePath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line = br.readLine(); // en-tete
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] cells = line.split(",", -1);
                    Object[] row = new Object[cells.length];
                    for (int i = 0; i < cells.length; i++) {
                        String c = cells[i].trim();
                        row[i] = "<EMPTY>".equals(c) ? "" : c;
                    }
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lecture CSV impossible : " + resourcePath, e);
        }
        return rows.toArray(new Object[0][]);
    }
}
