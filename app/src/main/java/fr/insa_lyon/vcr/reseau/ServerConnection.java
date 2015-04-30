package fr.insa_lyon.vcr.reseau;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by julien on 29/04/15.
 */
public class ServerConnection {

    // Response from the HTTP Request
    static InputStream httpResponseStream = null;
    // JSON Response String to create JSON Object
    static String jsonString = "";
    private String mUrl;
    private List<NameValuePair> mParams;


    public ServerConnection() {
    }

    public ServerConnection(String url, List<NameValuePair> params) {
        mUrl = url;
        mParams = params;
    }


    // Method to issue HTTP request, parse JSON result and return JSON Object
    public JSONArray makeHttpGet() {
        try {
            // client http default
            DefaultHttpClient httpClient = new DefaultHttpClient();
            // Format the parameters correctly for HTTP transmission
            String paramString = URLEncodedUtils.format(mParams, "utf-8");
            // Add parameters to url in GET format
            mUrl += "?" + paramString;
            // Execute the request
            HttpGet httpGet = new HttpGet(mUrl);
            // Execute the request and fetch Http response
            HttpResponse httpResponse = httpClient.execute(httpGet);
            // Extract the result from the response
            HttpEntity httpEntity = httpResponse.getEntity();
            // Open the result as an input stream for parsing
            httpResponseStream = httpEntity.getContent();
            // Catch Possible Exceptions
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // buffered reader httpResponseStream
            BufferedReader httpResponseReader = new BufferedReader(
                    new InputStreamReader(httpResponseStream, "iso-8859-1"), 8);
            // String to hold current line from httpResponseReader
            String line = null;
            // Clear jsonString
            jsonString = "";
            // While there is still more response to read
            while ((line = httpResponseReader.readLine()) != null) {
                // Add line to jsonString
                jsonString += (line + "\n");
            }
            // Close Response Stream
            httpResponseStream.close();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            // Create jsonObject from the jsonString and return it
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            // Return null if in error
            return null;
        }
    }
}
