package docker_test;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PerformanceTest extends BaseTest {

    @DataProvider(name = "test-data", parallel = true)
    public Object[][] createData() {
        Object[][] data = new Object[100][1];
        for (int i = 0; i < 100; i++) {
            data[i][0] = i + 1;
        }
        return data;
    }

    @Test(dataProvider = "test-data")
    @Description("Performance test execution")
    public void performanceTest(int executionNumber) {
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(String.format("Execution #%d", executionNumber)));
        navigateToGoogle(executionNumber);
    }

    @Step("Navigate to Google for execution #{executionNumber}")
    private void navigateToGoogle(int executionNumber) {
        driver.get("https://www.google.com");
    }

}
