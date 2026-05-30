package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * API Test Suite: Amadeus Flight Offers API
 * Endpoint: GET /v2/shopping/flight-offers
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Test Cases:
 *   TC_API_FL_001  Auth token is obtained successfully (HTTP 200)
 *   TC_API_FL_002  Flight-offers response HTTP 200 for CAI→RMF
 *   TC_API_FL_003  Response contains 'data' array with at least one offer
 *   TC_API_FL_004  Each offer has an 'id' field
 *   TC_API_FL_005  Origin IATA code in itinerary matches 'CAI'
 *   TC_API_FL_006  Destination IATA code in itinerary matches 'RMF'
 *   TC_API_FL_007  Each offer has a 'price' object with 'total' field
 *   TC_API_FL_008  'total' price values are numeric strings
 *   TC_API_FL_009  Response time is under 5000 ms
 *   TC_API_FL_010  Response body includes 'dictionaries' meta section
 */
public class FlightApiTest extends BaseApiTest {

    private static final String TOKEN_URL  = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private static final String OFFERS_PATH = "/shopping/flight-offers";

    private String accessToken;

    // ── TC_API_FL_001 ──────────────────────────────────────────────────────

    @BeforeClass
    @Override
    public void setUpApi() {
        super.setUpApi();

        String clientId     = config.getAmadeusClientId();
        String clientSecret = config.getAmadeusClientSecret();

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            log.warn("Amadeus credentials not configured – API tests will be skipped");
            accessToken = null;
            return;
        }

        Response tokenResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", clientId)
                .formParam("client_secret", clientSecret)
                .post(TOKEN_URL);

        tokenResponse.then()
                .statusCode(200)
                .body("access_token", notNullValue());

        accessToken = tokenResponse.jsonPath().getString("access_token");
        log.info("TC_API_FL_001 PASS – Amadeus auth token obtained");
    }

    // ── TC_API_FL_002 ──────────────────────────────────────────────────────

    @Test(description = "TC_API_FL_002 – Flight offers endpoint returns HTTP 200",
          priority = 1)
    public void flightOffersReturns200() {
        assumeCredentials();

        String departureDate = LocalDate.now().plusDays(30)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        given().spec(requestSpec)
               .baseUri(config.getAmadeusBaseUrl())
               .header("Authorization", "Bearer " + accessToken)
               .queryParam("originLocationCode",      config.getOriginIata())
               .queryParam("destinationLocationCode", config.getDestinationIata())
               .queryParam("departureDate",           departureDate)
               .queryParam("adults",                  1)
               .queryParam("max",                     5)
               .get(OFFERS_PATH)
               .then()
               .statusCode(200);

        log.info("TC_API_FL_002 PASS");
    }

    // ── TC_API_FL_003 – TC_API_FL_010 ─────────────────────────────────────

    @Test(description = "TC_API_FL_003-010 – Flight offers payload is valid and well-formed",
          priority = 2)
    public void flightOffersPayloadIsValid() {
        assumeCredentials();

        String departureDate = LocalDate.now().plusDays(30)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        Response response = given().spec(requestSpec)
                .baseUri(config.getAmadeusBaseUrl())
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("originLocationCode",      config.getOriginIata())
                .queryParam("destinationLocationCode", config.getDestinationIata())
                .queryParam("departureDate",           departureDate)
                .queryParam("adults",                  1)
                .queryParam("max",                     5)
                .get(OFFERS_PATH);

        long responseTime = response.time();
        log.info("Response time: {} ms", responseTime);

        SoftAssertions soft = new SoftAssertions();

        // TC_API_FL_003 – data array exists with at least one offer
        List<Object> data = response.jsonPath().getList("data");
        soft.assertThat(data)
            .as("TC_API_FL_003: 'data' array must exist and contain at least one flight offer")
            .isNotNull()
            .isNotEmpty();

        if (data != null && !data.isEmpty()) {

            // TC_API_FL_004 – each offer has an id
            List<String> ids = response.jsonPath().getList("data.id");
            soft.assertThat(ids)
                .as("TC_API_FL_004: every offer must have an 'id' field")
                .doesNotContainNull();

            // TC_API_FL_005 – origin IATA
            String firstOrigin = response.jsonPath()
                    .getString("data[0].itineraries[0].segments[0].departure.iataCode");
            soft.assertThat(firstOrigin)
                .as("TC_API_FL_005: first segment departure IATA should be CAI")
                .isEqualToIgnoringCase(config.getOriginIata());

            // TC_API_FL_006 – destination IATA
            List<Object> segments = response.jsonPath()
                    .getList("data[0].itineraries[0].segments");
            String lastArrival = response.jsonPath().getString(
                    "data[0].itineraries[0].segments[" + (segments.size() - 1) + "].arrival.iataCode");
            soft.assertThat(lastArrival)
                .as("TC_API_FL_006: final segment arrival IATA should be RMF")
                .isEqualToIgnoringCase(config.getDestinationIata());

            // TC_API_FL_007 – price object with total
            String total = response.jsonPath().getString("data[0].price.total");
            soft.assertThat(total)
                .as("TC_API_FL_007: price.total must be present")
                .isNotNull()
                .isNotBlank();

            // TC_API_FL_008 – total is numeric
            if (total != null) {
                soft.assertThatCode(() -> Double.parseDouble(total))
                    .as("TC_API_FL_008: price.total must be a numeric string")
                    .doesNotThrowAnyException();
            }
        }

        // TC_API_FL_009 – response time < 5 000 ms
        soft.assertThat(responseTime)
            .as("TC_API_FL_009: API response time must be under 5000 ms")
            .isLessThan(5000L);

        // TC_API_FL_010 – dictionaries section present
        Object dictionaries = response.jsonPath().get("dictionaries");
        soft.assertThat(dictionaries)
            .as("TC_API_FL_010: 'dictionaries' meta section must be present")
            .isNotNull();

        soft.assertAll();
    }

    // ── helper ────────────────────────────────────────────────────────────

    private void assumeCredentials() {
        if (accessToken == null) {
            throw new org.testng.SkipException(
                "Amadeus credentials not configured – test skipped. " +
                "Set AMADEUS_CLIENT_ID and AMADEUS_CLIENT_SECRET env vars.");
        }
    }
}
