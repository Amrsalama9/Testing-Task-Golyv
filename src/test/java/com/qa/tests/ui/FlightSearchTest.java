package com.qa.tests.ui;

import com.qa.pages.GoogleFlightsPage;
import com.qa.tests.BaseTest;
import com.qa.utils.ExtentReportManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * UI Test Suite: Google Flights – Cairo (CAI) → Marsa Alam (RMF)
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Test Cases Covered:
 *   TC_FL_001  Flights search page loads successfully
 *   TC_FL_002  Origin field accepts "Cairo" without error
 *   TC_FL_003  Destination field accepts "Marsa Alam" without error
 *   TC_FL_004  At least one flight result is returned
 *   TC_FL_005  Route label / page title references both cities
 *   TC_FL_006  Each visible result card shows an airline name
 *   TC_FL_007  Each visible result card shows a price
 *   TC_FL_008  No "no flights found" banner is displayed
 */
public class FlightSearchTest extends BaseTest {

    // ── TC_FL_001 ──────────────────────────────────────────────────────────

    @Test(description = "TC_FL_001 – Google Flights page loads and is accessible",
          priority = 1)
    public void flightsPageLoads() {
        ExtentReportManager.getTest().info("Navigating to Google Flights");
        GoogleFlightsPage flightsPage = new GoogleFlightsPage(driver);
        flightsPage.open(config.getGoogleFlightsUrl());

        String title = driver.getTitle();
        log.info("Page title: {}", title);

        ExtentReportManager.getTest().info("Page title: " + title);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(title)
            .as("Page title should reference 'Flights'")
            .containsIgnoringCase("Flights");
        soft.assertAll();
    }

    // ── TC_FL_002 + TC_FL_003 + TC_FL_004 ─────────────────────────────────

    @Test(description = "TC_FL_002-004 – Search Cairo → Marsa Alam returns results",
          priority = 2)
    public void searchCairoToMarsaAlamReturnsResults() {
        GoogleFlightsPage flightsPage = new GoogleFlightsPage(driver);
        flightsPage.open(config.getGoogleFlightsUrl())
                   .searchFlights(config.getOriginCity(), config.getDestinationCity());

        ExtentReportManager.getTest().info("Flight search submitted");

        SoftAssertions soft = new SoftAssertions();

        // TC_FL_004 – results exist
        int count = flightsPage.getResultCount();
        log.info("Flight result count: {}", count);
        ExtentReportManager.getTest().info("Result cards found: " + count);

        soft.assertThat(count)
            .as("At least one flight result should be returned for Cairo → Marsa Alam")
            .isGreaterThan(0);

        // TC_FL_008 – no "no flights" banner
        soft.assertThat(flightsPage.isNoResultsMessageDisplayed())
            .as("'No flights found' message should NOT be displayed")
            .isFalse();

        soft.assertAll();
    }

    // ── TC_FL_005 ──────────────────────────────────────────────────────────

    @Test(description = "TC_FL_005 – Results page route label references both cities",
          priority = 3)
    public void resultsPageShowsCorrectRoute() {
        GoogleFlightsPage flightsPage = new GoogleFlightsPage(driver);
        flightsPage.open(config.getGoogleFlightsUrl())
                   .searchFlights(config.getOriginCity(), config.getDestinationCity());

        String routeText = flightsPage.getRouteLabelText();
        log.info("Route label / title: {}", routeText);
        ExtentReportManager.getTest().info("Route text: " + routeText);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(routeText.toLowerCase())
            .as("Route label should mention Cairo or CAI")
            .containsAnyOf("cairo", "cai");
        soft.assertThat(routeText.toLowerCase())
            .as("Route label should mention Marsa Alam or RMF")
            .containsAnyOf("marsa alam", "rmf", "marsa");
        soft.assertAll();
    }

    // ── TC_FL_006 + TC_FL_007 ─────────────────────────────────────────────

    @Test(description = "TC_FL_006-007 – First result card shows airline name and price",
          priority = 4)
    public void firstResultHasAirlineAndPrice() {
        GoogleFlightsPage flightsPage = new GoogleFlightsPage(driver);
        flightsPage.open(config.getGoogleFlightsUrl())
                   .searchFlights(config.getOriginCity(), config.getDestinationCity());

        String airline = flightsPage.getAirlineForResult(0);
        String price   = flightsPage.getPriceForResult(0);

        log.info("First result – Airline: '{}', Price: '{}'", airline, price);
        ExtentReportManager.getTest().info("Airline: " + airline + " | Price: " + price);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(airline)
            .as("Airline name should not be empty")
            .isNotBlank();
        soft.assertThat(price)
            .as("Price should not be empty or N/A")
            .isNotBlank()
            .isNotEqualTo("N/A");
        soft.assertAll();
    }
}
