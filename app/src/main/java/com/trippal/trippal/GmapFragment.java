package com.trippal.trippal;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by layla on 5/22/2016.
 */
public class GmapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 9001;
    GoogleMap mMap;

    private static final double
            CSULA_LAT = 34.065207,
            CSULA_LNG = -118.170125,
            LASVEGAS_LAT = 36.126750,
            LASVEGAS_LNG = -115.165718;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;

        if (servicesOK()) {
            view = inflater.inflate(R.layout.fragment_maps, container, false);
            Log.i(LOG_TAG, "Service Ok");
            initMap();

            Button search_button = (Button) view.findViewById(R.id.map_search_button);
            search_button.setOnClickListener(this);

//                Toast.makeText(getActivity(), "Map not connected", Toast.LENGTH_SHORT).show();


        } else {
            view = inflater.inflate(R.layout.content_main, container, false);
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        MapFragment fragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
//        fragment.getMapAsync(this);
    }

    // checks if map service is connected
    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, getActivity(), ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "Can't connect to mapping service", Toast.LENGTH_SHORT);
        }

        return false;
    }


    private void initMap() {
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    //     geo location
    public void geoLocate(View v) throws IOException {
        hideSoftKeyboard(v);

        TextView tv = (TextView) getActivity().findViewById(R.id.map_location_editText);
        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(getActivity());

        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address address = list.get(0);
            String locality = address.getLocality();
            Toast.makeText(getActivity(), "Found: " + locality, Toast.LENGTH_SHORT).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();
            gotoLocation(lat, lng, 15);
        }
    }

    // hides softkey
    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    // moves the camera location
    private void gotoLocation(double lat, double lng, int zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_search_button:
                try {
                    geoLocate(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gotoLocation(CSULA_LAT, CSULA_LNG, 15);
        Toast.makeText(getActivity(), "Map Ready", Toast.LENGTH_SHORT).show();
    }
}
