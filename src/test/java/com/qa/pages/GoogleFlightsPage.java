package com.qa.pages;

import com.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for Google Flights (google.com/travel/flights).
 * Covers the origin/destination inputs, the search trigger, and the
 * basic assertions on results (airline names, price tiles, route label).
 */
public class GoogleFlightsPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────

    // Origin / destination inputs
    private static final By ORIGIN_INPUT       = By.cssSelector("[placeholder='Where from?'], [aria-label='Where from?']");
    private static final By DESTINATION_INPUT  = By.cssSelector("[placeholder='Where to?'], [aria-label='Where to?']");

    // Autocomplete suggestion list
    private static final By AUTOCOMPLETE_OPTION = By.cssSelector("[data-value], li[role='option']");

    // Search button
    private static final By SEARCH_BUTTON      = By.cssSelector("button[aria-label*='Search'], button.gws-flights__search-button");

    // Results
    private static final By FLIGHT_RESULT_CARDS = By.cssSelector("li[class*='pIav2d'], [jsname='IWWDBc'] li");
    private static final By RESULT_AIRLINE_NAME  = By.cssSelector("[class*='Ir0Voe'] .sSHqwe, div[class*='h1fkLb']");
    private static final By RESULT_PRICE         = By.cssSelector("[class*='FpEdX'] span, [data-gs*='price']");
    private static final By RESULT_DURATION      = By.cssSelector("[class*='gvkrdb'], [class*='AdWynf']");
    private static final By NO_RESULTS_MSG       = By.cssSelector("[class*='no-flights'], [data-gs='no_results']");

    // Route breadcrumb visible on results page header
    private static final By ROUTE_LABEL          = By.cssSelector("[class*='JMnxgf'], h1[class*='flights']");

    // Consent / cookie banner (same as search page)
    private static final By CONSENT_ACCEPT       = By.cssSelector("button[id='L2AGLb'], div[id='introAgreeButton']");

    public GoogleFlightsPage(WebDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────

    public GoogleFlightsPage open(String url) {
        navigateTo(url);
        acceptConsentIfPresent();
        return this;
    }

    /**
     * Fills in origin, destination and clicks Search.
     */
    public GoogleFlightsPage searchFlights(String origin, String destination) {
        log.info("Searching flights: {} → {}", origin, destination);
        enterOrigin(origin);
        enterDestination(destination);
        clickSearch();
        return this;
    }

    public List<WebElement> getFlightResultCards() {
        return WaitUtils.waitForAllVisible(driver, FLIGHT_RESULT_CARDS);
    }

    public boolean hasResults() {
        try {
            List<WebElement> cards = getFlightResultCards();
            return !cards.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoResultsMessageDisplayed() {
        return isDisplayed(NO_RESULTS_MSG);
    }

    public String getAirlineForResult(int index) {
        List<WebElement> cards = getFlightResultCards();
        return cards.get(index)
                    .findElements(RESULT_AIRLINE_NAME)
                    .stream()
                    .findFirst()
                    .map(WebElement::getText)
                    .orElse("Unknown");
    }

    public String getPriceForResult(int index) {
        List<WebElement> cards = getFlightResultCards();
        return cards.get(index)
                    .findElements(RESULT_PRICE)
                    .stream()
                    .findFirst()
                    .map(WebElement::getText)
                    .orElse("N/A");
    }

    public String getRouteLabelText() {
        try {
            return getText(ROUTE_LABEL);
        } catch (Exception e) {
            return driver.getTitle();
        }
    }

    public int getResultCount() {
        try {
            return getFlightResultCards().size();
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────

    private void enterOrigin(String city) {
        WebElement originEl = WaitUtils.waitForClickable(driver, ORIGIN_INPUT);
        originEl.click();
        originEl.clear();
        originEl.sendKeys(city);
        selectFirstSuggestion();
    }

    private void enterDestination(String city) {
        WebElement destEl = WaitUtils.waitForClickable(driver, DESTINATION_INPUT);
        destEl.click();
        destEl.clear();
        destEl.sendKeys(city);
        selectFirstSuggestion();
    }

    private void selectFirstSuggestion() {
        try {
            WebElement suggestion = WaitUtils.waitForClickable(driver, AUTOCOMPLETE_OPTION);
            suggestion.click();
        } catch (Exception e) {
            // If no dropdown, press Enter to confirm
            driver.findElement(DESTINATION_INPUT).sendKeys(Keys.ENTER);
        }
    }

    private void clickSearch() {
        WaitUtils.waitForClickable(driver, SEARCH_BUTTON).click();
        WaitUtils.waitForUrlContains(driver, "flights");
    }

    private void acceptConsentIfPresent() {
        try {
            WaitUtils.waitForClickable(driver, CONSENT_ACCEPT).click();
        } catch (Exception ignored) {}
    }
}
