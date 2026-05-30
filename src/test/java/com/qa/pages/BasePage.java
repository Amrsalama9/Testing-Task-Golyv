package com.qa.pages;

import com.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base page that every Page Object extends.
 * Provides common low-level interactions so page classes stay clean and DRY.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }

    // ── Navigation ──────────────────────────────────────────────────

    protected void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    // ── Element interactions ────────────────────────────────────────

    protected WebElement find(By locator) {
        return WaitUtils.waitForVisible(driver, locator);
    }

    protected void click(By locator) {
        WebElement el = WaitUtils.waitForClickable(driver, locator);
        log.debug("Clicking: {}", locator);
        el.click();
    }

    protected void type(By locator, String text) {
        WebElement el = find(locator);
        el.clear();
        log.debug("Typing '{}' into: {}", text, locator);
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return find(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return find(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Scroll helpers ──────────────────────────────────────────────

    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    protected void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    // ── JS helpers ──────────────────────────────────────────────────

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // ── Hover ───────────────────────────────────────────────────────

    protected void hoverOver(WebElement element) {
        new Actions(driver).moveToElement(element).perform();
    }
}
