package com.qa.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.qa.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton ExtentReports wrapper.
 * ExtentTest instances are kept in a ThreadLocal so parallel test classes
 * each write to their own node without collisions.
 */
public final class ExtentReportManager {

    private static final Logger log = LoggerFactory.getLogger(ExtentReportManager.class);
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
    private static final ConfigReader config = ConfigReader.getInstance();

    private ExtentReportManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportPath = config.getReportDir() + File.separator + "TripPlannerReport_" + ts + ".html";

            new File(config.getReportDir()).mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle(config.getReportTitle());
            spark.config().setReportName("Trip Planner - QA Automation");
            spark.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

            extentReports = new ExtentReports();
            extentReports.attachReporter(spark);
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Browser", config.getBrowser());
            extentReports.setSystemInfo("Java", System.getProperty("java.version"));
            extentReports.setSystemInfo("Tester", "QA Automation Framework");

            log.info("Extent report initialised at: {}", reportPath);
        }
        return extentReports;
    }

    public static ExtentTest getTest()                         { return testThread.get(); }
    public static void setTest(ExtentTest test)                { testThread.set(test); }
    public static void removeTest()                            { testThread.remove(); }

    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
