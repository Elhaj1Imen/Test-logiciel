package com.celectronix.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/** Liste de produits (resultats de recherche ou page de categorie). */
public class SearchResultsPage extends BasePage {

    private static final String[] NO_RESULT_WORDS = {
        "aucun produit", "aucun résultat", "aucun resultat", "no products", "no results",
        "nothing found", "0 résultat", "0 resultat", "introuvable", "ne correspond"
    };

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public int productCount() {
        return visible(Locators.PRODUCT_CARD).size();
    }

    public boolean hasNoResultMessage() {
        if (firstVisible(Locators.NO_RESULT_MESSAGE) != null) {
            return true;
        }
        String text = pageText();
        for (String w : NO_RESULT_WORDS) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }

    public boolean firstProductHasLink() {
        List<WebElement> cards = visible(Locators.PRODUCT_CARD);
        return !cards.isEmpty() && !cards.get(0).findElements(Locators.PRODUCT_CARD_LINK).isEmpty();
    }

    /** Ouvre la premiere fiche produit de la liste. */
    public ProductPage openFirstProduct() {
        List<WebElement> cards = visible(Locators.PRODUCT_CARD);
        if (cards.isEmpty()) {
            throw new NoSuchElementException("Aucun produit dans la liste (adapter Locators.PRODUCT_CARD)");
        }
        String href = null;
        for (WebElement a : cards.get(0).findElements(Locators.PRODUCT_CARD_LINK)) {
            String h = a.getAttribute("href");
            if (h != null && !h.isBlank() && !h.endsWith("#")) {
                href = h;
                break;
            }
        }
        if (href == null) {
            throw new NoSuchElementException("Lien du premier produit introuvable");
        }
        driver.get(href);
        waitForPageReady();
        return new ProductPage(driver);
    }

    public boolean isSortPresent() {
        return firstVisible(Locators.SORT_SELECT) != null;
    }

    /** Choisit la 2e option du tri et retourne true si l'action a pu etre faite. */
    public boolean changeSortOrder() {
        WebElement el = firstVisible(Locators.SORT_SELECT);
        if (el == null) {
            return false;
        }
        Select select = new Select(el);
        if (select.getOptions().size() < 2) {
            return false;
        }
        select.selectByIndex(1);
        pause(1500);
        waitForPageReady();
        return true;
    }

    public boolean isFilterPresent() {
        return firstVisible(Locators.FILTER_BLOCK) != null;
    }

    private static final Pattern PRICE =
        Pattern.compile("(\\d[\\d\\s]*(?:[.,]\\d+)?)\\s?(?:tnd|dt)", Pattern.CASE_INSENSITIVE);

    /** Prix lisible de chaque produit affiche (le plus bas si ancien/nouveau prix ; prix nuls ignores). */
    public List<Double> visiblePrices() {
        List<Double> prices = new ArrayList<>();
        for (WebElement card : visible(Locators.PRODUCT_CARD)) {
            Matcher m = PRICE.matcher(card.getText());
            double best = Double.MAX_VALUE;
            boolean found = false;
            while (m.find()) {
                String raw = m.group(1).replaceAll("\\s", "").replace(',', '.');
                try {
                    double value = Double.parseDouble(raw);
                    if (value > 0) {
                        best = Math.min(best, value);
                        found = true;
                    }
                } catch (NumberFormatException ignored) {
                    // format de prix non reconnu : ignore
                }
            }
            if (found) {
                prices.add(best);
            }
        }
        return prices;
    }

    /** Choisit l'option de tri "prix croissant" si elle existe. */
    public boolean sortByPriceAscending() {
        WebElement el = firstVisible(Locators.SORT_SELECT);
        if (el == null) {
            return false;
        }
        Select select = new Select(el);
        for (WebElement option : select.getOptions()) {
            String t = option.getText().toLowerCase();
            if (t.contains("prix") && (t.contains("croissant") || t.contains("asc") || t.contains("bas")
                    || t.contains("low") || t.contains("moins"))) {
                select.selectByVisibleText(option.getText());
                pause(2500);
                waitForPageReady();
                return true;
            }
        }
        return false;
    }

    private static final Pattern ZERO_PRICE =
        Pattern.compile("(?<![\\d.,])0(?:[.,]0+)?\\s?(?:tnd|dt)", Pattern.CASE_INSENSITIVE);

    /** Noms des produits de la liste dont le prix affiche est 0 TND. */
    public List<String> zeroPriceProductNames() {
        List<String> names = new ArrayList<>();
        for (WebElement card : visible(Locators.PRODUCT_CARD)) {
            String text = card.getText();
            if (ZERO_PRICE.matcher(text).find()) {
                String name = text.lines().map(String::trim).filter(l -> !l.isBlank()).findFirst().orElse("?");
                names.add(name);
            }
        }
        return names;
    }
}
