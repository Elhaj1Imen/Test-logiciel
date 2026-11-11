package com.celectronix.pages;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public boolean isFormDisplayed() {
        return waitFirstVisible(Locators.EMAIL_INPUT, 10) != null
            && firstVisible(Locators.PASSWORD_INPUT) != null;
    }

    public void login(String email, String password) {
        WebElement mail = waitFirstVisible(Locators.EMAIL_INPUT, 10);
        WebElement pass = firstVisible(Locators.PASSWORD_INPUT);
        if (mail == null || pass == null) {
            throw new NoSuchElementException("Formulaire de connexion introuvable (adapter Locators)");
        }
        mail.clear();
        mail.sendKeys(email);
        pass.clear();
        pass.sendKeys(password);
        WebElement submit = submitFor(pass);
        if (submit == null) {
            throw new NoSuchElementException("Bouton de connexion introuvable");
        }
        click(submit);
        pause(1500);
    }

    /**
     * Vrai si le site refuse la connexion : message d'erreur, validation HTML5,
     * ou formulaire toujours present sans signe de connexion reussie (deconnexion / logout).
     */
    public boolean isLoginRejected() {
        if (isErrorDisplayed()) {
            return true;
        }
        String text = pageText();
        String[] errorWords = {"incorrect", "invalide", "invalid", "erreur", "error", "échec", "echec",
                               "introuvable", "inconnu", "wrong", "failed", "n'existe pas"};
        for (String w : errorWords) {
            if (text.contains(w)) {
                return true;
            }
        }
        WebElement mail = firstVisible(Locators.EMAIL_INPUT);
        WebElement pass = firstVisible(Locators.PASSWORD_INPUT);
        boolean stillOnForm = mail != null && pass != null;
        boolean loggedIn = text.contains("déconnexion") || text.contains("deconnexion")
            || text.contains("se déconnecter") || text.contains("logout") || text.contains("log out");
        return stillOnForm && !loggedIn;
    }

    // ----- Inscription (meme page ou page dediee) -----

    public boolean isRegisterFormDisplayed() {
        return waitFirstVisible(Locators.EMAIL_INPUT, 10) != null
            && firstVisible(Locators.PASSWORD_INPUT) != null;
    }

    /** Remplit le formulaire d'inscription avec des donnees invalides et valide. */
    public boolean submitInvalidRegistration(String email, String password) {
        WebElement mail = waitFirstVisible(Locators.EMAIL_INPUT, 10);
        WebElement pass = firstVisible(Locators.PASSWORD_INPUT);
        if (mail == null || pass == null) {
            throw new NoSuchElementException("Formulaire d'inscription introuvable");
        }
        mail.clear();
        mail.sendKeys(email);
        pass.clear();
        pass.sendKeys(password);
        WebElement submit = submitFor(pass);
        if (submit == null) {
            throw new NoSuchElementException("Bouton d'inscription introuvable");
        }
        boolean htmlInvalid = isInvalid(mail);
        click(submit);
        pause(1500);
        return htmlInvalid || isErrorDisplayed();
    }
}
