package com.trippal.trippal;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by layla on 5/22/2016.
 */
public class GmapFragment extends Fragment {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    GoogleMap mMap;

    private static final double
            CSULA_LAT = 34.065207,
            CSULA_LNG = -118.170125,
            LASVEGAS_LAT = 36.126750,
            LASVEGAS_LNG = -115.165718;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;

        if (servicesOK()){
            view = inflater.inflate(R.layout.fragment_maps, container, false);
            if (initMap()){
                Toast.makeText(getActivity(), "Ready to Map", Toast.LENGTH_SHORT).show();
                gotoLocation(CSULA_LAT, CSULA_LNG, 13);
            }else{
                Toast.makeText(getActivity(), "Map not connected", Toast.LENGTH_SHORT).show();
            }

        }else{
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


    private boolean initMap() {
        if (mMap == null) {
            MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }
        return (mMap != null);
    }


    // geo location
//    public void geoLocate(View v)throws IOException {
////        hideSoftKeyboard(v);
//
//        TextView tv = (TextView) getActivity().findViewById(R.id.map_editText_location);
//        String searchString = tv.getText().toString();
//
//        Geocoder gc = new Geocoder(this);
//
//        List<Address> list = gc.getFromLocationName(searchString, 1);
//
//        if (list.size() > 0){
//            Address address = list.get(0);
//            String locality = address.getLocality();
//            Toast.makeText(this, "Found: " + locality, Toast.LENGTH_SHORT).show();
//
//            double lat = address.getLatitude();
//            double lng = address.getLongitude();
//            gotoLocation(lat, lng, 15);
//        }
//    }
//
//    // hides softkey
//    private void hideSoftKeyboard(View v) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//    }
//
    // moves the camera location
    private void gotoLocation(double lat, double lng, int zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

}
