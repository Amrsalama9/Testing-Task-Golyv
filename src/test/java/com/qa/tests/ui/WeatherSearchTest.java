package com.qa.tests.ui;

import com.qa.pages.GoogleSearchPage;
import com.qa.tests.BaseTest;
import com.qa.utils.ExtentReportManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class WeatherSearchTest extends BaseTest {

    @Test(description = "TC_WX_001 - weather search returns a SERP", groups = {"ui"})
    public void weatherSearchReturnsSERP() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getWeatherQuery());

        String url = driver.getCurrentUrl();
        log.info("SERP URL: {}", url);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(url).containsIgnoringCase("marsa");
        soft.assertAll();
    }

    @Test(description = "TC_WX_002 - weather widget appears on results page", groups = {"ui"}, priority = 2)
    public void weatherWidgetIsVisible() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getWeatherQuery());

        boolean visible = page.isWeatherWidgetPresent();
        log.info("Weather widget visible: {}", visible);
        ExtentReportManager.getTest().info("Widget displayed: " + visible);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(visible)
            .as("weather widget should show up for 'Marsa Alam weather'")
            .isTrue();
        soft.assertAll();
    }

    @Test(description = "TC_WX_003 - widget location label matches Marsa Alam", groups = {"ui"}, priority = 3)
    public void weatherLocationIsCorrect() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getWeatherQuery());

        String location = page.getWeatherLocationLabel();
        log.info("Location label: '{}'", location);
        ExtentReportManager.getTest().info("Location: " + location);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(location.toLowerCase())
            .as("location label should say Marsa Alam, not some other city")
            .containsIgnoringCase("marsa alam");
        soft.assertAll();
    }

    @Test(description = "TC_WX_004 - widget shows a temperature", groups = {"ui"}, priority = 4)
    public void weatherTemperatureIsPresent() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getWeatherQuery());

        String temp = page.getWeatherTemperature();
        log.info("Temperature: '{}'", temp);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(temp).as("temperature field should not be empty").isNotBlank();
        soft.assertAll();
    }

    @Test(description = "TC_WX_005 - widget shows a weather condition", groups = {"ui"}, priority = 5)
    public void weatherConditionIsPresent() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getWeatherQuery());

        String condition = page.getWeatherCondition();
        log.info("Condition: '{}'", condition);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(condition).as("should show something like Sunny, Cloudy etc").isNotBlank();
        soft.assertAll();
    }

    @Test(description = "TC_WX_006 - temperature is within realistic range for Marsa Alam", groups = {"ui"}, priority = 6)
    public void weatherTemperatureIsRealistic() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getWeatherQuery());

        // strip everything except digits and minus sign
        String raw = page.getWeatherTemperature().replaceAll("[^\\d\\-]", "").trim();
        log.info("Parsed temp: '{}'", raw);

        SoftAssertions soft = new SoftAssertions();
        if (raw.isEmpty()) {
            soft.fail("could not extract a numeric temperature from the widget");
        } else {
            int temp = Integer.parseInt(raw);
            // Marsa Alam is on the Red Sea coast - below 5 or above 55 would be wrong data
            soft.assertThat(temp).as("temp should be between 5 and 55 C").isBetween(5, 55);
        }
        soft.assertAll();
    }
}
