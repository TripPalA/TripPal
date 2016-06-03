package com.trippal.trippal;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by samskim on 6/3/16.
 */
public class FetchDirectionsTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchDirectionsTask.class.getSimpleName();
    private final Context mContext;

    public FetchDirectionsTask(Context context){
        mContext = context;
    }

    @Override
    protected String[] doInBackground(String... params) {

        if (params.length == 0){
            return null;
        }

        String origin = params[0];
        String destination = params[1];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String directionsJsonStr = null;

        String format = "json";

        final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
        final String ORIGIN_PARAM = "origin";
        final String DEST_PARAM = "destination";
        final String API_PARAM = "key";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(ORIGIN_PARAM, origin)
                .appendQueryParameter(DEST_PARAM, destination)
                .appendQueryParameter(API_PARAM, "AIzaSyDDbYFy3AmllmFdsh3Gy_-4nQcXsVQW040")
                .build();

        try {
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null){
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0){
                return null;
            }

            directionsJsonStr = buffer.toString();
            Log.i(LOG_TAG, "URL\n" + builtUri.toString());
            Log.i(LOG_TAG, "JSONSTR\n" + directionsJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getDirectionDatafromJson(directionsJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  null;
    }

    private String[] getDirectionDatafromJson(String directionsJsonStr) throws JSONException {

        JSONObject responseObj = new JSONObject(directionsJsonStr);

        return null;
    }
}
