package com.celectronix.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.celectronix.utils.Config;

/** Methodes communes a toutes les pages. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.timeout()));
    }

    // ---------- Recherche d'elements ----------

    protected List<WebElement> visible(By by) {
        List<WebElement> result = new ArrayList<>();
        for (WebElement e : driver.findElements(by)) {
            try {
                if (e.isDisplayed()) {
                    result.add(e);
                }
            } catch (StaleElementReferenceException ignored) {
                // element disparu pendant le parcours
            }
        }
        return result;
    }

    protected WebElement firstVisible(By by) {
        List<WebElement> list = visible(by);
        return list.isEmpty() ? null : list.get(0);
    }

    protected WebElement waitFirstVisible(By by, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            WebElement e = firstVisible(by);
            if (e != null) {
                return e;
            }
            pause(250);
        }
        return null;
    }

    /** Premier lien (meme masque dans un menu) dont le href est exploitable. */
    protected String hrefOf(By by) {
        for (WebElement e : driver.findElements(by)) {
            try {
                String href = e.getAttribute("href");
                if (href == null || href.isBlank() || href.endsWith("#")) {
                    continue;
                }
                String h = href.toLowerCase();
                boolean unusable = h.startsWith("javascript") || h.startsWith("mailto:") || h.startsWith("tel:")
                    || h.contains("add-to-cart") || h.contains("add_to_cart") || h.contains("action=add")
                    || h.contains("remove_item") || h.contains("wishlist");
                if (!unusable) {
                    return href;
                }
            } catch (StaleElementReferenceException ignored) {
                // on passe au suivant
            }
        }
        return null;
    }

    // ---------- Actions ----------

    protected void scrollTo(WebElement e) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", e);
    }

    /** Pause de demonstration (demo.pause.ms) : permet de suivre le test a l'ecran. 0 = test rapide. */
    protected static void demoPause() {
        String value = com.celectronix.utils.Config.get("demo.pause.ms");
        try {
            if (value != null && Long.parseLong(value.trim()) > 0) {
                pause(Long.parseLong(value.trim()));
            }
        } catch (NumberFormatException ignored) {
            // valeur invalide : pas de pause
        }
    }

    protected void click(WebElement e) {
        scrollTo(e);
        try {
            // l'element qui va etre clique est entoure en rouge pendant la pause
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.outline='4px solid red'; arguments[0].style.outlineOffset='2px';", e);
        } catch (RuntimeException ignored) {
            // surlignage facultatif
        }
        demoPause();
        try {
            e.click();
        } catch (ElementNotInteractableException ex) { // couvre aussi ElementClickInterceptedException
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
        }
    }

    protected boolean isInvalid(WebElement e) {
        Object r = ((JavascriptExecutor) driver).executeScript("return !arguments[0].checkValidity();", e);
        return Boolean.TRUE.equals(r);
    }

    public void waitForPageReady() {
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            Object state = ((JavascriptExecutor) d).executeScript("return document.readyState");
            return "complete".equals(state) || "interactive".equals(state);
        });
        demoPause();
    }

    public void dismissCookieBanner() {
        WebElement banner = firstVisible(Locators.COOKIE_ACCEPT);
        if (banner != null) {
            try {
                click(banner);
                pause(500);
            } catch (Exception ignored) {
                // banniere non bloquante
            }
        }
    }

    /**
     * Bouton d'envoi du formulaire qui contient le champ donne (evite le bouton de la barre de
     * recherche, qui est aussi un bouton "submit"). Repli : premier bouton submit visible de la page.
     */
    protected WebElement submitFor(WebElement field) {
        String[] xpaths = {
            "./ancestor::form[1]//button[@type='submit'] | ./ancestor::form[1]//input[@type='submit']",
            "./ancestor::form[1]//button[not(@type)]"
        };
        for (String xp : xpaths) {
            try {
                for (WebElement e : field.findElements(By.xpath(xp))) {
                    if (e.isDisplayed()) {
                        return e;
                    }
                }
            } catch (Exception ignored) {
                // on essaie l'alternative suivante
            }
        }
        return firstVisible(Locators.SUBMIT_BUTTON);
    }

    /** Adresses des images qui n'ont pas pu se charger sur la page courante (15 maximum). */
    public List<String> brokenImageUrls() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        pause(1500);
        js.executeScript("window.scrollTo(0, 0);");
        pause(500);
        Object result = js.executeScript(
            "return Array.from(document.images).filter(function(i){"
            + "return i.complete && i.naturalWidth===0 && i.src && i.src.indexOf('data:')!==0;})"
            + ".map(function(i){return i.src;}).slice(0,15);");
        List<String> urls = new ArrayList<>();
        if (result instanceof List) {
            for (Object o : (List<?>) result) {
                urls.add(String.valueOf(o));
            }
        }
        return urls;
    }

    // ---------- Etat de la page ----------

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String title() {
        return driver.getTitle();
    }

    public String pageText() {
        try {
            return driver.findElement(By.tagName("body")).getText().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isErrorDisplayed() {
        return waitFirstVisible(Locators.ERROR_MESSAGE, 5) != null;
    }

    protected static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
