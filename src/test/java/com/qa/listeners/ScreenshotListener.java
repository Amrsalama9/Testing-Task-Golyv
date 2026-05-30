package com.qa.listeners;

import com.qa.utils.DriverFactory;
import com.qa.utils.ScreenshotUtils;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Captures a screenshot to disk on every test failure.
 * Works alongside ExtentReportListener (which also attaches it to the HTML report).
 */
public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            ScreenshotUtils.capture(DriverFactory.getDriver(), result.getMethod().getMethodName());
        } catch (Exception ignored) {
            // driver may already be quit
        }
    }
}
