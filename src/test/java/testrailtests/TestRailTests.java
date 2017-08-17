package testrailtests;

import org.testng.Assert;
import org.testng.annotations.Test;
import testrail.TestRail;
import testrail.TestRailAPI;

public class TestRailTests
{
    @Test
    public void testAPI() throws Exception
    {
        TestRailAPI api = new TestRailAPI();
        String testRailCase = api.getCase(1).toString();
        System.out.println(testRailCase);
    }

    @Test
    @TestRail(testCaseId = {8615})
    public void passTest()
    {
        Assert.assertTrue(true);
    }

    @Test
    @TestRail(testCaseId = {8616})
    public void failTest()
    {
        Assert.assertTrue(false);
    }

    @Test
    @TestRail(testCaseId = {8617, 8618})
    public void failMultipleTests()
    {
        Assert.assertTrue(false);
    }

    @Test
    public void dontPostToTestRail()
    {
        Assert.assertTrue(true);
    }
}
