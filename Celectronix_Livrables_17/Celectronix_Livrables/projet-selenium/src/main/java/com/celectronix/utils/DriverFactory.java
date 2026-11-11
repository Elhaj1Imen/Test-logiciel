package com.celectronix.utils;

import java.time.Duration;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/** Cree le navigateur. Selenium Manager (Selenium 4.6+) telecharge le driver automatiquement. */
public final class DriverFactory {

    private DriverFactory() { }

    public static WebDriver create() {
        boolean headless = Config.headless();
        WebDriver driver;

        switch (Config.browser()) {
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                driver = new FirefoxDriver(options);
                break;
            }
            case "edge": {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--disable-notifications", "--remote-allowed-origins=*");
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                driver = new EdgeDriver(options);
                break;
            }
            case "chrome":
            default: {
                ChromeOptions options = new ChromeOptions();
                options.setPageLoadStrategy(PageLoadStrategy.EAGER);
                options.addArguments("--disable-notifications", "--remote-allowed-origins=*",
                        "--disable-blink-features=AutomationControlled");
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                driver = new ChromeDriver(options);
                break;
            }
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        try {
            driver.manage().window().maximize();
        } catch (Exception ignored) {
            // inutile en mode headless
        }
        return driver;
    }
}
