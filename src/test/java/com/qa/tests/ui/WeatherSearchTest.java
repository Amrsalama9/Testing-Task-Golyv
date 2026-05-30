package com.qa.tests.ui;

import com.qa.pages.GoogleSearchPage;
import com.qa.tests.BaseTest;
import com.qa.utils.ExtentReportManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * UI Test Suite: Google Search – "Marsa Alam weather"
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Test Cases Covered:
 *   TC_WX_001  Searching "Marsa Alam weather" returns a SERP
 *   TC_WX_002  Weather widget is visible on the SERP
 *   TC_WX_003  Location label in the widget contains "Marsa Alam"
 *   TC_WX_004  Widget shows a temperature value (numeric, not blank)
 *   TC_WX_005  Widget shows a weather condition (e.g., Sunny, Clear)
 *   TC_WX_006  Temperature is in a realistic range for Marsa Alam (5°C–55°C)
 */
public class WeatherSearchTest extends BaseTest {

    // ── TC_WX_001 ──────────────────────────────────────────────────────────

    @Test(description = "TC_WX_001 – Searching 'Marsa Alam weather' loads a SERP",
          priority = 1)
    public void weatherSearchReturnsSERP() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getWeatherQuery());

        String url = driver.getCurrentUrl();
        log.info("SERP URL: {}", url);
        ExtentReportManager.getTest().info("SERP URL: " + url);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(url)
            .as("URL should contain the search query parameter")
            .containsIgnoringCase("marsa");
        soft.assertAll();
    }

    // ── TC_WX_002 ──────────────────────────────────────────────────────────

    @Test(description = "TC_WX_002 – Weather widget is present on SERP",
          priority = 2)
    public void weatherWidgetIsVisible() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getWeatherQuery());

        boolean visible = searchPage.isWeatherWidgetPresent();
        log.info("Weather widget visible: {}", visible);
        ExtentReportManager.getTest().info("Weather widget displayed: " + visible);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(visible)
            .as("Google weather widget should appear for the query 'Marsa Alam weather'")
            .isTrue();
        soft.assertAll();
    }

    // ── TC_WX_003 ──────────────────────────────────────────────────────────

    @Test(description = "TC_WX_003 – Weather widget location label references Marsa Alam",
          priority = 3)
    public void weatherLocationIsCorrect() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getWeatherQuery());

        String location = searchPage.getWeatherLocationLabel();
        log.info("Weather location label: '{}'", location);
        ExtentReportManager.getTest().info("Location label: " + location);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(location.toLowerCase())
            .as("Location label should contain 'Marsa Alam'")
            .containsIgnoringCase("marsa alam");
        soft.assertAll();
    }

    // ── TC_WX_004 ──────────────────────────────────────────────────────────

    @Test(description = "TC_WX_004 – Weather widget shows a non-blank temperature",
          priority = 4)
    public void weatherTemperatureIsPresent() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getWeatherQuery());

        String temp = searchPage.getWeatherTemperature();
        log.info("Temperature displayed: '{}'", temp);
        ExtentReportManager.getTest().info("Temperature: " + temp);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(temp)
            .as("Temperature should not be empty")
            .isNotBlank();
        soft.assertAll();
    }

    // ── TC_WX_005 ──────────────────────────────────────────────────────────

    @Test(description = "TC_WX_005 – Weather widget shows a condition string",
          priority = 5)
    public void weatherConditionIsPresent() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getWeatherQuery());

        String condition = searchPage.getWeatherCondition();
        log.info("Weather condition: '{}'", condition);
        ExtentReportManager.getTest().info("Condition: " + condition);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(condition)
            .as("Weather condition text should not be blank")
            .isNotBlank();
        soft.assertAll();
    }

    // ── TC_WX_006 ──────────────────────────────────────────────────────────

    @Test(description = "TC_WX_006 – Temperature is within the plausible range for Marsa Alam",
          priority = 6)
    public void weatherTemperatureIsRealistic() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getWeatherQuery());

        String rawTemp = searchPage.getWeatherTemperature().replaceAll("[^\\d\\-]", "").trim();
        log.info("Parsed temperature value: '{}'", rawTemp);
        ExtentReportManager.getTest().info("Numeric temperature: " + rawTemp);

        SoftAssertions soft = new SoftAssertions();
        if (!rawTemp.isEmpty()) {
            int temp = Integer.parseInt(rawTemp);
            soft.assertThat(temp)
                .as("Temperature for Marsa Alam should be between 5°C and 55°C")
                .isBetween(5, 55);
        } else {
            soft.fail("Could not extract numeric temperature from widget");
        }
        soft.assertAll();
    }
}
