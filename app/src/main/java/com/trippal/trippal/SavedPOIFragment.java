package com.trippal.trippal;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by layla on 5/8/2016.
 */
public class SavedPOIFragment extends Fragment {

    View myView;

    ArrayAdapter adapter;
    ListView listView;

    MyPlace myPlaces;
    private static final String LOG_TAG = SavedPOIFragment.class.getSimpleName();

    private void refresh() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPlaces = new MyPlace(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.saved_poi_layout, container, false);

        listView = (ListView) myView.findViewById(R.id.savedPlacesListView);
        adapter =
                new ArrayAdapter<MyPlace.SavedPlace>(getActivity(), R.layout.list_item, R.id.item_name, new ArrayList<MyPlace.SavedPlace>());
        List<Map<String, Object>> placeList = new ArrayList<Map<String, Object>>();

        for (MyPlace.SavedPlace place : myPlaces.getPlaces()) {
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("name", place.getName());
            p.put("gid", place.getGoogleId());
            p.put("address", place.getAddress());
            placeList.add(p);
        }

        listView.setAdapter(new SimpleAdapter(getActivity(), placeList, R.layout.list_item, new String[]{"name", "address"}, new int[]{R.id.item_name, R.id.item_address}));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Map<String, Object> place = (Map<String, Object>) listView.getItemAtPosition(position);
                //TODO: open map for POI using google id
                Toast.makeText(getActivity(), place.get("gid").toString(), Toast.LENGTH_LONG).show();
            }
        });

        //clear all poi button
        Button button = (Button) myView.findViewById(R.id.clear_all_poi);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                myView.refreshDrawableState();
            }

            @Override
            public void onClick(View v) {
                myPlaces.clearPlaces();
                refresh();
            }
        });

        //add new place button
        Button button_add = (Button) myView.findViewById(R.id.add_poi);
        button_add.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                myPlaces.savePlace(new Place() {
                    @Override
                    public String getId() {
                        return "ChIJLSHERe3PwoAR_jua-uyLzSA";
                    }

                    @Override
                    public List<Integer> getPlaceTypes() {
                        List<Integer> l = new ArrayList<Integer>();
                        l.add(1);
                        return l;
                    }

                    @Override
                    public CharSequence getAddress() {
                        return "5151 State University Dr., Los Angeles, CA 90032";
                    }

                    @Override
                    public Locale getLocale() {
                        return null;
                    }

                    @Override
                    public CharSequence getName() {
                        return "Cal State LA";
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
                        return 0;
                    }

                    @Override
                    public int getPriceLevel() {
                        return 0;
                    }

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
                        return true;
                    }
                });
                refresh();
            }
        });

        return myView;
    }


}
