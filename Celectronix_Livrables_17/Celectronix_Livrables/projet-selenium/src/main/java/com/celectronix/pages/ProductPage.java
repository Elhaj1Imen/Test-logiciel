package com.celectronix.pages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductPage extends BasePage {

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isTitleDisplayed() {
        WebElement t = waitFirstVisible(Locators.PRODUCT_TITLE, 10);
        return t != null && !t.getText().isBlank();
    }

    public boolean isPriceDisplayed() {
        WebElement p = waitFirstVisible(Locators.PRODUCT_PRICE, 5);
        if (p != null && !p.getText().isBlank()) {
            return true;
        }
        // Repli : un montant suivi d'une devise (DT, TND, EUR...) est visible sur la page
        return Pattern.compile("\\d[\\d\\s.,]*\\s?(dt|tnd|eur|€|\\$)", Pattern.CASE_INSENSITIVE)
            .matcher(pageText()).find();
    }

    /** Clique sur "Ajouter au panier". Retourne true si une confirmation est detectee. */
    public boolean addToCart() {
        WebElement button = waitFirstVisible(Locators.ADD_TO_CART, 10);
        if (button == null) {
            button = firstVisible(Locators.ADD_TO_CART_BY_TEXT);
        }
        if (button == null) {
            throw new NoSuchElementException(
                "Bouton 'Ajouter au panier' introuvable (adapter Locators.ADD_TO_CART)");
        }
        click(button);
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end) {
            if (firstVisible(Locators.ADD_CONFIRMATION) != null) {
                return true;
            }
            String text = pageText();
            if (text.contains("ajouté au panier") || text.contains("ajoute au panier")
                    || text.contains("ajouté à votre panier") || text.contains("added to your cart")
                    || text.contains("added to cart")) {
                return true;
            }
            pause(300);
        }
        return false;
    }

    private static final Pattern PRICE_VALUE =
        Pattern.compile("(\\d[\\d\\s]*(?:[.,]\\d+)?)\\s?(?:tnd|dt)", Pattern.CASE_INSENSITIVE);

    /**
     * Prix numerique du produit affiche (0 inclus), lu dans le bloc qui entoure le titre h1
     * (evite de lire le total du panier de l'en-tete). Retourne null si aucun prix n'est lisible.
     */
    public Double productPriceValue() {
        WebElement title = waitFirstVisible(Locators.PRODUCT_TITLE, 10);
        if (title == null) {
            return null;
        }
        WebElement current = title;
        for (int level = 0; level < 8; level++) {
            try {
                current = current.findElement(By.xpath(".."));
            } catch (Exception e) {
                return null;
            }
            Matcher m = PRICE_VALUE.matcher(current.getText());
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1).replaceAll("\\s", "").replace(',', '.'));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean isAddToCartAvailable() {
        return firstVisible(Locators.ADD_TO_CART) != null || firstVisible(Locators.ADD_TO_CART_BY_TEXT) != null;
    }

    private static final By QUANTITY_INPUT = By.cssSelector(
        "input[name='qty'], input#quantity_wanted, input[class*='qty'], input[class*='quantity'], input[type='number']");

    /** Saisit une quantite sur la fiche produit ; retourne false si le champ n'existe pas. */
    public boolean setQuantity(int quantity) {
        WebElement input = waitFirstVisible(QUANTITY_INPUT, 8);
        if (input == null) {
            return false;
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].value='';", input);
        input.sendKeys(String.valueOf(quantity));
        input.sendKeys(Keys.TAB);
        pause(2500);
        return true;
    }

    /** Vrai si le site affiche un avertissement de stock (ex. "Il n'y a pas assez de produits en stock."). */
    public boolean isStockWarningDisplayed() {
        String text = pageText();
        for (String w : new String[] {"pas assez de produits", "stock insuffisant", "quantite maximale", "quantité maximale",
                                      "plus que", "pas disponible"}) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
