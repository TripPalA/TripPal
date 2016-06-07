package com.trippal.trippal;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Calvin on 6/3/2016.
 */
public class FetchPlaceTask extends AsyncTask<String, Void, List<Place>> {

    public interface AsyncResponse{
        void processFinish(List<Marker> markers);
    }

    public AsyncResponse delegate = null;

    private GoogleMap mMap;
    private Activity activity;
    private final String LOG_TAG = FetchPlaceTask.class.getSimpleName();

    public FetchPlaceTask(Activity activity, GoogleMap mMap, AsyncResponse delegate) {
        this.activity = activity;
        this.mMap = mMap;
        this.delegate = delegate;
    }

    @Override
    protected List<Place> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String placesJsonString = null;

        LatLng coord;
        if (params.length == 0) {
            coord = new LatLng(34.064430, -118161303);
        } else {
            coord = new LatLng(Double.parseDouble(params[0]), Double.parseDouble(params[1]));
        }


        final String FORMAT = "json";
        final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/" + FORMAT + "?";
        final String RADIUS = "radius";
        final String CURR_LOCATION = "location";
        final String API_PARAM = "key";

        //       Log.v(LOG_TAG, Utility.getRadius(activity));

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
//                .appendQueryParameter(RADIUS, Utility.getRadius(activity))//in km
                .appendQueryParameter(RADIUS, "1600") // set radius to 1 mile

                .appendQueryParameter(CURR_LOCATION, coord.latitude + "," + coord.longitude)
                .appendQueryParameter("type", "point_of_interest")

                .appendQueryParameter(API_PARAM, Utility.getApiKey(activity))
                .build();

        Log.v(LOG_TAG, "Requesting: " + builtUri.toString());

        try {
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            placesJsonString = buffer.toString();
            Log.v(LOG_TAG, placesJsonString);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        return parseJsonPlace(placesJsonString);
    }

    private List<Place> parseJsonPlace(String jsonString) {
        List<Place> list = new ArrayList<Place>();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = (JSONArray) json.get("results");
            for (int i = 0; i < array.length(); i++) {
                final JSONObject obj = (JSONObject) array.get(i);
                list.add(new Place() {
                    @Override
                    public String getId() {
                        try {
                            return obj.get("place_id").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public List<Integer> getPlaceTypes() {

                        //todo: assign place types to filter
                        return null;
                    }

                    @Override
                    public CharSequence getAddress() {
                        try {
                            return obj.get("vicinity").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public Locale getLocale() {
                        return null;
                    }

                    @Override
                    public CharSequence getName() {
                        try {
                            return obj.get("name").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public LatLng getLatLng() {
                        try {
                            JSONObject geo = (JSONObject) obj.get("geometry");
                            JSONObject loc = (JSONObject) geo.get("location");
                            LatLng coord = new LatLng(Double.parseDouble(loc.get("lat").toString()),
                                    Double.parseDouble(loc.get("lng").toString()));
                            return coord;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public LatLngBounds getViewport() {
                        return null;
                    }

                    @Override
                    public Uri getWebsiteUri() {
                        return null;
                    }

                    @Override
                    public CharSequence getPhoneNumber() {
                        return null;
                    }

                    @Override
                    public float getRating() {
                        try {
                            return (float) obj.getDouble("rating");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return (float) 0.0;
                    }

                    @Override
                    public int getPriceLevel() {
                        return 0;
                    }

                    // use it for photo
                    @Override
                    public CharSequence getAttributions() {

                        try {
                            JSONArray photos = obj.getJSONArray("photos");
                            if (photos != null && photos.length() > 0){
                                JSONObject photoObj = photos.getJSONObject(0);
                                JSONArray html_attributions = photoObj.getJSONArray("html_attributions");
                                if (html_attributions != null && html_attributions.length() > 0){
                                    String anchortag = html_attributions.get(0).toString();
                                    Pattern p = Pattern.compile("<a href=(\\\"[^\\\"]*\\\")[^<]*</a>");
                                    Matcher m = p.matcher(anchortag);
                                    String url = null;
                                    if (m.find()) {
                                        url = m.group(1); // this variable should contain the link URL
                                        url = url.replaceAll("\"", "");

                                    }
                                    return url;
                                }else{
                                    return null;
                                }
                            }else{
                                return null;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public Place freeze() {
                        return null;
                    }

                    @Override
                    public boolean isDataValid() {
                        return false;
                    }

                });
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;

    }

    @Override
    protected void onPostExecute(List<Place> place) {
        super.onPostExecute(place);

        List<Marker> markers = new ArrayList<>();

        //todo: do something with places found
        Log.v(LOG_TAG, "places found: " + String.valueOf(place.size()));

        if (place != null) {
            for (Place p : place) {
                LatLng latLng = p.getLatLng();

                String address = p.getAddress().toString();
                String rating = "Rating: " + p.getRating();

                String imageUrl = null;
                if (p.getAttributions() != null){
                    imageUrl = p.getAttributions().toString();
                }

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(p.getName().toString())
                        .snippet(address + "%%" + rating + "%%" + imageUrl);

                // Placing a marker on the touched position
                markers.add(mMap.addMarker(markerOptions));
            }
        }

        delegate.processFinish(markers);


    }
}
