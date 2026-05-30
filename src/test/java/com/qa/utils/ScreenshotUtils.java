package com.qa.utils;

import com.qa.config.ConfigReader;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final String DIR = ConfigReader.getInstance().getScreenshotDir();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    /**
     * Captures a screenshot and returns the absolute path of the saved file,
     * or an empty string if capturing fails.
     */
    public static String capture(WebDriver driver, String testName) {
        try {
            Path dir = Paths.get(DIR);
            Files.createDirectories(dir);
            String fileName = testName + "_" + LocalDateTime.now().format(FMT) + ".png";
            Path dest = dir.resolve(fileName);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), dest);
            log.info("Screenshot saved: {}", dest.toAbsolutePath());
            return dest.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to save screenshot: {}", e.getMessage());
            return "";
        }
    }
}
