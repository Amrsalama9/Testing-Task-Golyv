# Testing Task – Golyv

Automation suite for the trip planning scenario: user searches for flights from Cairo to Marsa Alam, checks the weather there, then looks up nearby restaurants and sorts them by rating.

Covers both the UI layer (Google Search + Google Flights via Selenium) and the API layer (Amadeus, OpenWeatherMap, Google Places via REST Assured).

---

## What's being tested

1. Google Flights – searching Cairo (CAI) → Marsa Alam (RMF) and verifying results come back with airline names and prices
2. Google Search weather widget – making sure the widget actually shows up, shows the right city, and has a sensible temperature
3. Google Search restaurants – searching "restaurant near Marsa Alam", sorting by Highest Rated, and confirming the order is actually descending

---

## Project layout

```
src/test/java/com/qa/
├── config/         ConfigReader – loads config.properties, env vars override file values
├── listeners/      TestNG listeners for reporting, screenshots, retry
├── pages/          Page Objects (BasePage, GoogleSearchPage, GoogleFlightsPage)
├── tests/
│   ├── ui/         FlightSearchTest, WeatherSearchTest, RestaurantSearchTest
│   └── api/        FlightApiTest, WeatherApiTest, RestaurantApiTest
└── utils/          DriverFactory, WaitUtils, ExtentReportManager, etc.
```

---

## Running it

Prerequisites: Java 17, Maven, Chrome installed.

```bash
# everything
mvn test

# just UI
mvn test -Dgroups=ui

# just API
mvn test -Dgroups=api

# different browser
mvn test -Dbrowser=firefox

# headed (useful when debugging a locator)
mvn test -Dheadless=false
```

Reports go to `test-output/reports/`. Screenshots on failure go to `test-output/screenshots/`.

---

## API keys

The API tests skip automatically if the keys aren't set — they won't fail the build. To run them properly:

```bash
export OWM_API_KEY=your_key            # openweathermap.org
export GOOGLE_PLACES_KEY=your_key      # Google Cloud Console → Places API
export AMADEUS_CLIENT_ID=your_id       # developers.amadeus.com
export AMADEUS_CLIENT_SECRET=your_sec
```

For CI, add these as repository secrets in GitHub → Settings → Secrets.

---

## Codespaces

Open the repo, click Code → Codespaces → New codespace. Java 17, Maven and Chrome are set up automatically by the devcontainer. Then just run `mvn test` in the terminal.

---

## Design notes

- **POM** – every page is its own class, locators are private constants, no raw Selenium calls in test classes
- **Thread-local driver** – each test class gets its own browser instance, safe to run in parallel
- **Soft assertions** – tests collect all failures before throwing, so you see everything that's wrong in one run rather than stopping at the first mismatch
- **Retry** – flaky network tests (Google UI especially) retry up to 2 times before being counted as failures
- **Config** – everything in `config.properties`, JVM system properties always win so `-Dbrowser=firefox` works without editing any file

---

## Test cases

Full catalogue with steps and expected results is in `docs/TEST_CASES.md` (also available as an Excel sheet).

52 test cases total: 20 UI, 32 API.
