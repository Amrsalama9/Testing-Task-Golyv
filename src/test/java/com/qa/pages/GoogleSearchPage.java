package com.qa.pages;

import com.qa.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Covers Google Search home page and the SERP.
 * Used for both the weather query and the restaurant query - same search box, different widgets on the results page.
 */
public class GoogleSearchPage extends BasePage {

    // Google changes these class names fairly often, so keeping a fallback for the main ones
    private static final By SEARCH_INPUT     = By.name("q");
    private static final By SEARCH_INPUT_ALT = By.cssSelector("textarea[name='q']");

    private static final By CONSENT_ACCEPT   = By.cssSelector("button[id='L2AGLb'], div[id='introAgreeButton']");

    private static final By WEATHER_WIDGET         = By.cssSelector("#wob_wc, [data-attrid='weather:observation']");
    private static final By WEATHER_LOCATION_LABEL = By.cssSelector("#wob_loc, [data-attrid='weather:observation'] [class*='location']");
    private static final By WEATHER_TEMP           = By.cssSelector("#wob_tm, [class*='wob_t']");
    private static final By WEATHER_CONDITION      = By.cssSelector("#wob_dc");

    private static final By RESTAURANT_RESULTS    = By.cssSelector("[data-hveid] .rllt__details, div[jsaction*='mouseover'] .dbg0pd");
    private static final By SORT_BY_BUTTON        = By.cssSelector("[aria-label*='Sort'], [data-sort-by]");
    private static final By SORT_BY_RATING_OPTION = By.xpath("//li[contains(., 'Highest rated') or contains(., 'Rating')]");

    public GoogleSearchPage(WebDriver driver) {
        super(driver);
    }

    public GoogleSearchPage open(String baseUrl) {
        navigateTo(baseUrl);
        dismissConsentIfNeeded();
        return this;
    }

    public GoogleSearchPage search(String query) {
        log.info("Searching: '{}'", query);
        WebElement input = getSearchInput();
        input.clear();
        input.sendKeys(query);
        input.sendKeys(Keys.ENTER);
        return this;
    }

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

    public List<WebElement> getRestaurantCards() {
        return WaitUtils.waitForAllVisible(driver, RESTAURANT_RESULTS);
    }

    public boolean isSortButtonPresent() {
        return isDisplayed(SORT_BY_BUTTON);
    }

    public GoogleSearchPage sortByHighestRated() {
        click(SORT_BY_BUTTON);
        WaitUtils.waitForClickable(driver, SORT_BY_RATING_OPTION).click();
        return this;
    }

    public String getRatingForCard(int index) {
        List<WebElement> cards = getRestaurantCards();
        if (index >= cards.size()) {
            throw new IndexOutOfBoundsException("Expected card at index " + index + " but only " + cards.size() + " cards found");
        }
        List<WebElement> stars = cards.get(index).findElements(By.cssSelector(".BTtC6e, .Aq14fc, [aria-label*='stars']"));
        if (stars.isEmpty()) return "0.0";
        return stars.get(0).getText().replace(",", ".").trim();
    }

    private WebElement getSearchInput() {
        try {
            return WaitUtils.waitForClickable(driver, SEARCH_INPUT);
        } catch (Exception e) {
            return WaitUtils.waitForClickable(driver, SEARCH_INPUT_ALT);
        }
    }

    private void dismissConsentIfNeeded() {
        try {
            WaitUtils.waitForClickable(driver, CONSENT_ACCEPT).click();
            log.info("Dismissed cookie consent");
        } catch (Exception e) {
            // no consent dialog, continuing
        }
    }
}
