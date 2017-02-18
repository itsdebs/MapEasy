package com.vagabond.mapeasy.maphandler.helper;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Debanjan on 8/1/16.
 */
class JsonParser {
    private static final String TAG = "Map Handler json parser";
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    // constructor
    public JsonParser() {
    }
    public String getJSONFromUrl(String urlStr) {

        // Making HTTP request
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();


        try {


            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return jsonResults.toString();
        } catch (IOException e) {
            return jsonResults.toString();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();

    }
}
