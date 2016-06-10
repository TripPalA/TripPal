package com.trippal.trippal;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.places.Place;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Layla on 6/9/2016.
 */

 /* get total distance and duration of trip
         * URL contains JSON w/distance and duration given origin and destination
         * https://maps.googleapis.com/maps/api/directions/json?origin=-20.291825,57.448668&destination=-20.179724,57.613463&key=AIzaSyDDbYFy3AmllmFdsh3Gy_-4nQcXsVQW040
         */
public class FetchDurationTask extends AsyncTask<String, Void, String> {

    public interface AsyncResponse {
        void processFinish(List<Place> markers);
    }

    public AsyncResponse delegate = null;

    private final String LOG_TAG = FetchPlaceTask.class.getSimpleName();
    private Activity activity;
    private GoogleMap mMap;


    public FetchDurationTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        String origin = params[0];
        String destination = params[1];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String durationJsonStr = null;

        try {

            // Construct the URL for the query
            final String FORMAT = "json";
            final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/" + FORMAT + "?";
            final String ORIGIN_PARAM = "origin";
            final String DEST_PARAM = "destination";
//          final String API_PARAM = "key";

            // My personal API for the TripPal project (TODO: Remove this later)
            final String API_PARAM = "AIzaSyDDbYFy3AmllmFdsh3Gy_-4nQcXsVQW040";


            // Build URI using constructions
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(ORIGIN_PARAM, origin)
                    .appendQueryParameter(DEST_PARAM, destination)
                    .appendQueryParameter(API_PARAM, Utility.getApiKey(activity))
                    .build();

            //TODO: I want this: "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + dest + "&key=AIzaSyDDbYFy3AmllmFdsh3Gy_-4nQcXsVQW040";
            // Back to a (properly constructed) URL
            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "URL:) : " + url);

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do
                durationJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                durationJsonStr = null;
            }

            durationJsonStr = buffer.toString();
            Log.v(LOG_TAG, "JSON: " + durationJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR", e);
            e.printStackTrace();
        }

        try {
            return parseJsonPlace(durationJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseJsonPlace(String durationJsonStr) throws JSONException {

//        List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

//        // Get JSONObjects that are needed
//        JSONObject jsonObject = new JSONObject(durationJsonStr); // parse response into json object
//        JSONObject routeObject = jsonObject.getJSONObject("route"); // pull out the "route" object
//        JSONObject durationObject = jsonObject.getJSONObject("duration"); // pull out the "duration" object
//        String durationString = durationObject.getString("text"); //this should be the duration text value (20 hours 40 mins)

        // testing sam's version:
        JSONObject responseObj = new JSONObject(durationJsonStr);
        JSONObject routes = responseObj.getJSONArray("routes").getJSONObject(0);
        JSONObject legs = routes.getJSONArray("legs").getJSONObject(0);
        JSONObject duration = legs.getJSONObject("duration");
        String durationString = duration.getString("text");


//        for(int i=0;i<routeObject.length();i++){
//
//            HashMap<String, String> hmDuration = new HashMap<String, String>();
//            hmDuration.put("duration", durationString);
//
//            /** Adding duration object to the path */
//            path.add(hmDuration);
//        }

        return durationString;
    }

    @Override
    protected void onPostExecute(String durationString) {
        super.onPostExecute(durationString);

        TextView tv = (TextView) activity.findViewById(R.id.placeDuration_tv);
        tv.setText(durationString);
        tv.setVisibility(View.VISIBLE);
    }
}

