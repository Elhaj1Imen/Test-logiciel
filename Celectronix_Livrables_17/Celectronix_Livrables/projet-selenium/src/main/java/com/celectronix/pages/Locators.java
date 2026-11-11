package com.celectronix.pages;

import org.openqa.selenium.By;

/**
 * Localisateurs centralises. Ils utilisent plusieurs alternatives CSS (separees par des virgules)
 * pour couvrir les themes e-commerce courants (WooCommerce, PrestaShop, Shopify...).
 * ==> SI UN TEST ECHOUE A CAUSE D'UN ELEMENT INTROUVABLE, ADAPTEZ UNIQUEMENT CE FICHIER
 *     (clic droit > Inspecter sur le site, puis ajoutez le selecteur reel).
 */
public final class Locators {

    private Locators() { }

    // ----- Commun -----
    public static final By COOKIE_ACCEPT = By.cssSelector(
        "#onetrust-accept-btn-handler, .cookie-accept, .cmplz-accept, #cookie_action_close_header, "
        + ".cc-allow, .cc-dismiss, #CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll, "
        + "button[aria-label*='ccept'], button[id*='accept'], a[id*='accept-cookie']");

    // ----- Accueil -----
    public static final By LOGO = By.cssSelector(
        "header a.custom-logo-link, header .logo a, header a.navbar-brand, .site-logo a, "
        + "header a[href='/'], header a[rel='home'], a.logo, #_desktop_logo a");
    public static final By MENU_LINKS = By.cssSelector(
        "nav a[href], header a[href], .menu a[href], #menu a[href], .main-menu a[href], .navbar a[href]");
    public static final By SEARCH_INPUT = By.cssSelector(
        "input[type='search'], input[name='s'], input[name='q'], input[name='search'], "
        + "input[name='search_query'], input.search-field, input[placeholder*='echerch'], "
        + "input[placeholder*='earch']");
    public static final By SEARCH_TOGGLE = By.cssSelector(
        ".search-toggle, .search-icon, .header-search a, a[href='#search'], "
        + "button[aria-label*='earch'], button[aria-label*='echerch'], [class*='search'] [class*='icon'], "
        + "i.fa-search");
    public static final By LOGIN_LINK = By.cssSelector(
        "a[href*='login'], a[href*='connexion'], a[href*='my-account'], a[href*='mon-compte'], "
        + "a[href*='account'], a[href*='compte']");
    public static final By REGISTER_LINK = By.cssSelector(
        "a[href*='register'], a[href*='inscription'], a[href*='creation-compte'], a[href*='registration'], "
        + "a[href*='creer-un-compte']");
    public static final By CART_LINK = By.cssSelector(
        "a[href*='cart'], a[href*='panier'], a[href*='basket']");
    public static final By CONTACT_LINK = By.cssSelector(
        "a[href*='contact']");
    public static final By FOOTER_LINKS = By.cssSelector(
        "footer a[href], .site-footer a[href], #footer a[href], .footer a[href]");

    // ----- Liste de produits -----
    public static final By PRODUCT_CARD = By.cssSelector(
        "li.product, .product-miniature, article.product, .product-item, .product-card, "
        + ".product-grid-item, .type-product, div.product, .product-layout, .item.product");
    public static final By PRODUCT_CARD_LINK = By.cssSelector(
        ".woocommerce-LoopProduct-link, .product-title a, .product-name a, h2 a, h3 a, a[href]");
    public static final By NO_RESULT_MESSAGE = By.cssSelector(
        ".woocommerce-info, .no-products, .page-not-found, .alert-warning, .product-list-empty, "
        + ".no-results, .not-found");
    public static final By SORT_SELECT = By.cssSelector(
        "select.orderby, select[name='orderby'], select#sort-by, select[name='sort'], select.sort-by, "
        + ".sort-by select, select#selectProductSort");
    public static final By FILTER_BLOCK = By.cssSelector(
        ".filters, #search_filters, .widget_price_filter, .facet, .sidebar, aside .widget, "
        + "[class*='filter']");

    // ----- Fiche produit -----
    public static final By PRODUCT_TITLE = By.cssSelector("h1");
    public static final By PRODUCT_PRICE = By.cssSelector(
        ".price, .product-price, [itemprop='price'], .current-price, span.amount, "
        + ".woocommerce-Price-amount");
    public static final By ADD_TO_CART = By.cssSelector(
        "button.single_add_to_cart_button, button[name='add-to-cart'], [data-button-action='add-to-cart'], "
        + "button.add-to-cart, button#add-to-cart, .add_to_cart_button, form.cart button[type='submit'], "
        + "button[class*='add-to-cart'], button[class*='add_to_cart']");
    private static final String UP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOW = "abcdefghijklmnopqrstuvwxyz";
    public static final By ADD_TO_CART_BY_TEXT = By.xpath(
        "//*[self::button or self::a][contains(translate(normalize-space(.),'" + UP + "','" + LOW
        + "'),'ajouter au panier') or contains(translate(normalize-space(.),'" + UP + "','" + LOW
        + "'),'add to cart')] | //input[@type='submit'][contains(translate(@value,'" + UP + "','" + LOW
        + "'),'ajouter au panier') or contains(translate(@value,'" + UP + "','" + LOW + "'),'add to cart')]");
    public static final By ADD_CONFIRMATION = By.cssSelector(
        "#blockcart-modal, .woocommerce-message, .added_to_cart, .cart-notification, "
        + ".minicart-open, .mini-cart-open, [class*='added-to-cart']");

    // ----- Panier -----
    public static final By CART_ITEM = By.cssSelector(
        "tr.cart_item, tr.woocommerce-cart-form__cart-item, .cart-item, .cart_item, .product-line-grid, "
        + ".cart-items li");
    public static final By CART_QUANTITY = By.cssSelector(
        "input.qty, input[name*='quantity'], input.js-cart-line-product-quantity, input[type='number']");
    public static final By CART_UPDATE = By.cssSelector(
        "button[name='update_cart'], .update-cart, button[data-link-action='update'], "
        + "input[name='update_cart']");
    public static final By CART_REMOVE = By.cssSelector(
        "a.remove, .remove-from-cart, a[data-link-action='delete-from-cart'], .cart-item-remove, "
        + "a.remove_from_cart_button, button.remove, [aria-label*='emove'], [aria-label*='upprimer']");

    // ----- Formulaires -----
    public static final By EMAIL_INPUT = By.cssSelector(
        "input[type='email'], input[name='email'], input#email, input[name='username'], input#username, "
        + "input[name='log'], input[name='user_email'], input[name='reg_email']");
    public static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
    public static final By SUBMIT_BUTTON = By.cssSelector(
        "button[type='submit'], input[type='submit'], button#submit-login, button[name='login'], "
        + "button[name='register']");
    public static final By ERROR_MESSAGE = By.cssSelector(
        ".alert-danger, .alert, .woocommerce-error, .help-block, .error, .invalid-feedback, .form-error, "
        + "[role='alert'], .wpcf7-not-valid-tip, .wpcf7-response-output");
    public static final By CONTACT_NAME = By.cssSelector(
        "input[name*='name'], input[name*='nom'], input#name, input[name='your-name']");
    public static final By CONTACT_MESSAGE = By.cssSelector("textarea");

    // Bouton de suppression du panier reconnu par son texte / son libelle d'accessibilite
    public static final By CART_REMOVE_BY_TEXT = By.xpath(
        "//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'supprimer')"
        + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'retirer')"
        + " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'remove')"
        + " or normalize-space(.)='×' or normalize-space(.)='x'"
        + " or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'supprimer')"
        + " or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'remove')"
        + " or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'supprimer')"
        + " or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'remove')]");
}
