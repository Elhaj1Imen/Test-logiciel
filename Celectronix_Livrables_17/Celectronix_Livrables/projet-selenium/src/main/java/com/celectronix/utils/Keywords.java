package com.celectronix.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Mots-cles de recherche varies : chaque test a son propre mot-cle (testdata/keywords.csv).
 * Si le catalogue ne donne rien pour ce mot, les autres mots du fichier sont essayes ensuite
 * (puis search.keyword de config.properties) : un test ne tombe donc jamais en echec a cause d'un mot mal choisi.
 */
public final class Keywords {

    private Keywords() { }

    /** Uniquement les mots-cles du test (sans repli) : pour les tests qui verifient plusieurs recherches precises. */
    public static List<String> own(String testId) {
        List<String> list = new ArrayList<>();
        for (Object[] row : CsvReader.read("testdata/keywords.csv")) {
            if (String.valueOf(row[0]).equalsIgnoreCase(testId)) {
                list.add(String.valueOf(row[1]));
            }
        }
        return list;
    }

    public static List<String> candidates(String testId) {
        Object[][] rows = CsvReader.read("testdata/keywords.csv");
        Set<String> ordered = new LinkedHashSet<>();
        for (Object[] row : rows) {
            if (String.valueOf(row[0]).equalsIgnoreCase(testId)) {
                ordered.add(String.valueOf(row[1]));
            }
        }
        for (Object[] row : rows) {
            ordered.add(String.valueOf(row[1]));
        }
        String fallback = Config.get("search.keyword");
        if (fallback != null && !fallback.isBlank()) {
            ordered.add(fallback.trim());
        }
        return new ArrayList<>(ordered);
    }
}
