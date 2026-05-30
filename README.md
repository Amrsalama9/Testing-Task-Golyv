# Trip Planner – QA Automation Suite

End-to-end and API automation covering the Cairo → Marsa Alam travel planning scenario on Google Search and Google Flights.

---

## Scenario Overview

A user planning a trip performs the following steps:

1. Searches for **flight options from Cairo to Marsa Alam** via Google Flights
2. Looks up the **weather in Marsa Alam** via Google Search
3. Verifies the weather widget is relevant to Marsa Alam
4. Searches for **restaurants near Marsa Alam** via Google Search
5. Sorts results by **Highest Rated**

---

## Project Structure

```
trip-planner-qa/
├── .devcontainer/
│   └── devcontainer.json          # GitHub Codespaces configuration
├── .github/
│   └── workflows/
│       └── ci.yml                 # GitHub Actions CI pipeline
├── src/
│   └── test/
│       ├── java/com/qa/
│       │   ├── config/
│       │   │   └── ConfigReader.java
│       │   ├── listeners/
│       │   │   ├── ExtentReportListener.java
│       │   │   ├── RetryListener.java
│       │   │   └── ScreenshotListener.java
│       │   ├── pages/
│       │   │   ├── BasePage.java
│       │   │   ├── GoogleFlightsPage.java
│       │   │   └── GoogleSearchPage.java
│       │   ├── tests/
│       │   │   ├── BaseTest.java
│       │   │   ├── api/
│       │   │   │   ├── BaseApiTest.java
│       │   │   │   ├── FlightApiTest.java
│       │   │   │   ├── WeatherApiTest.java
│       │   │   │   └── RestaurantApiTest.java
│       │   │   └── ui/
│       │   │       ├── FlightSearchTest.java
│       │   │       ├── WeatherSearchTest.java
│       │   │       └── RestaurantSearchTest.java
│       │   └── utils/
│       │       ├── DriverFactory.java
│       │       ├── ExtentReportManager.java
│       │       ├── RetryAnalyzer.java
│       │       ├── ScreenshotUtils.java
│       │       └── WaitUtils.java
│       └── resources/
│           ├── config.properties
│           ├── logback-test.xml
│           └── testng.xml
├── docs/
│   └── TEST_CASES.md
├── pom.xml
└── README.md
```

---

## Design Patterns Applied

| Pattern | Where used |
|---|---|
| **Page Object Model (POM)** | `pages/` – every page is its own class extending `BasePage` |
| **Singleton** | `ConfigReader`, `ExtentReportManager` |
| **Factory** | `DriverFactory` – creates the right WebDriver by config |
| **Thread-Local** | `DriverFactory` & `ExtentReportManager` – parallel-test-safe |
| **Builder** | REST Assured `RequestSpecBuilder` in `BaseApiTest` |
| **Template Method** | `BaseTest.setUp()` / `tearDown()` → subclasses just write `@Test` methods |
| **Fluent Interface** | Page methods return `this` for readable chained calls |
| **Listener / Observer** | TestNG `ITestListener` used for reporting, screenshots, retry |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build | Maven 3.x |
| Browser automation | Selenium 4 |
| Driver management | WebDriverManager 5 |
| API testing | REST Assured 5 |
| Test runner | TestNG 7 |
| Reporting | ExtentReports 5 (HTML, dark theme) |
| Logging | SLF4J + Logback |
| Assertions | AssertJ (fluent soft assertions) |
| CI/CD | GitHub Actions |
| Dev environment | GitHub Codespaces |

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Google Chrome (or Firefox / Edge)

### Run all tests (headless)

```bash
mvn test
```

### Run with a specific browser

```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

### Run only UI tests

```bash
mvn test -Dgroups=ui
```

### Run only API tests

```bash
mvn test -Dgroups=api
```

---

## GitHub Codespaces

Click **Code → Codespaces → New codespace** on the repository page.
The container installs Java 17, Maven, and Chrome automatically via `devcontainer.json`.
Once the terminal is ready:

```bash
mvn test
```

---

## Environment Variables (API Tests)

API tests skip gracefully when credentials are absent; they never fail hard.
Set these in your shell, Codespace secrets, or GitHub Actions secrets:

| Variable | Purpose |
|---|---|
| `OWM_API_KEY` | OpenWeatherMap API key |
| `GOOGLE_PLACES_KEY` | Google Places API key |
| `AMADEUS_CLIENT_ID` | Amadeus for Developers – client ID |
| `AMADEUS_CLIENT_SECRET` | Amadeus for Developers – client secret |

---

## Test Reports

After a run, open:

```
test-output/reports/TripPlannerReport_<timestamp>.html
```

Screenshots on failure are saved under:

```
test-output/screenshots/
```

---

## Test Cases Summary

See [docs/TEST_CASES.md](docs/TEST_CASES.md) for the full test case catalogue with steps, expected results, and priority.

---

## CI Pipeline

`.github/workflows/ci.yml` runs on every push to `main` or `develop`:

1. **Compile & Validate** – Maven compilation check
2. **UI Tests** – Chrome headless, uploads HTML report and failure screenshots as artifacts
3. **API Tests** – Reads secrets from GitHub Actions, skips gracefully if not configured

---

## Retry Strategy

Flaky tests (network-dependent Google UI) are retried up to **2 times** automatically via `RetryAnalyzer` + `RetryListener` before being marked as failures.

---

## Contributing

1. Branch from `develop`
2. Add / update page objects and test classes
3. Run `mvn test` locally before pushing
4. CI gates must pass before merge to `main`
