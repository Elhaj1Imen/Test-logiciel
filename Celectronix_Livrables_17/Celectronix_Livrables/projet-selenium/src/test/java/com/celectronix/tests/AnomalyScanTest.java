package com.celectronix.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.celectronix.pages.SearchResultsPage;

/**
 * OUTIL D'EXPLORATION - HORS DES 20 TESTS DU PROJET.
 * Parcourt les premieres categories du menu et releve : produits a 0 TND, images cassees,
 * pages introuvables, categories sans produit. Resultat : reports/anomalies_scan.csv.
 * Lancement : mvn test -Pscan   (ou mvnw.cmd test -Pscan)
 * Chaque ligne du CSV est un INDICE a verifier a la main avant de la noter comme anomalie.
 */
public class AnomalyScanTest extends BaseTest {

    private static String[] row(String type, String page, String detail) {
        return new String[] {type, page, detail};
    }

    @Test(description = "SCAN - Exploration du site pour reperer des anomalies (hors des 20 tests)")
    public void scanSiteForAnomalies() throws IOException {
        List<String[]> findings = new ArrayList<>();
        List<String> categories = home.categoryLinks(10);
        Assert.assertFalse(categories.isEmpty(), "Aucune categorie trouvee dans le menu");

        for (String url : categories) {
            try {
                driver.get(url);
                home.waitForPageReady();
                home.dismissCookieBanner();
            } catch (WebDriverException e) {
                findings.add(row("Erreur de chargement", url, "La page n'a pas pu etre chargee"));
                continue;
            }
            if (home.looksLikeNotFound()) {
                findings.add(row("Page introuvable (404)", url, String.valueOf(driver.getTitle())));
            }
            SearchResultsPage list = new SearchResultsPage(driver);
            if (list.productCount() == 0) {
                findings.add(row("Categorie sans produit reconnu", url, String.valueOf(driver.getTitle())));
            }
            for (String name : list.zeroPriceProductNames()) {
                findings.add(row("Produit a 0 TND", url, name));
            }
            for (String image : list.brokenImageUrls()) {
                findings.add(row("Image cassee", url, image));
            }
        }

        Path out = Paths.get("reports", "anomalies_scan.csv");
        Files.createDirectories(out.getParent());
        List<String> lines = new ArrayList<>();
        lines.add("Type;Page;Detail");
        for (String[] f : findings) {
            lines.add("\"" + f[0] + "\";\"" + f[1] + "\";\"" + f[2].replace("\"", "'") + "\"");
        }
        Files.write(out, lines, StandardCharsets.UTF_8);
        System.out.println("SCAN TERMINE : " + findings.size() + " indice(s) -> " + out.toAbsolutePath());
    }
}
