package com.trippal.trippal;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLngBounds;


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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Calvin on 6/3/2016.
 */
public class FetchAutoCompleteTask extends AsyncTask<String, Void, List<Place>> {


    private Activity activity;
    private final String LOG_TAG = FetchPlaceTask.class.getSimpleName();

    public FetchAutoCompleteTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected List<Place> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String placesJsonString = null;

        if (params.length == 0) {
            return null;
        }


        final String FORMAT = "json";
        final String BASE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/" + FORMAT + "?";
        final String INPUT = "input";
        final String API_PARAM = "key";

        //       Log.v(LOG_TAG, Utility.getRadius(activity));

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(INPUT, params[0])
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
            // Log.v(LOG_TAG, placesJsonString);

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
            JSONArray array = (JSONArray) json.get("predictions");
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
                        return null;
                    }

                    @Override
                    public CharSequence getAddress() {

                        try {
                            return obj.get("description").toString();
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
                            JSONArray terms = (JSONArray) obj.get("terms");
                            JSONObject o = (JSONObject) terms.get(0);
                            /*String address = "";
                            for (int j = 0; j < terms.length(); j++) {
                                address = address + terms.get(j) + " ";
                            }*/
                            return o.get("value").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public LatLng getLatLng() {
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
                        return (float) 0.0;
                    }

                    @Override
                    public int getPriceLevel() {
                        return 0;
                    }

                    // use it for photo
                    @Override
                    public CharSequence getAttributions() {
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

    ListView listView;

    @Override
    protected void onPostExecute(List<Place> places) {
        super.onPostExecute(places);

        //EditText textView = (EditText) activity.findViewById(R.id.map_dest_et);
        //textView.setText(places.get(0).getName());

        listView = (ListView) activity.findViewById(R.id.autocomplete_list_view);

        ArrayAdapter adapter =
                new ArrayAdapter<Places>(activity, R.layout.list_item, R.id.item_name, new ArrayList<Places>());
        List<Map<String, Object>> placeList = new ArrayList<Map<String, Object>>();

        for (Place trip : places) {
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("name", trip.getName());
            p.put("address", trip.getAddress());
            p.put("id", trip.getId());
            placeList.add(p);
        }

        listView.setAdapter(new SimpleAdapter(activity, placeList, R.layout.list_item,
                new String[]{"name", "address"},
                new int[]{R.id.item_name, R.id.item_address}));

        //make listview visible
        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Map<String, Object> place = (Map<String, Object>) listView.getItemAtPosition(position);

                /*Toast.makeText(activity, place.get("id").toString(),
                        Toast.LENGTH_LONG).show();*/


                EditText textView = (EditText) activity.findViewById(R.id.map_dest_et);
                textView.setText(place.get("address").toString());

                //hide listview after selection
                listView.destroyDrawingCache();
                listView.setVisibility(View.GONE);

            }
        });


        Log.v(LOG_TAG, "places found: " + String.valueOf(places.size()));


    }
}