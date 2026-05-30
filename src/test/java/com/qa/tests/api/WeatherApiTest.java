package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * OpenWeatherMap current weather tests for Marsa Alam.
 * Endpoint: GET https://api.openweathermap.org/data/2.5/weather
 *
 * Tests skip if OWM_API_KEY is not set.
 */
public class WeatherApiTest extends BaseApiTest {

    private static final String WEATHER_PATH = "/weather";

    @Test(description = "TC_API_WX_001-008 - weather response for Marsa Alam is correct", groups = {"api"})
    public void marsaAlamWeatherIsValid() {
        skipIfNoKey();

        Response response = given().spec(requestSpec)
                .baseUri(config.getOwmBaseUrl())
                .queryParam("q",     "Marsa Alam,EG")
                .queryParam("units", "metric")
                .queryParam("appid", config.getOwmApiKey())
                .get(WEATHER_PATH);

        long ms = response.time();
        log.info("OWM response time: {}ms", ms);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.statusCode()).as("expect HTTP 200").isEqualTo(200);

        String name = response.jsonPath().getString("name");
        log.info("City in response: {}", name);
        soft.assertThat(name).as("name should reference Marsa Alam").isNotNull().containsIgnoringCase("Marsa");

        Double temp = response.jsonPath().get("main.temp");
        soft.assertThat(temp).as("main.temp should be present").isNotNull();
        if (temp != null) {
            soft.assertThat(temp).as("temp should be realistic for Marsa Alam, between 5 and 55 Celsius").isBetween(5.0, 55.0);
        }

        String description = response.jsonPath().getString("weather[0].description");
        log.info("Weather description: {}", description);
        soft.assertThat(description).as("weather description should not be blank").isNotNull().isNotBlank();

        // Marsa Alam is roughly 25.07N, 34.89E
        Double lat = response.jsonPath().get("coord.lat");
        Double lon = response.jsonPath().get("coord.lon");
        if (lat != null && lon != null) {
            soft.assertThat(lat).as("lat should be in range for Marsa Alam").isBetween(23.0, 27.0);
            soft.assertThat(lon).as("lon should be in range for Marsa Alam").isBetween(33.0, 37.0);
        }

        Double windSpeed = response.jsonPath().get("wind.speed");
        if (windSpeed != null) {
            soft.assertThat(windSpeed).as("wind speed should not be negative").isGreaterThanOrEqualTo(0.0);
        }

        soft.assertThat(ms).as("should respond in under 3 seconds").isLessThan(3000L);

        soft.assertAll();
    }

    @Test(description = "TC_API_WX_009 - invalid key returns 401", groups = {"api"}, priority = 2)
    public void invalidKeyReturns401() {
        given().spec(requestSpec)
               .baseUri(config.getOwmBaseUrl())
               .queryParam("q",     "Marsa Alam,EG")
               .queryParam("units", "metric")
               .queryParam("appid", "INVALID_KEY_00000000000000000000000000")
               .get(WEATHER_PATH)
               .then()
               .statusCode(401);
    }

    @Test(description = "TC_API_WX_010 - non-existent city returns 404", groups = {"api"}, priority = 3)
    public void badCityReturns404() {
        skipIfNoKey();

        given().spec(requestSpec)
               .baseUri(config.getOwmBaseUrl())
               .queryParam("q",     "XXXXXXXXXXX_NOT_A_REAL_CITY")
               .queryParam("units", "metric")
               .queryParam("appid", config.getOwmApiKey())
               .get(WEATHER_PATH)
               .then()
               .statusCode(404);
    }

    private void skipIfNoKey() {
        if (config.getOwmApiKey().isEmpty()) {
            throw new SkipException("OWM_API_KEY not set - skipping. Get a free key at openweathermap.org/api");
        }
    }
}
