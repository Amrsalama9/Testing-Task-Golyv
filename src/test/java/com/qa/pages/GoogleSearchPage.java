package com.qa.pages;

import com.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Google Search home page and results page (SERP).
 * Covers the search input, weather widget, and local results panel.
 */
public class GoogleSearchPage extends BasePage {

    // ── Locators ─────────────────────────────────────────────────────

    // Search input (home + SERP)
    private static final By SEARCH_INPUT     = By.name("q");
    private static final By SEARCH_INPUT_ALT = By.cssSelector("textarea[name='q']");

    // Cookie / consent dialog that Google shows in some regions
    private static final By CONSENT_ACCEPT   = By.cssSelector("button[id='L2AGLb'], " +
                                                               "div[id='introAgreeButton']");

    // Weather widget
    private static final By WEATHER_WIDGET        = By.cssSelector("#wob_wc, [data-attrid='weather:observation']");
    private static final By WEATHER_LOCATION_LABEL = By.cssSelector("#wob_loc, [data-attrid='weather:observation'] [class*='location']");
    private static final By WEATHER_TEMP          = By.cssSelector("#wob_tm, [class*='wob_t']");
    private static final By WEATHER_CONDITION     = By.cssSelector("#wob_dc");

    // Local restaurant cards
    private static final By RESTAURANT_RESULTS    = By.cssSelector("[data-hveid] .rllt__details, " +
                                                                    "div[jsaction*='mouseover'] .dbg0pd");
    private static final By RESTAURANT_NAME       = By.cssSelector(".dbg0pd span:first-child, .OSrXXb");
    private static final By RESTAURANT_RATING     = By.cssSelector(".BTtC6e, .Aq14fc");
    private static final By SORT_BY_BUTTON        = By.cssSelector("[aria-label*='Sort'], [data-sort-by]");
    private static final By SORT_BY_RATING_OPTION = By.xpath("//li[contains(., 'Highest rated') or contains(., 'Rating')]");

    // Generic "More results" / "See more" inside local panel
    private static final By SEE_MORE_RESTAURANTS  = By.xpath("//a[contains(., 'More places') or contains(., 'More results')]");

    public GoogleSearchPage(WebDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────

    /**
     * Navigates to Google and accepts the cookie consent banner if it appears.
     */
    public GoogleSearchPage open(String baseUrl) {
        navigateTo(baseUrl);
        acceptConsentIfPresent();
        return this;
    }

    /**
     * Types the given query into the search box and submits.
     */
    public GoogleSearchPage search(String query) {
        log.info("Searching for: '{}'", query);
        WebElement input = getSearchInput();
        input.clear();
        input.sendKeys(query);
        input.sendKeys(Keys.ENTER);
        return this;
    }

    // ── Weather widget queries ────────────────────────────────────────

    public boolean isWeatherWidgetPresent() {
        return isDisplayed(WEATHER_WIDGET);
    }

    public String getWeatherLocationLabel() {
        return getText(WEATHER_LOCATION_LABEL).trim();
    }

    public String getWeatherTemperature() {
        return getText(WEATHER_TEMP).trim();
    }

    public String getWeatherCondition() {
        return getText(WEATHER_CONDITION).trim();
    }

    // ── Restaurant result queries ─────────────────────────────────────

    public List<WebElement> getRestaurantCards() {
        return WaitUtils.waitForAllVisible(driver, RESTAURANT_RESULTS);
    }

    public boolean isSortButtonPresent() {
        return isDisplayed(SORT_BY_BUTTON);
    }

    /**
     * Clicks the "Sort by" button and then selects "Highest rated".
     */
    public GoogleSearchPage sortByHighestRated() {
        log.info("Sorting restaurants by highest rated");
        click(SORT_BY_BUTTON);
        WaitUtils.waitForClickable(driver, SORT_BY_RATING_OPTION).click();
        return this;
    }

    /**
     * Returns the star-rating text for the Nth restaurant card (0-based).
     */
    public String getRatingForCard(int index) {
        List<WebElement> cards = getRestaurantCards();
        if (index >= cards.size()) {
            throw new IndexOutOfBoundsException("Only " + cards.size() + " restaurant cards found");
        }
        WebElement card = cards.get(index);
        List<WebElement> ratingEls = card.findElements(By.cssSelector(".BTtC6e, .Aq14fc, [aria-label*='stars']"));
        if (ratingEls.isEmpty()) return "0.0";
        return ratingEls.get(0).getText().replace(",", ".").trim();
    }

    // ── Private helpers ───────────────────────────────────────────────

    private WebElement getSearchInput() {
        try {
            return WaitUtils.waitForClickable(driver, SEARCH_INPUT);
        } catch (Exception e) {
            return WaitUtils.waitForClickable(driver, SEARCH_INPUT_ALT);
        }
    }

    private void acceptConsentIfPresent() {
        try {
            WaitUtils.waitForClickable(driver, CONSENT_ACCEPT).click();
            log.info("Cookie consent accepted");
        } catch (Exception e) {
            log.debug("No consent dialog found, continuing");
        }
    }
}
