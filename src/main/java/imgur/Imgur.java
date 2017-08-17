package imgur;

import business.Image;
import business.Settings;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class Imgur
{
    public static String BASE_URL = "https://api.imgur.com/3/";

    // TODO - make these configurable, read from appsettings.xml
    public static String ALBUM_ID_TESTNG_REPORTS = "G1yiy";
    public static String ALBUM_ID_TRACKDUCK_REPORTS = "94Ljl";
    public static String ALBUM_ID_SCREENSHOTS = "A0oaN";
    public static String ALBUM_ID_TEST = "wJeJz";

    public String getToken()
    {
        String token = Settings.read("//Imgur//token");
        return token;
    }

    public String getRefreshToken()
    {
        String refreshToken = Settings.read("//Imgur//refresh_token");
        return refreshToken;
    }

    public String getClientId()
    {
        String clientId = Settings.read("//Imgur//client_id");
        return clientId;
    }

    public String getClientSecret()
    {
        String clientSecret = Settings.read("//Imgur//client_secret");
        return clientSecret;
    }


    public String uploadImage(String pFilePath, String pAlbum, String pType)
    {
        String method = "POST";
        String uri = "upload";
        String imgstr = "";
        String title = "";
        String link = "";

        try
        {
            if (pType.equals("file"))
            {
                BufferedImage img = ImageIO.read(new File(pFilePath));
                imgstr = Image.convertImageToBase64(img, "png");
                title = pFilePath.substring(pFilePath.lastIndexOf("\\") + 1);
                title = title.substring(0, title.indexOf("."));
            }
            else if (pType.equals("url"))
            {
                imgstr = pFilePath;
                title = pFilePath.substring(pFilePath.lastIndexOf("/") + 1);
                title = title.substring(0, title.indexOf("."));
            }

            //map the parameters
            Map data = new HashMap();
            data.put("image", imgstr);
            data.put("album", pAlbum);
            data.put("type", pType);
            data.put("title", title);

            //upload image to Imgur and return response
            Object result = sendRequest(method, uri, data);

            //parse the response and return the image link
            JSONObject obj = (JSONObject) result;
            obj = (JSONObject) obj.get("data");
            link = obj.get("link").toString();
            checkCredits();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return link;
    }

    @Test
    public void checkCredits() throws Exception
    {
        String method = "GET";
        String uri = "credits";

        try
        {
            //send request to Imgur and return response
            Object result = sendRequest(method, uri, "");

            //parse the response and return the image link
            JSONObject obj = (JSONObject) result;
            obj = (JSONObject) obj;
            System.out.println("Credits Remaining = " + obj.toString());
            obj = (JSONObject) obj.get("data");
            String userCredits = obj.get("UserRemaining").toString();
            //System.out.println(userCredits);

            //if user is running out of credits, pause for one hour
            if (Integer.parseInt(userCredits) < 20)
            {
                Thread.sleep(3000000);
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public Object sendRequest(String method, String uri, Object data) throws MalformedURLException, IOException, APIException, InterruptedException
    {
        int count = 0;
        int maxTries = 3;

        Object result = null;
        int status = 0;
        String error = "";

        while (true)
        {
            try
            {
                URL url = new URL(BASE_URL + uri);

                // Create the connection object and set the required HTTP method
                // (GET/POST) and headers (content type and basic auth).
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("Content-Type", "application/json");

                //String token = requestToken();
                String token = getToken();
                conn.addRequestProperty("Authorization", "Bearer " + token);
                //conn.addRequestProperty("Authorization", "Bearer " + getToken());

                if (method == "POST")
                {
                    // Add the POST arguments, if any. We just serialize the passed
                    // data object (i.e. a dictionary) and then add it to the
                    // request body.
                    if (data != null)
                    {
                        byte[] block = JSONValue.toJSONString(data).getBytes("UTF-8");

                        conn.setDoOutput(true);
                        OutputStream ostream = conn.getOutputStream();
                        ostream.write(block);
                        ostream.flush();
                    }
                }
                // Execute the actual web request (if it wasn't already initiated
                // by getOutputStream above) and record any occurred errors (we use
                // the error stream in this case).
                status = conn.getResponseCode();

                InputStream istream = null;
                if (status != 200)
                {
                    //if the access token has expired, request a new one and save to file, the request will be re-sent with the new token
                    if (status == 403)
                    {
                        requestAccessToken();
                    }
                    else
                    {
                        istream = conn.getErrorStream();
                        if (istream == null)
                        {
                            throw new APIException("API return HTTP " + status + " (No additional error message received)");
                        }
                    }
                }
                else
                {
                    istream = conn.getInputStream();
                }

                // Read the response body, if any, and deserialize it from JSON.
                String text = "";
                if (istream != null)
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));

                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        text += line;
                        text += System.getProperty("line.separator");
                    }

                    reader.close();
                }


                if (text != "")
                {
                    result = JSONValue.parse(text);
                }
                else
                {
                    result = new JSONObject();
                }

                //System.out.println(result.toString());

                // Check for any occurred errors and add additional details to
                // the exception message, if any (e.g. the error message returned)
                if (status != 200)
                {
                    error = "No additional error message received";
                    if (result != null && result instanceof JSONObject)
                    {
                        JSONObject obj = (JSONObject) result;
                        //String customError = obj.toString();
                        if (obj.containsKey("error"))
                        {
                            error = '"' + (String) obj.get("error") + '"';
                        }
                        count++;
                    }
                    //if error from Imgur is "too many requests" wait the required amount of time before re-trying
                    if (status == 429)
                    {
                        JSONObject obj = (JSONObject) result;
                        getWaitTime(obj);
                    }
                    // if we have tried to send the request the max amount of times throw an exception
                    if (count == maxTries || count > maxTries)
                    {
                        // Assert.fail();
                        throw new APIException("API returned HTTP " + status + "(" + error + ")" + " Full response = " + result.toString());
                    }

                }

            }
            catch (APIException e)
            {
                count++;
                // if we have tried to send the request the max amount of times throw an exception
                if (count == maxTries || count > maxTries)
                {
                    //Assert.fail();
                    throw new APIException("API returned HTTP " + status + "(" + error + ")" + " Full response = " + result.toString());
                }
            }
            if (status == 200)
            {
                return result;
            }

        }
    }


    @Test
    public void requestAccessToken() throws MalformedURLException, IOException, APIException
    {
        System.out.println("Imgur access token has expired, requesting a new one");
        String resultToken = "";
        String resultRefreshToken = "";
        String uri = "https://api.imgur.com/oauth2/token";

        Map data = new HashMap();
        data.put("refresh_token", getRefreshToken());
        data.put("client_id", getClientId());
        data.put("client_secret", getClientSecret());
        data.put("grant_type", "refresh_token");

        URL url = new URL(uri);

        // Create the connection object and set the required HTTP method
        // (GET/POST) and headers (content type and basic auth).
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("Content-Type", "application/json");
        if (data != null)
        {
            byte[] block = JSONValue.toJSONString(data).getBytes("UTF-8");

            conn.setDoOutput(true);
            OutputStream ostream = conn.getOutputStream();
            ostream.write(block);
            ostream.flush();
        }

        // Execute the actual web request (if it wasn't already initiated
        // by getOutputStream above) and record any occurred errors (we use
        // the error stream in this case).
        int status = conn.getResponseCode();

        InputStream istream;
        if (status != 200)
        {
            istream = conn.getErrorStream();
            if (istream == null)
            {
                throw new APIException("API return HTTP " + status + " (No additional error message received)");
            }
        }
        else
        {
            istream = conn.getInputStream();
        }

        // Read the response body, if any, and deserialize it from JSON.
        String text = "";
        if (istream != null)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null)
            {
                text += line;
                text += System.getProperty("line.separator");
            }

            reader.close();
        }


        Object result;
        if (text != "")
        {
            result = JSONValue.parse(text);
            JSONObject obj = (JSONObject) result;
            resultToken = obj.get("access_token").toString();
            resultRefreshToken = obj.get("refresh_token").toString();

            //save the access token to the settings file so it can be read later
            Settings.write("//Imgur//token", resultToken);
            //System.out.println("access token = " + resultToken);
            //System.out.println("refresh token = " + resultRefreshToken);
            //Settings.write("//Imgur//refresh_token", resultRefreshToken);
        }
        else
        {
            result = new JSONObject();
        }

        // Check for any occurred errors and add additional details to
        // the exception message, if any (e.g. the error message returned)
        if (status != 200)
        {
            String error = "No additional error message received";
            if (result != null && result instanceof JSONObject)
            {
                JSONObject obj = (JSONObject) result;
                //String customError = obj.toString();
                if (obj.containsKey("error"))
                {
                    error = '"' + (String) obj.get("error") + '"';
                }
            }

            throw new APIException("API returned HTTP " + status + "(" + error + ")" + " Full response = " + result.toString());
        }
        //return resultToken;
    }


    public String requestToken() throws MalformedURLException, IOException, APIException
    {
        String resultToken = "";
        String resultRefreshToken = "";
        String uri = "https://api.imgur.com/oauth2/token";

        Map data = new HashMap();
        data.put("refresh_token", getRefreshToken());
        data.put("client_id", getClientId());
        data.put("client_secret", getClientSecret());
        data.put("grant_type", "refresh_token");

        URL url = new URL(uri);

        // Create the connection object and set the required HTTP method
        // (GET/POST) and headers (content type and basic auth).
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("Content-Type", "application/json");
        if (data != null)
        {
            byte[] block = JSONValue.toJSONString(data).getBytes("UTF-8");

            conn.setDoOutput(true);
            OutputStream ostream = conn.getOutputStream();
            ostream.write(block);
            ostream.flush();
        }

        // Execute the actual web request (if it wasn't already initiated
        // by getOutputStream above) and record any occurred errors (we use
        // the error stream in this case).
        int status = conn.getResponseCode();

        InputStream istream;
        if (status != 200)
        {
            istream = conn.getErrorStream();
            if (istream == null)
            {
                throw new APIException("API return HTTP " + status + " (No additional error message received)");
            }
        }
        else
        {
            istream = conn.getInputStream();
        }

        // Read the response body, if any, and deserialize it from JSON.
        String text = "";
        if (istream != null)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null)
            {
                text += line;
                text += System.getProperty("line.separator");
            }

            reader.close();
        }


        Object result;
        if (text != "")
        {
            result = JSONValue.parse(text);
            JSONObject obj = (JSONObject) result;
            resultToken = obj.get("access_token").toString();
            resultRefreshToken = obj.get("refresh_token").toString();

            Settings.write("//Imgur//token", resultToken);
            Settings.write("//Imgur//refresh_token", resultRefreshToken);
        }
        else
        {
            result = new JSONObject();
        }

        // Check for any occurred errors and add additional details to
        // the exception message, if any (e.g. the error message returned)
        if (status != 200)
        {
            String error = "No additional error message received";
            if (result != null && result instanceof JSONObject)
            {
                JSONObject obj = (JSONObject) result;
                //String customError = obj.toString();
                if (obj.containsKey("error"))
                {
                    error = '"' + (String) obj.get("error") + '"';
                }
            }

            throw new APIException("API returned HTTP " + status + "(" + error + ")" + " Full response = " + result.toString());
        }
        return resultToken;
    }

    public void getWaitTime(JSONObject obj) throws InterruptedException
    {
        //if Imgur throws 429 error "too many requests" get the error
        obj = (JSONObject) obj.get("data");
        String error = obj.get("error").toString();
        //get the number of minutes it wants us to wait
        String minutes = error.substring(error.indexOf("t ") + 1, error.indexOf(" more"));
        minutes = minutes.replace(" ", "");
        String timeStamp = new SimpleDateFormat("dd/MM/yyy HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println("Imgur reporting too many requests. Waiting for " + minutes + " minutes; Time now: " + timeStamp);

        int minuteInt = Integer.parseInt(minutes);
        long waitFor = minuteInt * 60000;
        //wait the required amount of time
        Thread.sleep(waitFor);
    }


}
