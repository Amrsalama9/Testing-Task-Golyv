package com.qa.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.qa.utils.DriverFactory;
import com.qa.utils.ExtentReportManager;
import com.qa.utils.ScreenshotUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        ExtentTest test = ExtentReportManager.getInstance().createTest(name, desc != null ? desc : "");
        ExtentReportManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentReportManager.getTest().log(Status.PASS, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        test.log(Status.FAIL, result.getThrowable());
        try {
            String path = ScreenshotUtils.capture(DriverFactory.getDriver(), result.getMethod().getMethodName());
            if (!path.isEmpty()) test.addScreenCaptureFromPath(path, "Screenshot");
        } catch (Exception ignored) {}
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentReportManager.getTest().log(Status.SKIP, "SKIP — " + result.getThrowable().getMessage());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
        ExtentReportManager.removeTest();
    }
}
