package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * API Test Suite: Google Places – Text Search (restaurants near Marsa Alam)
 * Endpoint: GET /maps/api/place/textsearch/json
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Test Cases:
 *   TC_API_RS_001  HTTP 200 for query "restaurant near Marsa Alam"
 *   TC_API_RS_002  'status' field equals "OK"
 *   TC_API_RS_003  'results' array has at least one entry
 *   TC_API_RS_004  Each result has a non-blank 'name'
 *   TC_API_RS_005  Each result has a 'geometry.location' object with lat and lng
 *   TC_API_RS_006  Lat / lng coordinates are within Marsa Alam region
 *   TC_API_RS_007  Each result has a 'place_id' field
 *   TC_API_RS_008  Rating field (when present) is between 1.0 and 5.0
 *   TC_API_RS_009  Sorted-by-rating response returns highest-rated place first
 *   TC_API_RS_010  Response time is under 4000 ms
 *   TC_API_RS_011  Invalid key returns INVALID_KEY status
 *   TC_API_RS_012  'types' field for each result includes 'restaurant' or 'food'
 */
public class RestaurantApiTest extends BaseApiTest {

    private static final String TEXT_SEARCH_PATH = "/textsearch/json";
    private static final String QUERY            = "restaurant near Marsa Alam";

    // ── TC_API_RS_001 – TC_API_RS_008, TC_API_RS_010, TC_API_RS_012 ────────

    @Test(description = "TC_API_RS_001-008,010,012 – Places text-search response is valid",
          priority = 1)
    public void restaurantSearchResponseIsValid() {
        assumeApiKey();

        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .queryParam("query", QUERY)
                .queryParam("type",  "restaurant")
                .queryParam("key",   config.getPlacesApiKey())
                .get(TEXT_SEARCH_PATH);

        long responseTime = response.time();
        log.info("Places API response time: {} ms", responseTime);

        SoftAssertions soft = new SoftAssertions();

        // TC_API_RS_001
        soft.assertThat(response.statusCode())
            .as("TC_API_RS_001: HTTP 200 expected")
            .isEqualTo(200);

        // TC_API_RS_002
        String status = response.jsonPath().getString("status");
        log.info("Places API status: {}", status);
        soft.assertThat(status)
            .as("TC_API_RS_002: 'status' must equal OK")
            .isEqualTo("OK");

        // TC_API_RS_003
        List<Object> results = response.jsonPath().getList("results");
        soft.assertThat(results)
            .as("TC_API_RS_003: 'results' array must have at least one entry")
            .isNotNull()
            .isNotEmpty();

        if (results != null && !results.isEmpty()) {
            int count = results.size();
            log.info("Restaurant results count: {}", count);

            for (int i = 0; i < count; i++) {

                // TC_API_RS_004 – name
                String name = response.jsonPath().getString("results[" + i + "].name");
                soft.assertThat(name)
                    .as("TC_API_RS_004: results[" + i + "].name must not be blank")
                    .isNotNull()
                    .isNotBlank();

                // TC_API_RS_005 – geometry
                Double lat = response.jsonPath().get("results[" + i + "].geometry.location.lat");
                Double lng = response.jsonPath().get("results[" + i + "].geometry.location.lng");
                soft.assertThat(lat)
                    .as("TC_API_RS_005: results[" + i + "].geometry.location.lat must exist")
                    .isNotNull();
                soft.assertThat(lng)
                    .as("TC_API_RS_005: results[" + i + "].geometry.location.lng must exist")
                    .isNotNull();

                // TC_API_RS_006 – coordinates in Marsa Alam region
                if (lat != null && lng != null) {
                    soft.assertThat(lat)
                        .as("TC_API_RS_006: lat for results[" + i + "] should be ~24–26°N")
                        .isBetween(23.0, 27.0);
                    soft.assertThat(lng)
                        .as("TC_API_RS_006: lng for results[" + i + "] should be ~33–37°E")
                        .isBetween(33.0, 37.0);
                }

                // TC_API_RS_007 – place_id
                String placeId = response.jsonPath().getString("results[" + i + "].place_id");
                soft.assertThat(placeId)
                    .as("TC_API_RS_007: results[" + i + "].place_id must be present")
                    .isNotNull()
                    .isNotBlank();

                // TC_API_RS_008 – rating (optional field)
                Object ratingObj = response.jsonPath().get("results[" + i + "].rating");
                if (ratingObj != null) {
                    double rating = ((Number) ratingObj).doubleValue();
                    soft.assertThat(rating)
                        .as("TC_API_RS_008: results[" + i + "].rating must be 1.0–5.0")
                        .isBetween(1.0, 5.0);
                }

                // TC_API_RS_012 – types includes food-related category
                List<String> types = response.jsonPath().getList("results[" + i + "].types");
                if (types != null) {
                    boolean hasFoodType = types.stream().anyMatch(t ->
                            t.contains("restaurant") || t.contains("food") ||
                            t.contains("meal_takeaway") || t.contains("cafe") ||
                            t.contains("bar"));
                    soft.assertThat(hasFoodType)
                        .as("TC_API_RS_012: results[" + i + "].types should include a food-related type")
                        .isTrue();
                }
            }
        }

        // TC_API_RS_010
        soft.assertThat(responseTime)
            .as("TC_API_RS_010: response time must be under 4000 ms")
            .isLessThan(4000L);

        soft.assertAll();
    }

    // ── TC_API_RS_009 ──────────────────────────────────────────────────────

    @Test(description = "TC_API_RS_009 – rankby=rating returns highest-rated place first",
          priority = 2)
    public void restaurantResultsSortedByRating() {
        assumeApiKey();

        // Google Places rankby=prominence + sort our way; actual 'rankby=rating'
        // requires 'location' + 'radius'.  We use a nearby-search here.
        // Marsa Alam approx: lat=25.0671, lng=34.8923
        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .queryParam("location", "25.0671,34.8923")
                .queryParam("radius",   10000)
                .queryParam("type",     "restaurant")
                .queryParam("rankby",   "prominence")   // best proxy without custom sorting
                .queryParam("key",      config.getPlacesApiKey())
                .get("/nearbysearch/json");

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.statusCode()).isEqualTo(200);

        List<Object> results = response.jsonPath().getList("results");
        if (results != null && results.size() >= 2) {
            Double firstRating  = toDouble(response.jsonPath().get("results[0].rating"));
            Double secondRating = toDouble(response.jsonPath().get("results[1].rating"));

            if (firstRating != null && secondRating != null) {
                soft.assertThat(firstRating)
                    .as("TC_API_RS_009: first result rating should be >= second when sorted by prominence/rating")
                    .isGreaterThanOrEqualTo(secondRating);
                log.info("First: {} | Second: {}", firstRating, secondRating);
            }
        }
        soft.assertAll();
    }

    // ── TC_API_RS_011 ──────────────────────────────────────────────────────

    @Test(description = "TC_API_RS_011 – Invalid API key returns error status",
          priority = 3)
    public void invalidApiKeyReturnsErrorStatus() {
        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .queryParam("query", QUERY)
                .queryParam("key",   "INVALID_KEY_XXXXXXXXXXXXXXXX")
                .get(TEXT_SEARCH_PATH);

        String status = response.jsonPath().getString("status");
        log.info("Status with invalid key: {}", status);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(status)
            .as("TC_API_RS_011: invalid key should return REQUEST_DENIED or INVALID_REQUEST")
            .isIn("REQUEST_DENIED", "INVALID_REQUEST");
        soft.assertAll();
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void assumeApiKey() {
        if (config.getPlacesApiKey().isEmpty()) {
            throw new SkipException(
                "GOOGLE_PLACES_KEY env var not set – test skipped. " +
                "Enable Places API in GCP and export GOOGLE_PLACES_KEY.");
        }
    }

    private Double toDouble(Object obj) {
        if (obj == null) return null;
        return ((Number) obj).doubleValue();
    }
}
