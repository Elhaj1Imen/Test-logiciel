package com.celectronix.pages;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.celectronix.utils.Config;

public class HomePage extends BasePage {

    private static final String[] EXCLUDED = {
        "login", "connexion", "account", "compte", "cart", "panier", "contact",
        "register", "inscription", "wishlist", "checkout", "commande"
    };

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        driver.get(Config.baseUrl());
        waitForPageReady();
        dismissCookieBanner();
        return this;
    }

    // ----- Logo / menu -----

    public boolean isLogoDisplayed() {
        return firstVisible(Locators.LOGO) != null;
    }

    public void clickLogo() {
        WebElement logo = firstVisible(Locators.LOGO);
        if (logo == null) {
            throw new NoSuchElementException("Logo introuvable (adapter Locators.LOGO)");
        }
        click(logo);
        pause(1000);
        waitForPageReady();
    }

    private List<WebElement> visibleMenuLinks() {
        List<WebElement> links = new ArrayList<>();
        for (WebElement l : visible(Locators.MENU_LINKS)) {
            if (!l.getText().isBlank()) {
                links.add(l);
            }
        }
        return links;
    }

    public int menuLinkCount() {
        return visibleMenuLinks().size();
    }

    /** Ouvre la premiere categorie du menu et retourne son URL (null si aucune). */
    public String openFirstCategory() {
        String home = Config.baseUrl();
        String homeNoSlash = home.substring(0, home.length() - 1);
        String host = URI.create(home).getHost();
        for (WebElement link : visibleMenuLinks()) {
            String href = link.getAttribute("href");
            if (href == null || href.contains("#") || href.startsWith("javascript")
                    || href.equals(home) || href.equals(homeNoSlash)) {
                continue;
            }
            String lower = href.toLowerCase();
            boolean excluded = false;
            for (String word : EXCLUDED) {
                if (lower.contains(word)) {
                    excluded = true;
                    break;
                }
            }
            try {
                if (excluded || !host.equals(URI.create(href).getHost())) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
            driver.get(href);
            waitForPageReady();
            return href;
        }
        return null;
    }

    // ----- Recherche -----

    public SearchResultsPage search(String keyword) {
        WebElement input = firstVisible(Locators.SEARCH_INPUT);
        if (input == null) {
            WebElement toggle = firstVisible(Locators.SEARCH_TOGGLE);
            if (toggle != null) {
                click(toggle);
                input = waitFirstVisible(Locators.SEARCH_INPUT, 5);
            }
        }
        if (input == null) {
            throw new NoSuchElementException("Champ de recherche introuvable (adapter Locators.SEARCH_INPUT)");
        }
        input.clear();
        input.sendKeys(keyword);
        input.sendKeys(Keys.ENTER);
        pause(1500);
        waitForPageReady();
        return new SearchResultsPage(driver);
    }

    // ----- Liens utiles (href lu dans la page courante) -----

    public String loginHref() {
        return hrefOf(Locators.LOGIN_LINK);
    }

    public String registerHref() {
        return hrefOf(Locators.REGISTER_LINK);
    }

    public String cartHref() {
        return hrefOf(Locators.CART_LINK);
    }

    public String contactHref() {
        return hrefOf(Locators.CONTACT_LINK);
    }

    public int footerLinkCount() {
        return visible(Locators.FOOTER_LINKS).size();
    }

    public boolean isFooterPresent() {
        return !driver.findElements(Locators.FOOTER_LINKS).isEmpty();
    }

    /** Liens internes (meme site) du pied de page, sans doublon, limites a {@code max}. */
    public List<String> footerPageLinks(int max) {
        String host = URI.create(Config.baseUrl()).getHost();
        Set<String> links = new LinkedHashSet<>();
        for (WebElement a : driver.findElements(Locators.FOOTER_LINKS)) {
            String href;
            try {
                href = a.getAttribute("href");
            } catch (Exception e) {
                continue;
            }
            if (href == null) {
                continue;
            }
            String lower = href.toLowerCase();
            if (!lower.startsWith("http") || lower.contains("#") || lower.contains("logout")
                    || lower.contains("deconnexion") || lower.contains("add-to-cart")) {
                continue;
            }
            try {
                if (!host.equals(URI.create(href).getHost())) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
            links.add(href);
            if (links.size() >= max) {
                break;
            }
        }
        return new ArrayList<>(links);
    }

    /** Vrai si le titre ou le h1 de la page courante indique une page introuvable (404). */
    public boolean looksLikeNotFound() {
        String text = title() == null ? "" : title().toLowerCase();
        try {
            text = text + " " + driver.findElement(By.tagName("h1")).getText().toLowerCase();
        } catch (Exception ignored) {
            // pas de h1
        }
        String[] markers = {"404", "not found", "introuvable", "page non trouv", "n'existe pas"};
        for (String m : markers) {
            if (text.contains(m)) {
                return true;
            }
        }
        return false;
    }

    /** Adresses des premieres categories du menu (meme site, sans doublon). */
    public List<String> categoryLinks(int max) {
        String home = Config.baseUrl();
        String homeNoSlash = home.substring(0, home.length() - 1);
        String host = URI.create(home).getHost();
        Set<String> out = new LinkedHashSet<>();
        for (WebElement link : visibleMenuLinks()) {
            String href = link.getAttribute("href");
            if (href == null || href.contains("#") || href.startsWith("javascript")
                    || href.equals(home) || href.equals(homeNoSlash)) {
                continue;
            }
            String lower = href.toLowerCase();
            boolean excluded = false;
            for (String word : EXCLUDED) {
                if (lower.contains(word)) {
                    excluded = true;
                    break;
                }
            }
            try {
                if (excluded || !host.equals(URI.create(href).getHost())) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
            out.add(href);
            if (out.size() >= max) {
                break;
            }
        }
        return new ArrayList<>(out);
    }
}
