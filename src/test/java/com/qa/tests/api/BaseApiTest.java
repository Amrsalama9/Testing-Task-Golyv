package com.qa.tests.api;

import com.qa.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

/**
 * Base class for all API test classes.
 * Configures a shared RestAssured RequestSpecification and logging.
 */
public abstract class BaseApiTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ConfigReader config = ConfigReader.getInstance();

    protected RequestSpecification requestSpec;

    @BeforeClass(alwaysRun = true)
    public void setUpApi() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }
}
