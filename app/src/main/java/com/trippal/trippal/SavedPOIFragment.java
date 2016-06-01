package com.trippal.trippal;

import android.app.Fragment;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPlaces = new MyPlace(getActivity());
        //  myPlaces.savePlace(null);

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

        Button button = (Button) myView.findViewById(R.id.clear_all_poi);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPlaces.clearPlaces();

            }
        });


        return myView;
    }


}
