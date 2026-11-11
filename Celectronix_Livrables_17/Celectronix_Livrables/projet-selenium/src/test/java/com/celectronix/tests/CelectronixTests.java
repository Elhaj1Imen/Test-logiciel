package com.celectronix.tests;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriverException;
import org.testng.SkipException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.celectronix.pages.ContactPage;
import com.celectronix.pages.CartPage;
import com.celectronix.pages.LoginPage;
import com.celectronix.pages.ProductPage;
import com.celectronix.pages.SecurityPage;
import com.celectronix.pages.SearchResultsPage;
import com.celectronix.utils.Config;
import com.celectronix.utils.Keywords;

/** Livrable 6 : les 20 tests fonctionnels automatises (TC01 a TC20). */
public class CelectronixTests extends BaseTest {

    // ===================== ACCUEIL ET NAVIGATION =====================

    @Test(priority = 1, description = "TC01 - Chargement de la page d'accueil")
    public void tc01_homePageLoads() {
        Assert.assertTrue(driver.getCurrentUrl().contains(host()),
            "L'URL de la page d'accueil est incorrecte : " + driver.getCurrentUrl());
        Assert.assertFalse(driver.getTitle().isBlank(), "Le titre de la page est vide");
        Assert.assertFalse(home.pageText().isBlank(), "La page d'accueil n'affiche aucun contenu");
    }

    @Test(priority = 2, description = "TC02 - Code promo invalide refuse avec un message clair",
          dataProvider = "promoInvalid", dataProviderClass = DataProviders.class)
    public void tc02_invalidPromoCode(String code) {
        // Test IGNORE volontairement (statut Skipped) : aucun code promo valide n'est fourni par la boutique.
        // Pour l'executer : promo.test.enabled=true dans config.properties.
        if (!"true".equalsIgnoreCase(Config.get("promo.test.enabled"))) {
            throw new SkipException("Test ignore : code promo non teste (aucun code valide fourni, promo.test.enabled=false)");
        }
        addFirstProductToCart();
        CartPage cart = openCart();
        Assert.assertTrue(cart.itemCount() > 0, "Le panier est vide : impossible de tester le code promo");
        if (!cart.applyPromoCode(code)) {
            throw new SkipException("Aucun champ de code promo sur la page panier");
        }
        Assert.assertTrue(cart.isPromoRejected(),
            "Aucun message d'erreur affiche pour le code promo invalide : " + code);
    }

    @Test(priority = 3, description = "TC03 - HTTPS force et sans contenu mixte (accueil, connexion, panier)")
    public void tc03_httpsAndMixedContent() {
        SecurityPage security = new SecurityPage(driver);
        List<String> problems = new ArrayList<>();
        String login = home.loginHref();
        String cart = home.cartHref();
        Assert.assertTrue(security.redirectsToHttps("http://" + host() + "/"),
            "L'adresse http:// ne redirige pas vers https://");
        problems.addAll(security.httpsProblemsOn(Config.baseUrl()));
        if (login != null) {
            problems.addAll(security.httpsProblemsOn(login));
        }
        if (cart != null) {
            problems.addAll(security.httpsProblemsOn(cart));
        }
        Assert.assertTrue(problems.isEmpty(), "Problemes HTTPS / contenu mixte : " + problems);
    }

    @Test(priority = 4, description = "TC04 - Navigation vers une categorie")
    public void tc04_navigateToCategory() {
        String href = home.openFirstCategory();
        Assert.assertNotNull(href, "Aucune categorie n'a pu etre ouverte depuis le menu");
        Assert.assertNotEquals(driver.getCurrentUrl(), Config.baseUrl(), "La navigation n'a pas eu lieu");
        Assert.assertFalse(driver.getTitle().isBlank(), "La page de categorie n'a pas de titre");
    }

    // ===================== RECHERCHE =====================

    @Test(priority = 5, description = "TC05 - Recherche par libelle ou reference exacte (ESP32, Arduino Uno, Capteur DHT11, 59-03213)")
    public void tc05_searchExistingProduct() {
        List<String> problems = new ArrayList<>();
        for (String keyword : Keywords.own("TC05")) {
            home.open();
            SearchResultsPage results = home.search(keyword);
            if (home.looksLikeNotFound()) {
                problems.add(keyword + " : page 404 / erreur");
            } else if (results.productCount() == 0) {
                problems.add(keyword + " : aucun resultat (page vide)");
            } else {
                String text = home.pageText();
                boolean relevant = false;
                for (String word : keyword.toLowerCase().split("\\s+")) {
                    relevant |= word.length() >= 3 && text.contains(word);
                }
                if (!relevant) {
                    problems.add(keyword + " : resultats sans rapport avec la recherche");
                }
            }
        }
        Assert.assertTrue(problems.isEmpty(), "Recherche precise non pertinente : " + problems);
    }

    @Test(priority = 6, description = "TC06 - Recherche d'un produit inexistant, puis recherche vide",
          dataProvider = "searchInvalid", dataProviderClass = DataProviders.class)
    public void tc06_searchUnknownProduct(String keyword) {
        SearchResultsPage results = home.search(keyword);
        Assert.assertTrue(results.productCount() == 0 || results.hasNoResultMessage(),
            "Le site devrait indiquer l'absence de resultat pour : " + keyword);
        home.open();
        home.search("");
        Assert.assertFalse(driver.getTitle().isBlank(), "La page est cassee apres une recherche vide");
        String text = home.pageText();
        Assert.assertFalse(text.contains("erreur 500") || text.contains("internal server error"),
            "Une erreur serveur est affichee apres une recherche vide");
    }

    @Test(priority = 7, description = "TC07 - Page d'erreur propre (404) sans trace technique")
    public void tc07_cleanErrorPage() {
        SecurityPage security = new SecurityPage(driver);
        driver.get(Config.baseUrl() + "page-inexistante-test-selenium-404");
        home.waitForPageReady();
        Assert.assertFalse(driver.getTitle().isBlank(), "La page d'erreur n'a pas de titre");
        Assert.assertTrue(home.looksLikeNotFound() || home.pageText().contains("404"),
            "Une adresse inexistante n'affiche pas de page 404 claire : " + driver.getTitle());
        List<String> leaks = security.technicalDetailsShown();
        Assert.assertTrue(leaks.isEmpty(), "Informations techniques visibles dans la page d'erreur : " + leaks);
    }

    @Test(priority = 8, description = "TC08 - Affichage de la liste de resultats")
    public void tc08_resultsListDisplayed() {
        SearchResultsPage results = searchProducts();
        Assert.assertTrue(results.productCount() > 0, "La liste de resultats est vide");
        Assert.assertTrue(results.firstProductHasLink(), "Le premier produit n'a pas de lien vers sa fiche");
    }

    // ===================== FICHE PRODUIT =====================

    @Test(priority = 9, description = "TC09 - Ouverture d'une fiche produit (titre et prix visibles)")
    public void tc09_openProductPage() {
        String before = driver.getCurrentUrl();
        ProductPage product = openFirstProduct();
        Assert.assertNotEquals(driver.getCurrentUrl(), before, "La fiche produit ne s'est pas ouverte");
        Assert.assertTrue(product.isTitleDisplayed(), "Le titre de la fiche produit n'est pas visible");
        Assert.assertTrue(product.isPriceDisplayed(), "Le prix du produit n'est pas visible");
    }

    @Test(priority = 10, description = "TC10 - Ouverture d'une page de categorie par son adresse : elle liste des produits",
          dataProvider = "categoryPage", dataProviderClass = DataProviders.class)
    public void tc10_openCategoryPage(String path) {
        driver.get(Config.baseUrl() + path);
        home.waitForPageReady();
        Assert.assertTrue(driver.getCurrentUrl().contains(path),
            "La page de categorie n'est pas accessible : " + driver.getCurrentUrl());
        Assert.assertFalse(home.looksLikeNotFound(), "La categorie affiche une page d'erreur : " + driver.getTitle());
        Assert.assertTrue(new SearchResultsPage(driver).productCount() > 0,
            "Aucun produit n'est liste dans la categorie " + path);
    }

    // ===================== PANIER =====================

    @Test(priority = 11, description = "TC11 - Ajout au panier et coherence du prix (prix superieur a 0)")
    public void tc11_addToCart() {
        ProductPage product = openFirstProduct();
        Assert.assertTrue(product.addToCart(), "L'ajout au panier n'a pas ete confirme");

        // Regle metier : un produit propose a la vente doit avoir un prix strictement positif
        String url = Config.get("anomaly.product.url");
        if (url != null && !url.isBlank()) {
            driver.get(url);
            home.waitForPageReady();
            ProductPage reference = new ProductPage(driver);
            Double price = reference.productPriceValue();
            Assert.assertNotNull(price, "Aucun prix lisible sur la fiche : " + url);
            Assert.assertTrue(price > 0 || !reference.isAddToCartAvailable(),
                "ANOMALIE : produit affiche a " + price + " TND avec un bouton 'Ajouter au panier' actif : " + url);
        }
    }

    @Test(priority = 12, description = "TC12 - Le produit apparait dans le panier avec une devise coherente (TND)")
    public void tc12_cartContainsProduct() {
        addFirstProductToCart();
        CartPage cart = openCart();
        Assert.assertTrue(cart.itemCount() > 0 && !cart.isEmpty(),
            "Le produit ajoute n'apparait pas dans le panier");
        // Regle de coherence : une seule notation de la devise dans le panier (TND ou DT)
        String text = cart.pageText();
        boolean usesTnd = text.contains("tnd");
        boolean usesDt = java.util.regex.Pattern.compile("\\d\\s*dt\\b").matcher(text).find();
        Assert.assertFalse(usesTnd && usesDt,
            "ANOMALIE : devises melangees dans le panier (montants en TND et message de livraison en DT)");
    }

    @Test(priority = 13, description = "TC13 - Quantite : avertissement de stock sur la fiche, quantite modifiable dans le panier, valeur -1 refusee")
    public void tc13_updateQuantity() {
        // Fiche produit : une quantite superieure au stock doit etre signalee a l'utilisateur
        ProductPage product = openFirstProduct();
        if (product.setQuantity(999)) {
            Assert.assertTrue(product.isStockWarningDisplayed(),
                "Aucun avertissement de stock pour une quantite de 999 sur la fiche produit");
        }

        // Panier : la quantite est modifiable
        addFirstProductToCart();
        CartPage cart = openCart();
        cart.setQuantity(2);
        Assert.assertEquals(cart.quantityValue(), "2", "La quantite n'a pas ete mise a jour");

        // Valeur limite : une quantite negative ne doit jamais etre acceptee par le panier
        cart.setQuantity(-1);
        String value = cart.quantityValue();
        boolean cartEmpty = cart.isEmpty() || cart.itemCount() == 0;
        Assert.assertTrue(cartEmpty || isValidQuantity(value),
            "Le panier accepte une quantite invalide (-1) : valeur affichee = '" + value + "'");
    }

    private static boolean isValidQuantity(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            return Integer.parseInt(value.trim()) >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Test(priority = 14, description = "TC14 - Suppression d'un produit du panier")
    public void tc14_removeFromCart() {
        addFirstProductToCart();
        CartPage cart = openCart();
        int before = cart.itemCount();
        Assert.assertTrue(before > 0, "Le panier est vide avant la suppression");
        cart.removeFirstItem();
        Assert.assertTrue(cart.isEmpty() || cart.itemCount() < before,
            "Le produit n'a pas ete retire du panier");
    }

    // ===================== COMPTE UTILISATEUR =====================

    @Test(priority = 15, description = "TC15 - Affichage de la page de connexion")
    public void tc15_loginPageDisplayed() {
        goTo(home.loginHref(), "connexion / mon compte");
        Assert.assertTrue(new LoginPage(driver).isFormDisplayed(),
            "Le formulaire de connexion n'est pas affiche");
    }

    @Test(priority = 16, description = "TC16 - Connexion avec identifiants invalides",
          dataProvider = "loginInvalid", dataProviderClass = DataProviders.class)
    public void tc16_invalidLogin(String email, String password) {
        goTo(home.loginHref(), "connexion / mon compte");
        LoginPage login = new LoginPage(driver);
        login.login(email, password);
        Assert.assertTrue(login.isLoginRejected(),
            "Le site aurait du refuser la connexion pour : " + email);
    }

    @Test(priority = 17, description = "TC17 - Inscription avec donnees invalides",
          dataProvider = "registerInvalid", dataProviderClass = DataProviders.class)
    public void tc17_invalidRegistration(String email, String password) {
        String href = home.registerHref();
        if (href == null) {
            href = home.loginHref(); // sur certains sites l'inscription est sur la page de connexion
        }
        goTo(href, "inscription / creation de compte");
        LoginPage page = new LoginPage(driver);
        Assert.assertTrue(page.isRegisterFormDisplayed(), "Le formulaire d'inscription n'est pas affiche");
        Assert.assertTrue(page.submitInvalidRegistration(email, password),
            "Le site aurait du refuser l'inscription pour : " + email);
    }

    // ===================== CONTACT, PIED DE PAGE, TRI =====================

    @Test(priority = 18, description = "TC18 - Formulaire de contact avec donnees invalides",
          dataProvider = "contactInvalid", dataProviderClass = DataProviders.class)
    public void tc18_contactFormValidation(String name, String email, String message) {
        String href = home.contactHref();
        if (href != null) {
            goTo(href, "contact");
        }
        ContactPage contact = new ContactPage(driver);
        Assert.assertTrue(contact.openFormWithFallback(Config.baseUrl()),
            "Le formulaire de contact n'est pas affiche (voir errors/*.html pour la page obtenue)");
        Assert.assertTrue(contact.submitAndCheckRejected(name, email, message),
            "Le formulaire aurait du etre refuse pour : [" + name + " / " + email + " / " + message + "]");
    }

    @Test(priority = 19, description = "TC19 - Pied de page : les liens utiles fonctionnent")
    public void tc19_footerLinks() {
        Assert.assertTrue(home.isFooterPresent(), "Aucun pied de page avec liens n'a ete trouve");
        List<String> links = home.footerPageLinks(12);
        Assert.assertFalse(links.isEmpty(), "Le pied de page ne contient aucun lien interne");

        List<String> broken = new ArrayList<>();
        for (String link : links) {
            try {
                driver.get(link);
                home.waitForPageReady();
                if (home.looksLikeNotFound()) {
                    broken.add(link + " (titre : " + driver.getTitle() + ")");
                }
            } catch (WebDriverException e) {
                broken.add(link + " (erreur de chargement)");
            }
        }
        Assert.assertTrue(broken.isEmpty(),
            "Liens du pied de page menant a une page introuvable ou en erreur : " + broken);
    }

    @Test(priority = 20, description = "TC20 - Tri des produits par prix croissant")
    public void tc20_sortOrFilterProducts() {
        SearchResultsPage list = searchProducts();
        Assert.assertTrue(list.productCount() > 0, "La liste de produits est vide");
        if (list.isSortPresent()) {
            if (list.sortByPriceAscending()) {
                List<Double> prices = list.visiblePrices();
                Assert.assertTrue(list.productCount() > 0, "Aucun produit apres application du tri");
                for (int i = 1; i < prices.size(); i++) {
                    Assert.assertTrue(prices.get(i) >= prices.get(i - 1),
                        "Le tri par prix croissant est incorrect : " + prices);
                }
            } else {
                Assert.assertTrue(list.changeSortOrder(), "Le tri n'a pas pu etre applique");
                Assert.assertTrue(list.productCount() > 0, "Aucun produit apres application du tri");
            }
        } else {
            Assert.assertTrue(list.isFilterPresent(),
                "Ni tri ni filtre n'est disponible sur la liste de produits");
        }
    }
}
