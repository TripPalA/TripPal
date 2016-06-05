package com.trippal.trippal;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by samskim on 6/3/16.
 */
public class FetchDirectionsTask extends AsyncTask<String, Void, List<PolylineOptions>> {

    private final String LOG_TAG = FetchDirectionsTask.class.getSimpleName();
    private final Context mContext;
    private GoogleMap mMap;

    public FetchDirectionsTask(Context context, GoogleMap mMap){
        mContext = context;
        this.mMap = mMap;
    }

    @Override
    protected List<PolylineOptions> doInBackground(String... params) {

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

    private ArrayList<PolylineOptions> getDirectionDatafromJson(String directionsJsonStr) throws JSONException {

        JSONObject responseObj = new JSONObject(directionsJsonStr);
        JSONObject routes = responseObj.getJSONArray("routes").getJSONObject(0);
        JSONObject legs = routes.getJSONArray("legs").getJSONObject(0);
        JSONArray steps = legs.getJSONArray("steps");


        ArrayList<PolylineOptions> polylines = new ArrayList<>();

        for (int i = 0; i < steps.length(); i++){
            JSONObject step = steps.getJSONObject(i);
            JSONObject start_loc_obj = step.getJSONObject("start_location");
            JSONObject end_loc_obj = step.getJSONObject("end_location");
            LatLng start_loc = new LatLng(start_loc_obj.getDouble("lat"), start_loc_obj.getDouble("lng"));
            LatLng end_loc = new LatLng(end_loc_obj.getDouble("lat"), end_loc_obj.getDouble("lng"));
            PolylineOptions lineOption = new PolylineOptions()
                    .color(Color.CYAN)
                    .add(start_loc)
                    .add(end_loc);

            polylines.add(lineOption);
        }


        return polylines;
    }

    @Override
    protected void onPostExecute(List<PolylineOptions> lineOptions) {
        if (lineOptions != null){

            for (PolylineOptions option: lineOptions){
                mMap.addPolyline(option);
            }

        }

    }

}