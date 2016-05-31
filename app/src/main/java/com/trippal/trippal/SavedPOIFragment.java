package com.trippal.trippal;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.trippal.trippal.data.TripContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by layla on 5/8/2016.
 */
public class SavedPOIFragment extends Fragment {

    View myView;


    MyPlace myPlaces;
    private static final String LOG_TAG = SavedPOIFragment.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPlaces = new MyPlace(getActivity());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.saved_poi_layout, container, false);

        ListView listView = (ListView) myView.findViewById(R.id.savedPlacesListView);
        ArrayAdapter adapter =
                new ArrayAdapter<String>(getActivity(), R.layout.list_item, R.id.item_text, new ArrayList<String>());
        List<MyPlace.SavedPlace> placeList = myPlaces.getPlaces();

        for (MyPlace.SavedPlace place : placeList) {
            adapter.add(place.getName() + "\n" + place.getAddress());
        }
        listView.setAdapter(adapter);

        /*listView.setOnClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String placeName = adapter.getItem(position).toString();
                Toast.makeText(getActivity(), placeName, Toast.LENGTH_LONG).show();
            }

        });*/
        //TODO: handle onclick event to open map, delete place

        return myView;
    }


}
