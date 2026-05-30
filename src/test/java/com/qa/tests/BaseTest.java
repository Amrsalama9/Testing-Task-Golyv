package com.qa.tests;

import com.qa.config.ConfigReader;
import com.qa.utils.DriverFactory;
import com.qa.utils.ExtentReportManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ConfigReader config = ConfigReader.getInstance();
    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        log.info("Starting: {}", method.getName());
        driver = DriverFactory.getDriver();
        ExtentReportManager.getTest().info("Browser: " + config.getBrowser());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method method) {
        log.info("Done: {}", method.getName());
        DriverFactory.quitDriver();
    }
}
