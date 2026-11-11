package com.celectronix.tests;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.celectronix.pages.CartPage;
import com.celectronix.pages.HomePage;
import com.celectronix.pages.ProductPage;
import com.celectronix.pages.SearchResultsPage;
import com.celectronix.utils.Config;
import com.celectronix.utils.DriverFactory;
import com.celectronix.utils.DriverManager;
import com.celectronix.utils.Keywords;

/** Chaque test demarre sur un navigateur neuf positionne sur la page d'accueil (tests independants). */
public abstract class BaseTest {

    protected WebDriver driver;
    protected HomePage home;
    /** Identifiant du test en cours (ex. TC09), deduit du nom de la methode : choisit son mot-cle de recherche. */
    protected String testId = "";
    protected String usedKeyword = "";

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        testId = method.getName().length() >= 4 ? method.getName().substring(0, 4).toUpperCase() : "";
        driver = DriverFactory.create();
        DriverManager.set(driver);
        home = new HomePage(driver);
        home.open();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quit();
    }

    // ---------- Aides communes ----------

    protected String host() {
        return URI.create(Config.baseUrl()).getHost().replace("www.", "");
    }

    /** Navigue vers un lien lu sur le site ; echoue clairement si le lien n'existe pas. */
    protected void goTo(String href, String description) {
        Assert.assertNotNull(href, "Lien introuvable sur le site : " + description);
        driver.get(href);
        home.waitForPageReady();
        home.dismissCookieBanner();
    }

    /** Recherche avec le mot-cle propre au test ; essaie les autres mots du CSV si le catalogue ne donne rien. */
    protected SearchResultsPage searchProducts() {
        List<String> tried = new ArrayList<>();
        for (String keyword : Keywords.candidates(testId)) {
            SearchResultsPage results = home.search(keyword);
            if (results.productCount() > 0) {
                usedKeyword = keyword;
                System.out.println("[" + testId + "] mot-cle utilise : " + keyword);
                return results;
            }
            tried.add(keyword);
            home.open();
        }
        Assert.fail("Aucun produit trouve pour les mots-cles essayes : " + tried);
        return null;
    }

    protected ProductPage openFirstProduct() {
        return searchProducts().openFirstProduct();
    }

    protected void addFirstProductToCart() {
        ProductPage product = openFirstProduct();
        Assert.assertTrue(product.addToCart(), "Aucune confirmation d'ajout au panier detectee");
    }

    protected CartPage openCart() {
        CartPage cart = new CartPage(driver);
        return cart.open();
    }
}
