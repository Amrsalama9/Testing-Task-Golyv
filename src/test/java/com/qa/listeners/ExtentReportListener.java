package com.qa.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.qa.utils.ExtentReportManager;
import com.qa.utils.ScreenshotUtils;
import com.qa.utils.DriverFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Hooks into TestNG lifecycle to create Extent report nodes and log
 * pass / fail / skip statuses automatically for every test method.
 */
public class ExtentReportListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        ExtentTest test = ExtentReportManager.getInstance()
                .createTest(name, desc != null ? desc : "");
        ExtentReportManager.setTest(test);
        test.info("Starting test: " + name);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentReportManager.getTest().log(Status.PASS, "Test PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        test.log(Status.FAIL, result.getThrowable());

        // Attach screenshot if driver is available
        try {
            String path = ScreenshotUtils.capture(
                    DriverFactory.getDriver(), result.getMethod().getMethodName());
            if (!path.isEmpty()) {
                test.addScreenCaptureFromPath(path, "Failure Screenshot");
            }
        } catch (Exception ignored) {
            // driver might already be closed
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentReportManager.getTest().log(Status.SKIP,
                "Test SKIPPED – " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
        ExtentReportManager.removeTest();
    }
}
