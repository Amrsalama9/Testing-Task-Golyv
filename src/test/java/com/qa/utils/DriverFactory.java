package com.qa.utils;

import com.qa.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Thread-local WebDriver factory.
 * Each test thread gets its own isolated driver instance; no static shared state.
 */
public final class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driverHolder = new ThreadLocal<>();
    private static final ConfigReader config = ConfigReader.getInstance();

    private DriverFactory() {}

    public static WebDriver getDriver() {
        if (driverHolder.get() == null) {
            driverHolder.set(createDriver());
        }
        return driverHolder.get();
    }

    public static void quitDriver() {
        WebDriver driver = driverHolder.get();
        if (driver != null) {
            driver.quit();
            driverHolder.remove();
            log.info("WebDriver quit and removed from thread-local");
        }
    }

    // ── private ────────────────────────────────────────────────────

    private static WebDriver createDriver() {
        String browser = config.getBrowser().toLowerCase().trim();
        boolean headless = config.isHeadless();
        log.info("Initialising {} driver – headless={}", browser, headless);

        WebDriver driver = switch (browser) {
            case "firefox" -> buildFirefox(headless);
            case "edge"    -> buildEdge(headless);
            default        -> buildChrome(headless);
        };

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeout()));
        driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver buildChrome(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--disable-search-engine-choice-screen");
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.addArguments("--no-sandbox");
        opts.addArguments("--disable-dev-shm-usage");
        opts.addArguments("--lang=en-US");
        opts.addArguments("--disable-notifications");
        opts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        if (headless) {
            opts.addArguments("--headless=new");
            opts.addArguments("--window-size=1920,1080");
        }
        return new ChromeDriver(opts);
    }

    private static WebDriver buildFirefox(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions opts = new FirefoxOptions();
        opts.addPreference("intl.accept_languages", "en-US");
        if (headless) opts.addArguments("-headless");
        return new FirefoxDriver(opts);
    }

    private static WebDriver buildEdge(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions opts = new EdgeOptions();
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.addArguments("--lang=en-US");
        if (headless) {
            opts.addArguments("--headless=new");
            opts.addArguments("--window-size=1920,1080");
        }
        return new EdgeDriver(opts);
    }
}
