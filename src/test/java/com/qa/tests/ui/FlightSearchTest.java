package com.qa.tests.ui;

import com.qa.pages.GoogleFlightsPage;
import com.qa.tests.BaseTest;
import com.qa.utils.ExtentReportManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class FlightSearchTest extends BaseTest {

    @Test(description = "TC_FL_001 - flights page loads")
    public void flightsPageLoads() {
        GoogleFlightsPage page = new GoogleFlightsPage(driver);
        page.open(config.getGoogleFlightsUrl());

        String title = driver.getTitle();
        log.info("Title: {}", title);
        ExtentReportManager.getTest().info("Page title: " + title);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(title).containsIgnoringCase("Flights");
        soft.assertAll();
    }

    @Test(description = "TC_FL_002-004 - search Cairo to Marsa Alam returns results", priority = 2)
    public void searchCairoToMarsaAlamReturnsResults() {
        GoogleFlightsPage page = new GoogleFlightsPage(driver);
        page.open(config.getGoogleFlightsUrl())
            .searchFlights(config.getOriginCity(), config.getDestinationCity());

        int count = page.getResultCount();
        log.info("Results found: {}", count);
        ExtentReportManager.getTest().info("Result count: " + count);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(count)
            .as("should return at least one flight for CAI->RMF")
            .isGreaterThan(0);
        soft.assertThat(page.isNoResultsMessageDisplayed())
            .as("no-results banner should not show up")
            .isFalse();
        soft.assertAll();
    }

    @Test(description = "TC_FL_005 - route label mentions both cities", priority = 3)
    public void resultsPageShowsCorrectRoute() {
        GoogleFlightsPage page = new GoogleFlightsPage(driver);
        page.open(config.getGoogleFlightsUrl())
            .searchFlights(config.getOriginCity(), config.getDestinationCity());

        String routeText = page.getRouteLabelText().toLowerCase();
        log.info("Route text: {}", routeText);
        ExtentReportManager.getTest().info("Route: " + routeText);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(routeText).containsAnyOf("cairo", "cai");
        soft.assertThat(routeText).containsAnyOf("marsa alam", "rmf", "marsa");
        soft.assertAll();
    }

    @Test(description = "TC_FL_006-007 - first result has airline name and price", priority = 4)
    public void firstResultHasAirlineAndPrice() {
        GoogleFlightsPage page = new GoogleFlightsPage(driver);
        page.open(config.getGoogleFlightsUrl())
            .searchFlights(config.getOriginCity(), config.getDestinationCity());

        String airline = page.getAirlineForResult(0);
        String price   = page.getPriceForResult(0);

        log.info("Airline: '{}' | Price: '{}'", airline, price);
        ExtentReportManager.getTest().info("Airline: " + airline + " | Price: " + price);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(airline).as("airline name should not be blank").isNotBlank();
        soft.assertThat(price).as("price should be visible").isNotBlank().isNotEqualTo("N/A");
        soft.assertAll();
    }
}
