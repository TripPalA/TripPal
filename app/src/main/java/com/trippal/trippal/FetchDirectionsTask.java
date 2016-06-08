package com.trippal.trippal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

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

    public interface AsyncResponse {
        void processFinish(List<Polyline> lines);
    }

    public AsyncResponse delegate = null;

    private final String LOG_TAG = FetchDirectionsTask.class.getSimpleName();
    private Activity activity;
    private GoogleMap mMap;

    public FetchDirectionsTask(Activity activity, GoogleMap mMap, AsyncResponse delegate){
        this.activity = activity;
        this.mMap = mMap;
        this.delegate = delegate;
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
                .appendQueryParameter(API_PARAM, Utility.getApiKey(activity))
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

            JSONObject polyline_obj = step.getJSONObject("polyline");
            String encodedPointString = polyline_obj.getString("points");

            List<LatLng> list = PolyUtil.decode(encodedPointString);

            PolylineOptions lineOption = new PolylineOptions()
                    .color(Color.CYAN);

            for (LatLng latLng: list){
                lineOption.add(latLng);
            }

            polylines.add(lineOption);
        }

        return polylines;
    }

    @Override
    protected void onPostExecute(List<PolylineOptions> lineOptions) {
        List<Polyline> lines = new ArrayList<>();

        if (lineOptions != null){
            for (PolylineOptions option: lineOptions){
                lines.add(mMap.addPolyline(option));
            }

        }
        // this saves lines to MainActivity
        delegate.processFinish(lines);
    }

}
