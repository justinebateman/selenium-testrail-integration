package business;

import imgur.Imgur;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;

public class Screenshot
{
    public static String resourcePathFolder = "C:\\Automation\\Share";


    public static String takeScreenshot(WebDriver driver, String pBrowserName, String pTestName) throws Exception
    {
        String savePath = resourcePathFolder + "\\AutomationResults\\Screenshots\\";
        String directoryPath = savePath + Common.dateString();
        String fileName;

        //create folder with todays date to save screenshots to
        Common.createFolder(directoryPath);

        //take the screenshot
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String pathBeforeCopy = scrFile.getAbsolutePath();

        // Copy screenshot to shared folder
        String filePath;
        if (Common.postResultsToTestRail)
        {
            fileName = "TESTRAIL_" + Common.dateTimeString() + "_" + pTestName + "_" + pBrowserName + ".png";
            filePath = directoryPath + "\\" + fileName;
        }
        else
        {
            fileName = Common.dateTimeString() + "_" + pTestName + "_" + pBrowserName + ".png";
            filePath = directoryPath + "\\" + fileName;
        }
        FileUtils.copyFile(scrFile, new File(filePath));

        //get the path on the FTP so we can link back to it later
        //String ftpPath = filePath.replace(resourcePathFolder, resourcePathFTP);
        //ftpPath = ftpPath.replace("\\", "/");
        //if (Common.debugInfo) System.out.println("Screenshot captured - " + filePath);

        //upload to imgur
        Imgur imgur = new Imgur();
        String imgurLink = imgur.uploadImage(filePath, Imgur.ALBUM_ID_TESTNG_REPORTS, "file");
        if (Common.debugInfo)
            System.out.println("Screenshot captured - " + filePath + "\nImgur link - " + imgurLink + "\n");
        return imgurLink;
    }
}
