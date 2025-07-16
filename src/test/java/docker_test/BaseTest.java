package docker_test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;
    protected String seleniumGridUrl = "http://localhost:4444";
    protected String jenkinsUrl = "http://localhost:80";

    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(String browser) throws MalformedURLException {
        if (System.getProperty("selenium.grid.url") != null) {
            seleniumGridUrl = System.getProperty("selenium.grid.url");
        }

        if (System.getProperty("jenkins.url") != null) {
            jenkinsUrl = System.getProperty("jenkins.url");
        }

        driver = createDriver(browser);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    private WebDriver createDriver(String browser) throws MalformedURLException {
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");

                if (isRunningInDocker()) {
                    return new RemoteWebDriver(new URL(seleniumGridUrl), chromeOptions);
                } else {
                    WebDriverManager.chromedriver().setup();
                    return new ChromeDriver(chromeOptions);
                }

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--headless");

                if (isRunningInDocker()) {
                    return new RemoteWebDriver(new URL(seleniumGridUrl), firefoxOptions);
                } else {
                    WebDriverManager.firefoxdriver().setup();
                    return new FirefoxDriver(firefoxOptions);
                }

            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
    }

    private boolean isRunningInDocker() {
        return System.getProperty("docker.environment") != null &&
                System.getProperty("docker.environment").equals("true");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
