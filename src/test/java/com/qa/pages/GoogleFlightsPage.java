package com.qa.pages;

import com.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Google Flights page — handles the search form and result cards.
 * Locators are a bit fragile since Google doesn't expose stable test IDs here,
 * so keeping fallbacks where possible.
 */
public class GoogleFlightsPage extends BasePage {

    private static final By ORIGIN_INPUT        = By.cssSelector("[placeholder='Where from?'], [aria-label='Where from?']");
    private static final By DESTINATION_INPUT   = By.cssSelector("[placeholder='Where to?'], [aria-label='Where to?']");
    private static final By AUTOCOMPLETE_OPTION = By.cssSelector("[data-value], li[role='option']");
    private static final By SEARCH_BUTTON       = By.cssSelector("button[aria-label*='Search'], button.gws-flights__search-button");

    private static final By FLIGHT_RESULT_CARDS = By.cssSelector("li[class*='pIav2d'], [jsname='IWWDBc'] li");
    private static final By RESULT_AIRLINE_NAME  = By.cssSelector("[class*='Ir0Voe'] .sSHqwe, div[class*='h1fkLb']");
    private static final By RESULT_PRICE         = By.cssSelector("[class*='FpEdX'] span, [data-gs*='price']");
    private static final By NO_RESULTS_MSG       = By.cssSelector("[class*='no-flights'], [data-gs='no_results']");
    private static final By ROUTE_LABEL          = By.cssSelector("[class*='JMnxgf'], h1[class*='flights']");

    private static final By CONSENT_ACCEPT = By.cssSelector("button[id='L2AGLb'], div[id='introAgreeButton']");

    public GoogleFlightsPage(WebDriver driver) {
        super(driver);
    }

    public GoogleFlightsPage open(String url) {
        navigateTo(url);
        dismissConsentIfNeeded();
        return this;
    }

    public GoogleFlightsPage searchFlights(String origin, String destination) {
        log.info("Searching flights {} -> {}", origin, destination);
        fillOrigin(origin);
        fillDestination(destination);
        submitSearch();
        return this;
    }

    public List<WebElement> getFlightResultCards() {
        return WaitUtils.waitForAllVisible(driver, FLIGHT_RESULT_CARDS);
    }

    public boolean hasResults() {
        try {
            return !getFlightResultCards().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoResultsMessageDisplayed() {
        return isDisplayed(NO_RESULTS_MSG);
    }

    public String getAirlineForResult(int index) {
        return getFlightResultCards().get(index)
                .findElements(RESULT_AIRLINE_NAME)
                .stream().findFirst()
                .map(WebElement::getText)
                .orElse("");
    }

    public String getPriceForResult(int index) {
        return getFlightResultCards().get(index)
                .findElements(RESULT_PRICE)
                .stream().findFirst()
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

    private void fillOrigin(String city) {
        WebElement el = WaitUtils.waitForClickable(driver, ORIGIN_INPUT);
        el.click();
        el.clear();
        el.sendKeys(city);
        pickFirstSuggestion();
    }

    private void fillDestination(String city) {
        WebElement el = WaitUtils.waitForClickable(driver, DESTINATION_INPUT);
        el.click();
        el.clear();
        el.sendKeys(city);
        pickFirstSuggestion();
    }

    private void pickFirstSuggestion() {
        try {
            WaitUtils.waitForClickable(driver, AUTOCOMPLETE_OPTION).click();
        } catch (Exception e) {
            driver.findElement(DESTINATION_INPUT).sendKeys(Keys.ENTER);
        }
    }

    private void submitSearch() {
        WaitUtils.waitForClickable(driver, SEARCH_BUTTON).click();
        WaitUtils.waitForUrlContains(driver, "flights");
    }

    private void dismissConsentIfNeeded() {
        try {
            WaitUtils.waitForClickable(driver, CONSENT_ACCEPT).click();
        } catch (Exception ignored) {}
    }
}
