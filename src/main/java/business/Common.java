package business;

import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class Common
{
    // set to true to record test results to TestRail
   public static Boolean postResultsToTestRail = false;
    public static Boolean debugInfo = true;

    public static String dateTimeString()
    {
        String out = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return out;
    }

    public static String dateString()
    {
        String out = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return out;
    }

    public static String createFolder(String pDirectoryPath) throws Exception
    {
        Path filePath = Files.createDirectories(Paths.get(pDirectoryPath));
        return filePath.toString();
    }

    public static void checkURL(WebDriver p_driver, String p_expectedURL)
    {
        try
        {
            // check URL
            Thread.sleep(1000);
            String URL = p_driver.getCurrentUrl();
            assertThat(URL, containsString(p_expectedURL));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
