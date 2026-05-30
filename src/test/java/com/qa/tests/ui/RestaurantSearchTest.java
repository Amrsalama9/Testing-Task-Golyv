package com.qa.tests.ui;

import com.qa.pages.GoogleSearchPage;
import com.qa.tests.BaseTest;
import com.qa.utils.ExtentReportManager;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.List;

public class RestaurantSearchTest extends BaseTest {

    @Test(description = "TC_RS_001 - restaurant search loads a SERP", groups = {"ui"})
    public void restaurantSearchReturnsSERP() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getRestaurantQuery());

        String url = driver.getCurrentUrl();
        log.info("SERP URL: {}", url);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(url).containsIgnoringCase("marsa");
        soft.assertAll();
    }

    @Test(description = "TC_RS_002 - at least one restaurant card is shown", groups = {"ui"}, priority = 2)
    public void restaurantResultsArePresent() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getRestaurantQuery());

        List<WebElement> cards = page.getRestaurantCards();
        log.info("Cards found: {}", cards.size());
        ExtentReportManager.getTest().info("Restaurant cards: " + cards.size());

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(cards).as("local panel should have at least one restaurant").isNotEmpty();
        soft.assertAll();
    }

    @Test(description = "TC_RS_003 - sort button is available", groups = {"ui"}, priority = 3)
    public void sortButtonIsPresent() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getRestaurantQuery());

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(page.isSortButtonPresent())
            .as("sort/filter button should be visible in the local results panel")
            .isTrue();
        soft.assertAll();
    }

    @Test(description = "TC_RS_004 - sorting by highest rated does not break the page", groups = {"ui"}, priority = 4)
    public void sortByHighestRatedWorks() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getRestaurantQuery());
        page.sortByHighestRated();

        List<WebElement> cards = page.getRestaurantCards();
        log.info("Cards after sort: {}", cards.size());

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(cards).as("results should still be there after sorting").isNotEmpty();
        soft.assertAll();
    }

    @Test(description = "TC_RS_005 - after highest rated sort, first card rating >= last card rating", groups = {"ui"}, priority = 5)
    public void sortedResultsAreInDescendingOrder() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getRestaurantQuery());
        page.sortByHighestRated();

        List<WebElement> cards = page.getRestaurantCards();

        if (cards.size() < 2) {
            log.warn("Only {} card(s) - cannot verify sort order with a single result", cards.size());
            return;
        }

        String firstRaw = page.getRatingForCard(0);
        String lastRaw  = page.getRatingForCard(cards.size() - 1);
        log.info("First: {} | Last: {}", firstRaw, lastRaw);
        ExtentReportManager.getTest().info("First rating: " + firstRaw + " | Last rating: " + lastRaw);

        SoftAssertions soft = new SoftAssertions();
        try {
            double first = Double.parseDouble(firstRaw);
            double last  = Double.parseDouble(lastRaw);
            soft.assertThat(first)
                .as("first card should have a rating >= last card after sorting by highest rated")
                .isGreaterThanOrEqualTo(last);
        } catch (NumberFormatException e) {
            soft.fail("could not parse ratings - first=" + firstRaw + " last=" + lastRaw);
        }
        soft.assertAll();
    }

    @Test(description = "TC_RS_006 - every visible card has some text", groups = {"ui"}, priority = 6)
    public void allCardsHaveText() {
        GoogleSearchPage page = new GoogleSearchPage(driver);
        page.open(config.getGoogleBaseUrl()).search(config.getRestaurantQuery());

        List<WebElement> cards = page.getRestaurantCards();
        SoftAssertions soft = new SoftAssertions();
        for (int i = 0; i < cards.size(); i++) {
            soft.assertThat(cards.get(i).getText())
                .as("card[" + i + "] should have visible text")
                .isNotBlank();
        }
        soft.assertAll();
    }
}
