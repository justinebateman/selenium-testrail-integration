package testrailtests;

import business.Common;
import business.Screenshot;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.annotations.*;
import testrail.TestRail;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ScreenshotTests
{
    // selenium driver
    public WebDriver driver;
    public static String baseUrl = "https://www.google.co.uk/";
    public String nodeUrl = "http://localhost:4444/wd/hub";
    public static String browserName;

    final public static By txtSearch = By.xpath(".//*[@id='lst-ib']");

    @Test
    @TestRail(testCaseId = {8615})
    public void testScreenshot(Method method, ITestContext testContext) throws Exception
    {
        StringBuilder testComment = new StringBuilder();
        final String expectedUrl = "https://www.google.co.uk/#q=TestNG";

        //take screenshot
        String testName = this.getClass().getCanonicalName().replace(".", "_") + "_" + method.getName();
        String screenshotPath = Screenshot.takeScreenshot(driver, browserName, testName);
        testComment.append("\n\n![Screenshot](" + screenshotPath + ")");

        //search
        driver.findElement(txtSearch).sendKeys("TestNG");
        driver.findElement(txtSearch).sendKeys(Keys.ENTER);
        Thread.sleep(1500);
        Common.checkURL(driver, expectedUrl);

        //take screenshot
        String secondScreenshotPath = Screenshot.takeScreenshot(driver, browserName, testName);
        testComment.append("\n\n![Screenshot](" + secondScreenshotPath + ")");

        //save to Test Rail
        testContext.setAttribute("Comment", testComment);
    }

    @Test
    @TestRail(testCaseId = {8615})
    public void testNoScreenshot() throws Exception
    {
        final String expectedUrl = "https://www.google.co.uk/#q=TestNG1";

        //search
        driver.findElement(txtSearch).sendKeys("TestNG");
        driver.findElement(txtSearch).sendKeys(Keys.ENTER);
        Thread.sleep(1500);
        Common.checkURL(driver, expectedUrl);
    }

    //endregion
    @Parameters({"browserName", "platform"})
    @BeforeMethod
    public void setUp(@Optional("firefox") String pBrowserName, @Optional("WINDOWS") String pPlatform) throws Exception
    {
        browserName = pBrowserName;
        DesiredCapabilities capability = null;

        // set browser
        if (pBrowserName.equals("firefox"))
            capability = DesiredCapabilities.firefox();
        else if (pBrowserName.equals("chrome"))
            capability = DesiredCapabilities.chrome();
        else if (pBrowserName.equals("iexplore"))
            capability = DesiredCapabilities.internetExplorer();
        else if (pBrowserName.equals("opera"))
            capability = DesiredCapabilities.operaBlink();

        // set platform
        if (pPlatform.equals("XP"))
            capability.setPlatform(Platform.XP);
        else if (pPlatform.equals("WINDOWS"))
            capability.setPlatform(Platform.WINDOWS);

        // start driver
        driver = new RemoteWebDriver(new URL(nodeUrl), capability);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        // open URL
        driver.get(baseUrl);

        // check URL
        Common.checkURL(driver, baseUrl);
    }

    @AfterMethod(alwaysRun = true)
    public void teardown()
    {
        // close browser
        driver.quit();
    }
}
