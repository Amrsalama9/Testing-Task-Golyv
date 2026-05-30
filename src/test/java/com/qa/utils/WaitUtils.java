package com.qa.utils;

import com.qa.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Centralised explicit-wait helpers.
 * All methods are static and accept the driver instance so they stay
 * compatible with the thread-local DriverFactory pattern.
 */
public final class WaitUtils {

    private static final Logger log = LoggerFactory.getLogger(WaitUtils.class);
    private static final int TIMEOUT = ConfigReader.getInstance().getExplicitWait();

    private WaitUtils() {}

    private static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    public static WebElement waitForVisible(WebDriver driver, By locator) {
        log.debug("Waiting for visible: {}", locator);
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(WebDriver driver, By locator) {
        log.debug("Waiting for clickable: {}", locator);
        return wait(driver).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickable(WebDriver driver, WebElement element) {
        log.debug("Waiting for element to be clickable");
        return wait(driver).until(ExpectedConditions.elementToBeClickable(element));
    }

    public static List<WebElement> waitForAllVisible(WebDriver driver, By locator) {
        log.debug("Waiting for all visible: {}", locator);
        return wait(driver).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static boolean waitForTextPresent(WebDriver driver, By locator, String text) {
        log.debug("Waiting for text '{}' in: {}", text, locator);
        return wait(driver).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static boolean waitForUrlContains(WebDriver driver, String fragment) {
        log.debug("Waiting for URL to contain: {}", fragment);
        return wait(driver).until(ExpectedConditions.urlContains(fragment));
    }

    public static void hardWait(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
