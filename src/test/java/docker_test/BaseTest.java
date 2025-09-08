package docker_test;

import com.codeborne.selenide.Configuration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import static com.codeborne.selenide.Selenide.closeWebDriver;

public class BaseTest {
    protected String seleniumGridUrl = System.getProperty("selenium.hub.url", "http://localhost:4444/wd/hub");

    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(String browser) {
        Configuration.browser = browser.toLowerCase();
        Configuration.remote = seleniumGridUrl;
        Configuration.timeout = 10000;
        Configuration.headless = true;
    }

    @AfterMethod
    public void tearDown() {
        closeWebDriver();
    }
}
