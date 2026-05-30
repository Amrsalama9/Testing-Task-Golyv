package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * API Test Suite: OpenWeatherMap – Current Weather
 * Endpoint: GET /data/2.5/weather
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Test Cases:
 *   TC_API_WX_001  HTTP 200 returned for "Marsa Alam" query
 *   TC_API_WX_002  Response body 'name' field contains "Marsa Alam"
 *   TC_API_WX_003  'main.temp' is present and numeric
 *   TC_API_WX_004  Temperature is within a realistic range (278 K – 328 K ≈ 5°C–55°C)
 *   TC_API_WX_005  'weather[0].description' is non-blank
 *   TC_API_WX_006  'coord.lat' and 'coord.lon' are within Marsa Alam boundaries
 *   TC_API_WX_007  'wind.speed' is present and non-negative
 *   TC_API_WX_008  Response time is under 3000 ms
 *   TC_API_WX_009  HTTP 401 returned when API key is invalid
 *   TC_API_WX_010  HTTP 404 returned for a non-existent city name
 */
public class WeatherApiTest extends BaseApiTest {

    private static final String WEATHER_PATH = "/weather";

    // ── TC_API_WX_001 + core payload assertions ────────────────────────────

    @Test(description = "TC_API_WX_001-008 – Current weather for Marsa Alam is valid",
          priority = 1)
    public void marsaAlamWeatherResponseIsValid() {
        assumeApiKey();

        Response response = given().spec(requestSpec)
                .baseUri(config.getOwmBaseUrl())
                .queryParam("q",     "Marsa Alam,EG")
                .queryParam("units", "metric")
                .queryParam("appid", config.getOwmApiKey())
                .get(WEATHER_PATH);

        long responseTime = response.time();
        log.info("OWM response time: {} ms", responseTime);

        SoftAssertions soft = new SoftAssertions();

        // TC_API_WX_001
        soft.assertThat(response.statusCode())
            .as("TC_API_WX_001: HTTP 200 expected")
            .isEqualTo(200);

        // TC_API_WX_002
        String cityName = response.jsonPath().getString("name");
        log.info("City name in response: '{}'", cityName);
        soft.assertThat(cityName)
            .as("TC_API_WX_002: 'name' field should reference Marsa Alam")
            .isNotNull()
            .containsIgnoringCase("Marsa");

        // TC_API_WX_003
        Double temp = response.jsonPath().get("main.temp");
        soft.assertThat(temp)
            .as("TC_API_WX_003: 'main.temp' must be present and numeric")
            .isNotNull();

        // TC_API_WX_004
        if (temp != null) {
            soft.assertThat(temp)
                .as("TC_API_WX_004: temperature should be between 5°C and 55°C for Marsa Alam")
                .isBetween(5.0, 55.0);
        }

        // TC_API_WX_005
        String description = response.jsonPath().getString("weather[0].description");
        log.info("Weather description: '{}'", description);
        soft.assertThat(description)
            .as("TC_API_WX_005: weather description must not be blank")
            .isNotNull()
            .isNotBlank();

        // TC_API_WX_006
        Double lat = response.jsonPath().get("coord.lat");
        Double lon = response.jsonPath().get("coord.lon");
        // Marsa Alam is roughly 25.07°N, 34.89°E
        if (lat != null && lon != null) {
            soft.assertThat(lat)
                .as("TC_API_WX_006: latitude should be in Marsa Alam region (~24–26°N)")
                .isBetween(23.0, 27.0);
            soft.assertThat(lon)
                .as("TC_API_WX_006: longitude should be in Marsa Alam region (~33–37°E)")
                .isBetween(33.0, 37.0);
        }

        // TC_API_WX_007
        Double windSpeed = response.jsonPath().get("wind.speed");
        if (windSpeed != null) {
            soft.assertThat(windSpeed)
                .as("TC_API_WX_007: wind speed must be non-negative")
                .isGreaterThanOrEqualTo(0.0);
        }

        // TC_API_WX_008
        soft.assertThat(responseTime)
            .as("TC_API_WX_008: response time must be under 3000 ms")
            .isLessThan(3000L);

        soft.assertAll();
    }

    // ── TC_API_WX_009 ──────────────────────────────────────────────────────

    @Test(description = "TC_API_WX_009 – Invalid API key returns HTTP 401",
          priority = 2)
    public void invalidApiKeyReturns401() {
        given().spec(requestSpec)
               .baseUri(config.getOwmBaseUrl())
               .queryParam("q",     "Marsa Alam,EG")
               .queryParam("units", "metric")
               .queryParam("appid", "INVALID_KEY_00000000000000000000000000")
               .get(WEATHER_PATH)
               .then()
               .statusCode(401);

        log.info("TC_API_WX_009 PASS – 401 returned for invalid key");
    }

    // ── TC_API_WX_010 ──────────────────────────────────────────────────────

    @Test(description = "TC_API_WX_010 – Non-existent city name returns HTTP 404",
          priority = 3)
    public void nonExistentCityReturns404() {
        assumeApiKey();

        given().spec(requestSpec)
               .baseUri(config.getOwmBaseUrl())
               .queryParam("q",     "XXXXXXXXXX_NOT_A_CITY")
               .queryParam("units", "metric")
               .queryParam("appid", config.getOwmApiKey())
               .get(WEATHER_PATH)
               .then()
               .statusCode(404);

        log.info("TC_API_WX_010 PASS – 404 returned for non-existent city");
    }

    // ── helper ────────────────────────────────────────────────────────────

    private void assumeApiKey() {
        if (config.getOwmApiKey().isEmpty()) {
            throw new SkipException(
                "OWM_API_KEY env var not set – test skipped. " +
                "Register at https://openweathermap.org/api and export OWM_API_KEY.");
        }
    }
}
