package com.celectronix.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;

import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.celectronix.utils.Config;

public class CartPage extends BasePage {

    private static final String[] EMPTY_WORDS = {
        "panier est vide", "panier vide", "cart is currently empty", "cart is empty", "aucun article"
    };

    public CartPage(WebDriver driver) {
        super(driver);
    }

    /** Ouvre le panier via le lien du site, sinon via des URL usuelles. */
    public CartPage open() {
        String href = hrefOf(Locators.CART_LINK);
        if (href != null) {
            driver.get(href);
            waitForPageReady();
            pause(1000);
        }
        if (itemCount() == 0) {
            // Repli : adresses usuelles de la page panier
            String[] paths = {"panier", "cart", "panier?action=show", "index.php?controller=cart&action=show",
                              "index.php?route=checkout/cart"};
            for (String path : paths) {
                driver.get(Config.baseUrl() + path);
                waitForPageReady();
                pause(1000);
                if (itemCount() > 0) {
                    break;
                }
            }
        }
        return this;
    }

    public int itemCount() {
        // lignes de panier reconnues, ou a defaut champs quantite visibles (1 par article)
        return Math.max(visible(Locators.CART_ITEM).size(), visible(Locators.CART_QUANTITY).size());
    }

    public boolean isEmpty() {
        String text = pageText();
        for (String w : EMPTY_WORDS) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }

    public void setQuantity(int quantity) {
        WebElement input = waitFirstVisible(Locators.CART_QUANTITY, 10);
        if (input == null) {
            throw new NoSuchElementException("Champ quantite introuvable (adapter Locators.CART_QUANTITY)");
        }
        // clear() ne vide pas toujours ce champ (le "1" restait et donnait "12") : on vide par JavaScript
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value='';", input);
        input.sendKeys(String.valueOf(quantity));
        WebElement update = firstVisible(Locators.CART_UPDATE);
        if (update != null) {
            click(update);
        } else {
            input.sendKeys(Keys.TAB);
        }
        pause(2000);
    }

    public String quantityValue() {
        List<WebElement> inputs = visible(Locators.CART_QUANTITY);
        return inputs.isEmpty() ? null : inputs.get(0).getAttribute("value");
    }

    public void removeFirstItem() {
        WebElement remove = waitFirstVisible(Locators.CART_REMOVE, 8);
        if (remove == null) {
            remove = firstVisible(Locators.CART_REMOVE_BY_TEXT);
        }
        if (remove == null) {
            throw new NoSuchElementException("Bouton de suppression introuvable (adapter Locators.CART_REMOVE)");
        }
        click(remove);
        pause(2500);
    }

    // ----- Montants et code promo -----

    /** Montants affiches dans la page (ex. 34,500) : sert a verifier que les totaux sont recalcules. */
    public List<String> amounts() {
        Matcher m = Pattern.compile("\\d[\\d .]*,\\d{3}").matcher(pageText());
        List<String> found = new ArrayList<>();
        while (m.find()) {
            found.add(m.group().trim());
        }
        return found;
    }

    private static final By PROMO_TOGGLE = By.xpath(
        "//*[contains(@class,'promo-code-button')]//a | //*[contains(@class,'promo')]//*[self::a or self::button or self::summary or self::h3 or self::div or self::span]"
        + "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'code promo')]"
        + " | //*[self::a or self::button or self::summary or self::h2 or self::h3 or self::div or self::span or self::p]"
        + "[normalize-space(text())='Code promo']");
    private static final By PROMO_INPUT = By.cssSelector(
        "input[name='discount_name'], input[name*='promo' i], input[name*='coupon' i], input[name*='voucher' i], input[placeholder*='promo' i]");
    private static final By PROMO_ERROR = By.cssSelector(
        ".promo-code .alert-danger, .js-error-text, .alert-danger, .alert-error, .error, .invalid-feedback");

    /** Saisit un code promo ; retourne false si le panier n'a aucun champ de code promo. */
    public boolean applyPromoCode(String code) {
        WebElement input = firstVisible(PROMO_INPUT);
        if (input == null) {
            WebElement toggle = firstVisible(PROMO_TOGGLE);
            if (toggle != null) {
                click(toggle);
                input = waitFirstVisible(PROMO_INPUT, 5);
            }
        }
        if (input == null) {
            return false;
        }
        input.clear();
        input.sendKeys(code);
        input.sendKeys(Keys.ENTER);
        pause(2500);
        return true;
    }

    /** Vrai si un message d'erreur de code promo est affiche. */
    public boolean isPromoRejected() {
        if (firstVisible(PROMO_ERROR) != null) {
            return true;
        }
        String text = pageText();
        for (String w : new String[] {"n'est pas valide", "invalide", "inexistant", "expir", "n'existe pas", "incorrect"}) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }

    /** Attend (jusqu'a N secondes) que les montants changent apres une modification de quantite (panier en AJAX). */
    public boolean waitForAmountsChange(List<String> before, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end) {
            if (!amounts().equals(before)) {
                return true;
            }
            pause(500);
        }
        return false;
    }

    /** Vrai si le site previent l'utilisateur d'un probleme de stock ou de quantite maximale. */
    public boolean stockWarningShown() {
        String text = pageText();
        for (String w : new String[] {"pas assez de produits", "pas assez", "stock insuffisant", "quantite maximale", "quantité maximale", "pas disponible",
                                      "non disponible", "n'est plus disponible", "stock disponible", "maximum"}) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }
}
