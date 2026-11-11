package com.celectronix.pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/** Controles de securite et de conformite : HTTPS, contenu mixte, pages d'erreur sans trace technique. */
public class SecurityPage extends BasePage {

    /** Termes qui ne doivent jamais apparaitre dans une page d'erreur destinee au public. */
    private static final String[] TECHNICAL_MARKERS = {
        "stack trace", "stacktrace", "exception", "sqlstate", "fatal error", "warning:", "notice:",
        "on line", "traceback", "mysql", "pdoexception", "smarty", "at java.", "undefined variable",
        "internal server error", "/var/www", "c:\\\\"
    };

    private static final String MIXED_CONTENT_SCRIPT =
        "var bad = [];"
        + "performance.getEntriesByType('resource').forEach(function (e) { if (e.name.indexOf('http://') === 0) bad.push(e.name); });"
        + "document.querySelectorAll('[src],[href]').forEach(function (n) {"
        + "  var u = n.getAttribute('src') || n.getAttribute('href') || '';"
        + "  if (u.indexOf('http://') === 0 && n.tagName !== 'A') bad.push(u); });"
        + "return bad;";

    public SecurityPage(WebDriver driver) {
        super(driver);
    }

    /** Ouvre l'adresse puis retourne les anomalies HTTPS (adresse non securisee, ressources http://). */
    @SuppressWarnings("unchecked")
    public List<String> httpsProblemsOn(String url) {
        List<String> problems = new ArrayList<>();
        driver.get(url);
        waitForPageReady();
        pause(1500);
        if (!currentUrl().startsWith("https://")) {
            problems.add(url + " ouverte en " + currentUrl() + " (pas en HTTPS)");
        }
        Object result = ((JavascriptExecutor) driver).executeScript(MIXED_CONTENT_SCRIPT);
        if (result instanceof List) {
            for (Object o : (List<Object>) result) {
                problems.add("contenu mixte sur " + url + " : " + o);
            }
        }
        return problems;
    }

    /** Ouvre l'adresse en HTTP simple : le site doit rediriger vers HTTPS. */
    public boolean redirectsToHttps(String httpUrl) {
        driver.get(httpUrl);
        waitForPageReady();
        return currentUrl().startsWith("https://");
    }

    /** Termes techniques (trace de pile, erreur SQL, chemin de fichier...) visibles dans la page courante. */
    public List<String> technicalDetailsShown() {
        String text = pageText();
        List<String> found = new ArrayList<>();
        for (String marker : TECHNICAL_MARKERS) {
            if (text.contains(marker)) {
                found.add(marker);
            }
        }
        return found;
    }
}
