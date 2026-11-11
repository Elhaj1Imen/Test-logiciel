package com.celectronix.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Livrable 9 : captures d'ecran (PNG) et traces d'exception (TXT) des tests en echec. */
public final class ScreenshotUtil {

    public static final Path SCREENSHOT_DIR = Paths.get("screenshots");
    public static final Path ERROR_DIR = Paths.get("errors");

    private ScreenshotUtil() { }

    public static Path saveScreenshot(WebDriver driver, String baseName) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Path source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Path target = SCREENSHOT_DIR.resolve(baseName + ".png");
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (Exception e) {
            System.err.println("Capture impossible : " + e.getMessage());
            return null;
        }
    }

    public static Path savePageSource(WebDriver driver, String baseName) {
        try {
            Files.createDirectories(ERROR_DIR);
            Path target = ERROR_DIR.resolve(baseName + ".html");
            Files.write(target, driver.getPageSource().getBytes(StandardCharsets.UTF_8));
            return target;
        } catch (Exception e) {
            System.err.println("Sauvegarde du HTML impossible : " + e.getMessage());
            return null;
        }
    }

    public static Path saveTrace(Throwable error, String baseName) {
        try {
            Files.createDirectories(ERROR_DIR);
            StringWriter sw = new StringWriter();
            if (error != null) {
                error.printStackTrace(new PrintWriter(sw));
            }
            Path target = ERROR_DIR.resolve(baseName + ".txt");
            Files.write(target, sw.toString().getBytes(StandardCharsets.UTF_8));
            return target;
        } catch (Exception e) {
            System.err.println("Ecriture de la trace impossible : " + e.getMessage());
            return null;
        }
    }
}
