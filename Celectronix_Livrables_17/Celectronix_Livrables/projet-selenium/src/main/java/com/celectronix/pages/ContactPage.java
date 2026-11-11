package com.celectronix.pages;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ContactPage extends BasePage {

    public ContactPage(WebDriver driver) {
        super(driver);
    }

    public boolean isFormDisplayed() {
        return waitFirstVisible(Locators.CONTACT_MESSAGE, 10) != null;
    }

    /** Si la page courante n'a pas de formulaire, essaie des adresses usuelles de la page contact. */
    public boolean openFormWithFallback(String baseUrl) {
        if (waitFirstVisible(Locators.CONTACT_MESSAGE, 5) != null) {
            return true;
        }
        String[] paths = {"contact", "contactez-nous", "nous-contacter", "contact-us", "contact/", "index.php?controller=contact"};
        for (String path : paths) {
            driver.get(baseUrl + path);
            waitForPageReady();
            if (waitFirstVisible(Locators.CONTACT_MESSAGE, 4) != null) {
                return true;
            }
        }
        return false;
    }

    /** Soumet le formulaire ; retourne true si le site le rejette (validation HTML5 ou message). */
    public boolean submitAndCheckRejected(String name, String email, String message) {
        WebElement msg = waitFirstVisible(Locators.CONTACT_MESSAGE, 10);
        if (msg == null) {
            throw new NoSuchElementException("Formulaire de contact introuvable (adapter Locators)");
        }
        WebElement nameField = firstVisible(Locators.CONTACT_NAME);
        WebElement emailField = firstVisible(Locators.EMAIL_INPUT);
        if (nameField != null) {
            nameField.clear();
            nameField.sendKeys(name);
        }
        if (emailField != null) {
            emailField.clear();
            emailField.sendKeys(email);
        }
        msg.clear();
        msg.sendKeys(message);

        boolean htmlInvalid = isInvalid(msg)
            || (nameField != null && isInvalid(nameField))
            || (emailField != null && isInvalid(emailField));

        WebElement submit = submitFor(msg);
        if (submit == null) {
            throw new NoSuchElementException("Bouton d'envoi introuvable");
        }
        click(submit);
        pause(1500);
        return htmlInvalid || isErrorDisplayed();
    }
}
