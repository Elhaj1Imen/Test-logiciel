package com.celectronix.utils;

import org.openqa.selenium.WebDriver;

/** Conserve le WebDriver courant (accessible par le listener pour les captures). */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() { }

    public static WebDriver get() {
        return DRIVER.get();
    }

    public static void set(WebDriver driver) {
        DRIVER.set(driver);
    }

    public static void quit() {
        WebDriver d = DRIVER.get();
        if (d != null) {
            try {
                d.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }
}
