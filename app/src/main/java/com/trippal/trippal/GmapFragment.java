package com.trippal.trippal;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by layla on 5/22/2016.
 */
public class GmapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 9001;
    GoogleMap mMap;

    private static final double
            CSULA_LAT = 34.065207,
            CSULA_LNG = -118.170125,
            LASVEGAS_LAT = 36.126750,
            LASVEGAS_LNG = -115.165718;

    private Marker originMarker;
    private Marker destMarker;
    private Polyline line;
    private GoogleApiClient mLocationClient;
    private LocationListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;

        setHasOptionsMenu(true);

        if (servicesOK()) {
            view = inflater.inflate(R.layout.fragment_maps, container, false);

            Button search_button = (Button) view.findViewById(R.id.map_search_button);
            search_button.setOnClickListener(this);

            initMap();

            mLocationClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationClient.connect();


        } else {
            view = inflater.inflate(R.layout.content_main, container, false);
        }

        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.currentLocation:
                showCurrentLocation(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    private void addMarker(Address add, double lat, double lng) {
        MarkerOptions options = new MarkerOptions()
                .title(add.getLocality())
                .position(new LatLng(lat, lng))
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                    .icon(BitmapDescriptorFactory.fromResource(R.))

        String country = add.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }

        if (originMarker == null) {
            originMarker = mMap.addMarker(options);
        } else if (destMarker == null) {
            destMarker = mMap.addMarker(options);

//            drawLine();

            // try fetchdirections task

            FetchDirectionsTask dirTask = new FetchDirectionsTask(getActivity(), mMap);
            LatLng originPos = originMarker.getPosition();
            LatLng destPos = destMarker.getPosition();
            String origin = originPos.latitude + "," + originPos.longitude;
            String dest = destPos.latitude + "," + destPos.longitude;
            dirTask.execute(origin, dest);
        } else {
            removeEverything();
            originMarker = mMap.addMarker(options);
        }

    }

    private void drawLine() {
        PolylineOptions lineOptions = new PolylineOptions()
                .color(Color.CYAN)
                .add(originMarker.getPosition())
                .add(destMarker.getPosition());

        line = mMap.addPolyline(lineOptions);
    }

    private void removeEverything() {
        originMarker.remove();
        originMarker = null;
        destMarker.remove();
        destMarker = null;
        if (line != null) {
            line.remove();
            line = null;
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

        if (mMap != null) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getActivity().getLayoutInflater().inflate(R.layout.map_info_window, null);
                    TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);

                    LatLng latLng = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + latLng.latitude);
                    tvLng.setText("Longitude: " + latLng.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                @Override
                public void onMapLongClick(LatLng latLng) {
                    Geocoder gc = new Geocoder(getActivity());
                    List<Address> list = null;

                    try {
                        list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    Address address = list.get(0);
                    addMarker(address, latLng.latitude, latLng.longitude);
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String msg = marker.getTitle() + " (" +
                            marker.getPosition().latitude + ", " +
                            marker.getPosition().longitude + ") ";
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder gc = new Geocoder(getActivity());
                    List<Address> list = null;

                    LatLng latLng = marker.getPosition();

                    try {
                        list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    Address address = list.get(0);
                    marker.setTitle(address.getLocality());
                    marker.setSnippet(address.getCountryName());
                    marker.showInfoWindow();
                }
            });
        }

    }

    private void showCurrentLocation(MenuItem item) {
        checkPermission();
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(getActivity(), "Couldn't connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(update);
        }
    }


    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getActivity(), "Ready to Map", Toast.LENGTH_SHORT).show();

        mListener = new LocationListener() {

            // listens to location changes
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(getActivity(),
                        "Location changed: " + location.getLatitude() + ", " +
                                location.getLongitude(), Toast.LENGTH_SHORT).show();

                gotoLocation(location.getLatitude(), location.getLongitude(), 15);

            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // use minute for production
//        request.setInterval(60000);

        checkPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, request, mListener
        );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mLocationClient, mListener);
    }

}
