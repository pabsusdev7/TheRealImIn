/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ingenuityapps.android.therealimin.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the imin rest api servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String IP_ADDRESS = "10.59.25.76";//10.59.25.36

    private static final String STATIC_REST_URL =
            "http://" + IP_ADDRESS + ":8080/imin-web/webresources/";

    private static final String REST_BASE_URL = STATIC_REST_URL;



    /* The format we want our API to return */
    private static final String format = "json";

    final static String QUERY_PARAM = "q";
    final static String LAT_PARAM = "lat";
    final static String LON_PARAM = "lon";


    public static URL buildUrl(String apiName, HashMap<String,String> params) {

        Uri.Builder builder = Uri.parse(REST_BASE_URL).buildUpon()
                .appendPath(apiName);

        if(params!=null)
        {
            for(String param:params.keySet())
                builder = builder
                        .appendQueryParameter(param, params.get(param));
        }

        Uri builtUri = builder.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static URL buildUrl(String apiName, HashMap<String,String> params, String extraPath) {

        Uri.Builder builder = Uri.parse(REST_BASE_URL).buildUpon()
                .appendPath(apiName)
                .appendPath(extraPath);

        if(params!=null)
        {
            for(String param:params.keySet())
                builder = builder
                        .appendQueryParameter(param, params.get(param));
        }

        Uri builtUri = builder.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static URL buildUrl(String apiName) {

        Uri.Builder builder = Uri.parse(REST_BASE_URL).buildUpon()
                .appendPath(apiName);

        Uri builtUri = builder.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }


    public static URL buildUrl(Double lat, Double lon) {
        return null;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static Boolean getPOSTResponseFromHttpUrl(URL url, String jsonParams) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            urlConnection.setRequestProperty("Accept","application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParams);

            os.flush();
            os.close();

            String response = String.valueOf(urlConnection.getResponseCode());

            Log.v(TAG,"Response code and Message: " + response + " - " + urlConnection.getResponseMessage());

            return response.equals("200") ? true : false;


        } finally {
            urlConnection.disconnect();
        }
    }
}