package configurator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UIBaseClass extends BaseClass {
    public static WebDriver driver;

    public void initializeDriver() {
        // Suppress ChromeDriver warnings
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        Logger.getLogger("org.openqa.selenium.chromium").setLevel(Level.SEVERE);
        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.SEVERE);

        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
        System.out.println("Running in headless mode");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2); // disable notifications
        options.setExperimentalOption("prefs", prefs);

        // --- Initialize ChromeDriver
        driver = new ChromeDriver(options);
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null; // help GC
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

}
