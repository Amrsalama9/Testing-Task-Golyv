package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Google Places API (New) tests - text search for restaurants near Marsa Alam.
 *
 * Google replaced the old GET endpoint at maps.googleapis.com/maps/api/place with
 * a new POST-based API at places.googleapis.com/v1 in 2023. This suite uses the new one.
 *
 * Text search:  POST https://places.googleapis.com/v1/places:searchText
 * Nearby search: POST https://places.googleapis.com/v1/places:searchNearby
 *
 * Auth goes in the X-Goog-Api-Key header. Field mask in X-Goog-FieldMask header.
 *
 * Tests skip if GOOGLE_PLACES_KEY is not set.
 */
public class RestaurantApiTest extends BaseApiTest {

    private static final String TEXT_SEARCH   = "/places:searchText";
    private static final String NEARBY_SEARCH = "/places:searchNearby";

    @Test(description = "TC_API_RS_001-008,010,012 - Places text search returns valid restaurant data")
    public void restaurantSearchResponseIsValid() {
        skipIfNoKey();

        String body = "{"
                + "\"textQuery\": \"restaurant near Marsa Alam\","
                + "\"includedType\": \"restaurant\","
                + "\"languageCode\": \"en\""
                + "}";

        String fieldMask = "places.id,places.displayName,places.formattedAddress,"
                         + "places.location,places.rating,places.types,places.businessStatus";

        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .header("X-Goog-Api-Key",  config.getPlacesApiKey())
                .header("X-Goog-FieldMask", fieldMask)
                .header("Content-Type",     "application/json")
                .body(body)
                .post(TEXT_SEARCH);

        long ms = response.time();
        log.info("Places API response time: {}ms", ms);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.statusCode()).as("expect HTTP 200").isEqualTo(200);

        List<Object> places = response.jsonPath().getList("places");
        soft.assertThat(places).as("places array should not be empty").isNotNull().isNotEmpty();

        if (places != null && !places.isEmpty()) {
            int count = places.size();
            log.info("Restaurant results: {}", count);

            for (int i = 0; i < count; i++) {
                String name = response.jsonPath().getString("places[" + i + "].displayName.text");
                soft.assertThat(name)
                    .as("places[" + i + "].displayName.text should not be blank")
                    .isNotNull().isNotBlank();

                Double lat = response.jsonPath().get("places[" + i + "].location.latitude");
                Double lng = response.jsonPath().get("places[" + i + "].location.longitude");
                soft.assertThat(lat).as("places[" + i + "] latitude should exist").isNotNull();
                soft.assertThat(lng).as("places[" + i + "] longitude should exist").isNotNull();

                if (lat != null && lng != null) {
                    soft.assertThat(lat).as("lat should be in Marsa Alam region").isBetween(23.0, 27.0);
                    soft.assertThat(lng).as("lng should be in Marsa Alam region").isBetween(33.0, 37.0);
                }

                String placeId = response.jsonPath().getString("places[" + i + "].id");
                soft.assertThat(placeId).as("places[" + i + "].id should be present").isNotNull().isNotBlank();

                Object ratingObj = response.jsonPath().get("places[" + i + "].rating");
                if (ratingObj != null) {
                    double rating = ((Number) ratingObj).doubleValue();
                    soft.assertThat(rating).as("rating should be between 1.0 and 5.0").isBetween(1.0, 5.0);
                }

                List<String> types = response.jsonPath().getList("places[" + i + "].types");
                if (types != null) {
                    boolean isFood = types.stream().anyMatch(t ->
                            t.contains("restaurant") || t.contains("food") ||
                            t.contains("meal_takeaway") || t.contains("cafe") || t.contains("bar"));
                    soft.assertThat(isFood)
                        .as("places[" + i + "] should have a food-related type, got: " + types)
                        .isTrue();
                }
            }
        }

        soft.assertThat(ms).as("response should come back under 4 seconds").isLessThan(4000L);

        soft.assertAll();
    }

    @Test(description = "TC_API_RS_009 - nearby search returns results ordered by rating descending", priority = 2)
    public void nearbySearchOrderedByRating() {
        skipIfNoKey();

        // Marsa Alam coordinates: 25.0671N, 34.8923E
        String body = "{"
                + "\"includedTypes\": [\"restaurant\"],"
                + "\"maxResultCount\": 10,"
                + "\"locationRestriction\": {"
                + "  \"circle\": {"
                + "    \"center\": {\"latitude\": 25.0671, \"longitude\": 34.8923},"
                + "    \"radius\": 10000.0"
                + "  }"
                + "},"
                + "\"rankPreference\": \"POPULARITY\""
                + "}";

        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .header("X-Goog-Api-Key",  config.getPlacesApiKey())
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.rating")
                .header("Content-Type",     "application/json")
                .body(body)
                .post(NEARBY_SEARCH);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.statusCode()).isEqualTo(200);

        List<Object> places = response.jsonPath().getList("places");
        if (places != null && places.size() >= 2) {
            Double first  = toDouble(response.jsonPath().get("places[0].rating"));
            Double second = toDouble(response.jsonPath().get("places[1].rating"));
            if (first != null && second != null) {
                log.info("First rating: {} | Second rating: {}", first, second);
                soft.assertThat(first)
                    .as("first result rating should be >= second when sorted by popularity")
                    .isGreaterThanOrEqualTo(second);
            }
        }
        soft.assertAll();
    }

    @Test(description = "TC_API_RS_011 - invalid API key returns 403", priority = 3)
    public void invalidKeyReturns403() {
        String body = "{\"textQuery\": \"restaurant near Marsa Alam\"}";

        Response response = given().spec(requestSpec)
                .baseUri(config.getPlacesBaseUrl())
                .header("X-Goog-Api-Key",  "INVALID_KEY_XXXXXXXXXXXXXXXX")
                .header("X-Goog-FieldMask", "places.displayName")
                .header("Content-Type",     "application/json")
                .body(body)
                .post(TEXT_SEARCH);

        log.info("Status with bad key: {}", response.statusCode());

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.statusCode())
            .as("should return 403 with an invalid API key")
            .isEqualTo(403);
        soft.assertAll();
    }

    private void skipIfNoKey() {
        if (config.getPlacesApiKey().isEmpty()) {
            throw new SkipException("GOOGLE_PLACES_KEY not set - skipping. Enable Places API in GCP and export the key.");
        }
    }

    private Double toDouble(Object obj) {
        return obj == null ? null : ((Number) obj).doubleValue();
    }
}
