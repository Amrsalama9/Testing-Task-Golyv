package com.qa.tests.ui;

import com.qa.pages.GoogleSearchPage;
import com.qa.tests.BaseTest;
import com.qa.utils.ExtentReportManager;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * UI Test Suite: Google Search – "restaurant near Marsa Alam"
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Test Cases Covered:
 *   TC_RS_001  Searching "restaurant near Marsa Alam" loads a SERP
 *   TC_RS_002  At least one restaurant result card is visible
 *   TC_RS_003  Sort-by button is present in the local results panel
 *   TC_RS_004  Clicking "Highest rated" reloads results without error
 *   TC_RS_005  After sorting, the first result's rating is >= the last result's rating
 *   TC_RS_006  Result cards contain a visible name / label
 */
public class RestaurantSearchTest extends BaseTest {

    // ── TC_RS_001 ──────────────────────────────────────────────────────────

    @Test(description = "TC_RS_001 – Searching 'restaurant near Marsa Alam' returns a SERP",
          priority = 1)
    public void restaurantSearchReturnsSERP() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getRestaurantQuery());

        String url = driver.getCurrentUrl();
        log.info("SERP URL: {}", url);
        ExtentReportManager.getTest().info("SERP URL: " + url);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(url)
            .as("URL should contain query-related parameter")
            .containsIgnoringCase("marsa");
        soft.assertAll();
    }

    // ── TC_RS_002 ──────────────────────────────────────────────────────────

    @Test(description = "TC_RS_002 – At least one restaurant result card is displayed",
          priority = 2)
    public void restaurantResultsArePresent() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getRestaurantQuery());

        List<WebElement> cards = searchPage.getRestaurantCards();
        log.info("Restaurant cards found: {}", cards.size());
        ExtentReportManager.getTest().info("Restaurant cards: " + cards.size());

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(cards)
            .as("At least one restaurant card should appear in the local panel")
            .isNotEmpty();
        soft.assertAll();
    }

    // ── TC_RS_003 ──────────────────────────────────────────────────────────

    @Test(description = "TC_RS_003 – Sort-by button is present in the results panel",
          priority = 3)
    public void sortButtonIsPresent() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getRestaurantQuery());

        boolean sortPresent = searchPage.isSortButtonPresent();
        log.info("Sort button present: {}", sortPresent);
        ExtentReportManager.getTest().info("Sort button visible: " + sortPresent);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(sortPresent)
            .as("A sort button should be available within the local results panel")
            .isTrue();
        soft.assertAll();
    }

    // ── TC_RS_004 ──────────────────────────────────────────────────────────

    @Test(description = "TC_RS_004 – Sorting by 'Highest rated' completes without error",
          priority = 4)
    public void sortByHighestRatedExecutesWithoutError() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getRestaurantQuery());

        searchPage.sortByHighestRated();

        List<WebElement> cards = searchPage.getRestaurantCards();
        log.info("Restaurant cards after sort: {}", cards.size());
        ExtentReportManager.getTest().info("Cards after sort: " + cards.size());

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(cards)
            .as("Results should still be present after sorting by rating")
            .isNotEmpty();
        soft.assertAll();
    }

    // ── TC_RS_005 ──────────────────────────────────────────────────────────

    @Test(description = "TC_RS_005 – After 'Highest rated' sort, first result rating >= last",
          priority = 5)
    public void sortedResultsAreInDescendingRatingOrder() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getRestaurantQuery());

        searchPage.sortByHighestRated();

        List<WebElement> cards = searchPage.getRestaurantCards();
        if (cards.size() < 2) {
            log.warn("Only {} card(s) returned – skipping ordering assertion", cards.size());
            ExtentReportManager.getTest().info("Not enough cards to verify order");
            return;
        }

        String firstRatingStr = searchPage.getRatingForCard(0);
        String lastRatingStr  = searchPage.getRatingForCard(cards.size() - 1);

        log.info("First rating: '{}', last rating: '{}'", firstRatingStr, lastRatingStr);
        ExtentReportManager.getTest().info(
            "First rating: " + firstRatingStr + " | Last rating: " + lastRatingStr);

        SoftAssertions soft = new SoftAssertions();
        try {
            double first = Double.parseDouble(firstRatingStr);
            double last  = Double.parseDouble(lastRatingStr);
            soft.assertThat(first)
                .as("First result rating should be >= last result rating after 'Highest rated' sort")
                .isGreaterThanOrEqualTo(last);
        } catch (NumberFormatException e) {
            log.warn("Could not parse rating values for ordering check");
            soft.fail("Rating values could not be parsed: first='" + firstRatingStr +
                      "', last='" + lastRatingStr + "'");
        }
        soft.assertAll();
    }

    // ── TC_RS_006 ──────────────────────────────────────────────────────────

    @Test(description = "TC_RS_006 – Each visible restaurant card has a non-blank name",
          priority = 6)
    public void allResultCardsHaveNames() {
        GoogleSearchPage searchPage = new GoogleSearchPage(driver);
        searchPage.open(config.getGoogleBaseUrl())
                  .search(config.getRestaurantQuery());

        List<WebElement> cards = searchPage.getRestaurantCards();
        ExtentReportManager.getTest().info("Checking names for " + cards.size() + " cards");

        SoftAssertions soft = new SoftAssertions();
        for (int i = 0; i < cards.size(); i++) {
            String text = cards.get(i).getText();
            soft.assertThat(text)
                .as("Restaurant card [" + i + "] should have visible text")
                .isNotBlank();
        }
        soft.assertAll();
    }
}
