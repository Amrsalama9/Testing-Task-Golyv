package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Google Places API tests — text search for restaurants near Marsa Alam.
 * Endpoint: GET https://maps.googleapis.com/maps/api/place/textsearch/json
 *
 * Tests skip if GOOGLE_PLACES_KEY is not set.
 */
public class RestaurantApiTest extends BaseApiTest {

    private static final String TEXT_SEARCH   = "/textsearch/json";
    private static final String NEARBY_SEARCH = "/nearbysearch/json";
    private static final String QUERY         = "restaurant near Marsa Alam";

    @Test(description = "TC_API_RS_001-008,010,012 - Places text search response is valid")
    public void restaurantSearchResponseIsValid() {
        skipIfNoKey();

        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .queryParam("query", QUERY)
                .queryParam("type",  "restaurant")
                .queryParam("key",   config.getPlacesApiKey())
                .get(TEXT_SEARCH);

        long ms = response.time();
        log.info("Places API response time: {}ms", ms);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.statusCode()).as("expect HTTP 200").isEqualTo(200);

        String status = response.jsonPath().getString("status");
        log.info("status: {}", status);
        soft.assertThat(status).as("status should be OK").isEqualTo("OK");

        List<Object> results = response.jsonPath().getList("results");
        soft.assertThat(results).as("results should not be empty").isNotNull().isNotEmpty();

        if (results != null && !results.isEmpty()) {
            for (int i = 0; i < results.size(); i++) {
                String name = response.jsonPath().getString("results[" + i + "].name");
                soft.assertThat(name).as("results[" + i + "].name should not be blank").isNotNull().isNotBlank();

                Double lat = response.jsonPath().get("results[" + i + "].geometry.location.lat");
                Double lng = response.jsonPath().get("results[" + i + "].geometry.location.lng");
                soft.assertThat(lat).as("results[" + i + "] lat should exist").isNotNull();
                soft.assertThat(lng).as("results[" + i + "] lng should exist").isNotNull();

                if (lat != null && lng != null) {
                    soft.assertThat(lat).as("lat should be in Marsa Alam region").isBetween(23.0, 27.0);
                    soft.assertThat(lng).as("lng should be in Marsa Alam region").isBetween(33.0, 37.0);
                }

                String placeId = response.jsonPath().getString("results[" + i + "].place_id");
                soft.assertThat(placeId).as("place_id should be present").isNotNull().isNotBlank();

                Object ratingObj = response.jsonPath().get("results[" + i + "].rating");
                if (ratingObj != null) {
                    double rating = ((Number) ratingObj).doubleValue();
                    soft.assertThat(rating).as("rating should be 1.0-5.0").isBetween(1.0, 5.0);
                }

                List<String> types = response.jsonPath().getList("results[" + i + "].types");
                if (types != null) {
                    boolean isFood = types.stream().anyMatch(t ->
                            t.contains("restaurant") || t.contains("food") ||
                            t.contains("meal_takeaway") || t.contains("cafe") || t.contains("bar"));
                    soft.assertThat(isFood).as("results[" + i + "] should have a food-related type").isTrue();
                }
            }
        }

        soft.assertThat(ms).as("should respond in under 4 seconds").isLessThan(4000L);
        soft.assertAll();
    }

    @Test(description = "TC_API_RS_009 - nearby search by prominence returns higher-rated places first", priority = 2)
    public void nearbySearchOrderedByRating() {
        skipIfNoKey();

        // Marsa Alam coords: 25.0671, 34.8923
        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .queryParam("location", "25.0671,34.8923")
                .queryParam("radius",   10000)
                .queryParam("type",     "restaurant")
                .queryParam("rankby",   "prominence")
                .queryParam("key",      config.getPlacesApiKey())
                .get(NEARBY_SEARCH);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.statusCode()).isEqualTo(200);

        List<Object> results = response.jsonPath().getList("results");
        if (results != null && results.size() >= 2) {
            Double first  = toDouble(response.jsonPath().get("results[0].rating"));
            Double second = toDouble(response.jsonPath().get("results[1].rating"));
            if (first != null && second != null) {
                log.info("First rating: {} | Second rating: {}", first, second);
                soft.assertThat(first)
                    .as("first result rating should be >= second when sorted by prominence")
                    .isGreaterThanOrEqualTo(second);
            }
        }
        soft.assertAll();
    }

    @Test(description = "TC_API_RS_011 - invalid key returns REQUEST_DENIED", priority = 3)
    public void invalidKeyReturnsErrorStatus() {
        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .queryParam("query", QUERY)
                .queryParam("key",   "INVALID_KEY_XXXXXXXXXXXXXXXX")
                .get(TEXT_SEARCH);

        String status = response.jsonPath().getString("status");
        log.info("Status with bad key: {}", status);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(status)
            .as("should get REQUEST_DENIED or INVALID_REQUEST with a bad key")
            .isIn("REQUEST_DENIED", "INVALID_REQUEST");
        soft.assertAll();
    }

    private void skipIfNoKey() {
        if (config.getPlacesApiKey().isEmpty()) {
            throw new SkipException("GOOGLE_PLACES_KEY not set — skipping. Enable Places API in GCP and export the key.");
        }
    }

    private Double toDouble(Object obj) {
        return obj == null ? null : ((Number) obj).doubleValue();
    }
}
