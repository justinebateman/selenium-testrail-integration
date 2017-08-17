/**
 * TestRail API binding for Java (API v2, available since TestRail 3.0)
 * <p>
 * Learn more:
 * <p>
 * http://docs.gurock.com/testrail-api2/start
 * http://docs.gurock.com/testrail-api2/accessing
 * <p>
 * Copyright Gurock Software GmbH. See license.md for details.
 */

package imgur;

public class APIException extends Exception
{
    public APIException(String message)
    {
        super(message);
    }
}
