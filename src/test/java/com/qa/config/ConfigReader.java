package com.qa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton config loader. Reads config.properties on first use.
 * JVM system properties take precedence over the file, so -Dbrowser=firefox
 * always wins. If a value looks like ${ENV_VAR}, we resolve it from the environment.
 */
public final class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final ConfigReader INSTANCE = new ConfigReader();
    private final Properties props = new Properties();

    private ConfigReader() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) throw new IllegalStateException("config.properties not found on classpath");
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    public static ConfigReader getInstance() {
        return INSTANCE;
    }

    public String get(String key) {
        String value = System.getProperty(key, props.getProperty(key, ""));
        if (value.startsWith("${") && value.endsWith("}")) {
            String envKey = value.substring(2, value.length() - 1);
            String envVal = System.getenv(envKey);
            if (envVal == null) {
                log.warn("Env var '{}' not set (needed for config key '{}')", envKey, key);
                return "";
            }
            return envVal;
        }
        return value;
    }

    public int getInt(String key)         { return Integer.parseInt(get(key)); }
    public boolean getBoolean(String key) { return Boolean.parseBoolean(get(key)); }

    public String getBrowser()            { return get("browser"); }
    public boolean isHeadless()           { return getBoolean("headless"); }
    public String getGoogleBaseUrl()      { return get("google.base.url"); }
    public String getGoogleFlightsUrl()   { return get("google.flights.url"); }
    public String getOriginCity()         { return get("origin.city"); }
    public String getOriginIata()         { return get("origin.iata"); }
    public String getDestinationCity()    { return get("destination.city"); }
    public String getDestinationIata()    { return get("destination.iata"); }
    public String getWeatherQuery()       { return get("weather.query"); }
    public String getRestaurantQuery()    { return get("restaurant.query"); }
    public int getImplicitWait()          { return getInt("implicit.wait"); }
    public int getExplicitWait()          { return getInt("explicit.wait"); }
    public int getPageLoadTimeout()       { return getInt("page.load.timeout"); }
    public boolean isScreenshotOnFail()   { return getBoolean("screenshot.on.failure"); }
    public String getScreenshotDir()      { return get("screenshot.dir"); }
    public String getReportDir()          { return get("report.dir"); }
    public String getReportTitle()        { return get("report.title"); }
    public String getOwmBaseUrl()         { return get("owm.base.url"); }
    public String getOwmApiKey()          { return get("owm.api.key"); }
    public String getPlacesBaseUrl()      { return get("places.base.url"); }
    public String getPlacesApiKey()       { return get("places.api.key"); }
    public String getAmadeusBaseUrl()     { return get("amadeus.base.url"); }
    public String getAmadeusClientId()    { return get("amadeus.client.id"); }
    public String getAmadeusClientSecret(){ return get("amadeus.client.secret"); }
}
