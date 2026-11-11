package com.celectronix.tests;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.celectronix.utils.Config;
import com.celectronix.utils.DriverManager;
import com.celectronix.utils.ScreenshotUtil;

/**
 * Livrable 8 : rapport HTML (reports/ExtentReport.html) avec Passed / Failed / Skipped.
 * Livrable 9 : capture PNG (screenshots/) et trace TXT (errors/) pour chaque test en echec.
 */
public class TestListener implements ITestListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    @Override
    public synchronized void onStart(ITestContext context) {
        if (extent == null) {
            new File("reports").mkdirs();
            ExtentSparkReporter spark = new ExtentSparkReporter(System.getProperty("report.file", "reports/ExtentReport.html"));
            spark.config().setDocumentTitle("Rapport d'execution - celectronix.com");
            spark.config().setReportName("Tests fonctionnels TestNG + Selenium");
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Site teste", Config.baseUrl());
            extent.setSystemInfo("Navigateur", Config.browser());
            extent.setSystemInfo("Systeme", System.getProperty("os.name"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        String label = (description != null && !description.isBlank()) ? description : name;
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            label = label + " " + Arrays.toString(params);
        }
        CURRENT.set(extent.createTest(label));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT.get().pass("Test reussi");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String base = result.getMethod().getMethodName() + "_" + System.currentTimeMillis();
        WebDriver driver = DriverManager.get();
        Path png = driver != null ? ScreenshotUtil.saveScreenshot(driver, base) : null;
        ScreenshotUtil.saveTrace(result.getThrowable(), base);
        if (driver != null) {
            ScreenshotUtil.savePageSource(driver, base);
        }

        ExtentTest test = CURRENT.get();
        test.fail(result.getThrowable());
        if (png != null) {
            test.addScreenCaptureFromPath("../screenshots/" + png.getFileName());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = CURRENT.get();
        if (test != null) {
            test.skip(result.getThrowable() != null ? result.getThrowable().toString() : "Test ignore");
        }
    }

    @Override
    public synchronized void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
