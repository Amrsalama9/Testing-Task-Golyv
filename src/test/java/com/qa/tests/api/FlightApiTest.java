package com.qa.tests.api;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Amadeus flight-offers API tests.
 * Tests skip automatically if AMADEUS_CLIENT_ID and AMADEUS_CLIENT_SECRET are not set.
 *
 * Endpoint: GET https://test.api.amadeus.com/v2/shopping/flight-offers
 */
public class FlightApiTest extends BaseApiTest {

    private static final String TOKEN_URL   = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private static final String OFFERS_PATH = "/shopping/flight-offers";

    private String accessToken;

    @BeforeClass
    @Override
    public void setUpApi() {
        super.setUpApi();

        String clientId     = config.getAmadeusClientId();
        String clientSecret = config.getAmadeusClientSecret();

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            log.warn("Amadeus credentials not set - API flight tests will be skipped");
            return;
        }

        Response tokenResp = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type",    "client_credentials")
                .formParam("client_id",     clientId)
                .formParam("client_secret", clientSecret)
                .post(TOKEN_URL);

        tokenResp.then().statusCode(200).body("access_token", notNullValue());
        accessToken = tokenResp.jsonPath().getString("access_token");
        log.info("Amadeus token obtained");
    }

    @Test(description = "TC_API_FL_001-002 - flight offers endpoint returns 200 for Cairo to Marsa Alam", groups = {"api"})
    public void flightOffersReturns200() {
        skipIfNoCredentials();

        given().spec(requestSpec)
               .baseUri(config.getAmadeusBaseUrl())
               .header("Authorization", "Bearer " + accessToken)
               .queryParam("originLocationCode",      config.getOriginIata())
               .queryParam("destinationLocationCode", config.getDestinationIata())
               .queryParam("departureDate",           departureDateIn30Days())
               .queryParam("adults", 1)
               .queryParam("max",    5)
               .get(OFFERS_PATH)
               .then()
               .statusCode(200);
    }

    @Test(description = "TC_API_FL_003-010 - flight offers payload is correct", groups = {"api"}, priority = 2)
    public void flightOffersPayloadIsValid() {
        skipIfNoCredentials();

        Response response = given().spec(requestSpec)
                .baseUri(config.getAmadeusBaseUrl())
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("originLocationCode",      config.getOriginIata())
                .queryParam("destinationLocationCode", config.getDestinationIata())
                .queryParam("departureDate",           departureDateIn30Days())
                .queryParam("adults", 1)
                .queryParam("max",    5)
                .get(OFFERS_PATH);

        long responseTime = response.time();
        log.info("Response time: {}ms", responseTime);

        SoftAssertions soft = new SoftAssertions();

        List<Object> data = response.jsonPath().getList("data");
        soft.assertThat(data).as("data array should have at least one offer").isNotNull().isNotEmpty();

        if (data != null && !data.isEmpty()) {
            soft.assertThat(response.jsonPath().<String>getList("data.id"))
                .as("every offer must have an id").doesNotContainNull();

            String origin = response.jsonPath().getString("data[0].itineraries[0].segments[0].departure.iataCode");
            soft.assertThat(origin).as("departure IATA should be CAI").isEqualToIgnoringCase(config.getOriginIata());

            int segCount = response.jsonPath().<List<?>>get("data[0].itineraries[0].segments").size();
            String dest  = response.jsonPath().getString("data[0].itineraries[0].segments[" + (segCount - 1) + "].arrival.iataCode");
            soft.assertThat(dest).as("final arrival IATA should be RMF").isEqualToIgnoringCase(config.getDestinationIata());

            String total = response.jsonPath().getString("data[0].price.total");
            soft.assertThat(total).as("price.total should be present").isNotNull().isNotBlank();
            if (total != null) {
                soft.assertThatCode(() -> Double.parseDouble(total))
                    .as("price.total should be numeric").doesNotThrowAnyException();
            }
        }

        soft.assertThat(responseTime).as("response should come back in under 5 seconds").isLessThan(5000L);
        soft.assertThat(response.jsonPath().get("dictionaries")).as("dictionaries section should be present").isNotNull();

        soft.assertAll();
    }

    private String departureDateIn30Days() {
        return LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void skipIfNoCredentials() {
        if (accessToken == null) {
            throw new SkipException("Amadeus credentials not set - skipping. Export AMADEUS_CLIENT_ID and AMADEUS_CLIENT_SECRET to run these.");
        }
    }
}
