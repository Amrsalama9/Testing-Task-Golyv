# Testing Task - Golyv

Automation suite for the trip planning scenario: a user searches for flights from Cairo to Marsa Alam, checks the weather there, then looks up nearby restaurants and sorts them by rating.

Covers both the UI layer (Google Search and Google Flights via Selenium) and the API layer (Amadeus, OpenWeatherMap, Google Places via REST Assured).

---

## What is being tested

1. Google Flights - searching Cairo (CAI) to Marsa Alam (RMF) and checking that results come back with airline names and prices
2. Google Search weather widget - making sure the widget shows up, shows the right city, and has a temperature that makes sense
3. Google Search restaurants - searching "restaurant near Marsa Alam", sorting by Highest Rated, and confirming the order is actually descending

---

## Project layout

```
src/test/java/com/qa/
    config/       ConfigReader - loads config.properties, env vars override file values
    listeners/    TestNG listeners for reporting, screenshots, retry
    pages/        Page Objects (BasePage, GoogleSearchPage, GoogleFlightsPage)
    tests/
        ui/       FlightSearchTest, WeatherSearchTest, RestaurantSearchTest
        api/      FlightApiTest, WeatherApiTest, RestaurantApiTest
    utils/        DriverFactory, WaitUtils, ExtentReportManager, etc.
```

---

## Running it

Prerequisites: Java 17, Maven, Chrome installed.

```bash
# run everything
mvn test

# just the UI tests
mvn test -Dgroups=ui

# just the API tests
mvn test -Dgroups=api

# different browser
mvn test -Dbrowser=firefox

# headed mode, useful when debugging a locator
mvn test -Dheadless=false
```

Reports land in test-output/reports and screenshots on failure go to test-output/screenshots.

---

## API keys

The API tests skip automatically if the keys are not set - they will not fail the build. To run them properly:

```bash
export OWM_API_KEY=your_key
export GOOGLE_PLACES_KEY=your_key
export AMADEUS_CLIENT_ID=your_id
export AMADEUS_CLIENT_SECRET=your_secret
```

For CI, add these as repository secrets under GitHub Settings then Secrets and variables.

---

## Mock servers

If you do not have real API keys yet, you can run the API tests against local mock servers that simulate all three external APIs:

```bash
python mock-server/mock_servers.py &
mvn test -Papi-mock
```

See mock-server/README.md for details.

---

## Codespaces

Open the repo, click Code then Codespaces then New codespace. Java 17, Maven and Chrome are set up automatically by the devcontainer. Then just run mvn test in the terminal.

---

## Design notes

- POM: every page is its own class, locators are private constants, no raw Selenium calls inside test classes
- Thread-local driver: each test class gets its own browser instance, safe to run in parallel
- Soft assertions: tests collect all failures before throwing, so you see everything wrong in one run
- Retry: flaky network-dependent tests retry up to 2 times before being marked as failures
- Config: everything in config.properties, JVM system properties always win so -Dbrowser=firefox works without editing any file

---

## Test cases

52 test cases total across 6 modules: 20 UI, 32 API.
Full catalogue with steps and expected results is in docs/TripPlanner_TestSchedule.xlsx.
