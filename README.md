# selenium-testrail-integration
How to use your Selenium tests to add results to a TestRail run

This repo shows how you can use the [TestRail API](http://docs.gurock.com/testrail-api2/start) to post Selenium test results to your test runs. You can use this to essentially create Selenium result reports through TestRail.

There is a test listener that you can set up either in your IDE or using [TestNG xml](http://testng.org/doc/documentation-main.html#testng-xml). It has methods that run on test start, test pass or test fail. When a Selenium test passes it uses the TestRail API to pass the corresponding test case, when a Selenium test fails it fails the TestRail test.

There are also examples of how to take screenshots during your Selenium tests, upload the image to Imgur, store the direct URL and add this as an embedded image in the TestRail result comments

To use Imgur you will need to create a file called appsettings.xml in C:\Automation\Settings. On the to do list for this project is to make that path configurable. The xml file should have this structure

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><settings>
	<Imgur>
		<client_id>IMGUR_CLIENTID</client_id>
		<client_secret>IMGUR_CLIENTSECRET</client_secret>
		<token>IMGUR_TOKEN</token>
		<refresh_token>IMGUR_REFRESHTOKEN</refresh_token>
	</Imgur>		
</settings>
```