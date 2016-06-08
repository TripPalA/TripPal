package com.trippal.trippal;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.ArrayList;
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
    private Marker currentPosMarker;
    private GoogleApiClient mLocationClient;
    private LocationListener mListener;
    private boolean findingPlace = false;
    private boolean init = false;
    List<Polyline> lines;
    List<Marker> markers;
    private Location lastCheckedPoint;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;

        setHasOptionsMenu(true);

        if (servicesOK()) {
            view = inflater.inflate(R.layout.fragment_maps, container, false);

            // set buttons on listener
            setButtonListners(view);

            // initialize map
            initMap();
            lines = new ArrayList<>();
            markers = new ArrayList<>();

            // set location client for listening to map changes
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
                showCurrentLocation();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setButtonListners(View view) {
        Button origin_search_button = (Button) view.findViewById(R.id.origin_search_button);
        origin_search_button.setOnClickListener(this);

        Button dest_search_button = (Button) view.findViewById(R.id.dest_search_button);
        dest_search_button.setOnClickListener(this);

        Button go_button = (Button) view.findViewById(R.id.map_go_button);
        go_button.setOnClickListener(this);
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

        TextView tv = null;
        if (v.getId() == R.id.origin_search_button) {
            tv = (TextView) getActivity().findViewById(R.id.map_origin_et);
        } else if (v.getId() == R.id.dest_search_button) {
            tv = (TextView) getActivity().findViewById(R.id.map_dest_et);
        }

        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(getActivity());

        // search place
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address address = list.get(0);
            String locality = address.getLocality();
            Toast.makeText(getActivity(), "Found: " + locality, Toast.LENGTH_SHORT).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();

            addMarker(v, address, lat, lng);

            gotoLocation(lat, lng, 15);
        }

    }

    private void addMarker(View v, Address add, double lat, double lng) {
        String address = add.getFeatureName() + " " + add.getThoroughfare() + ", " +
                add.getLocality() + ", " + add.getAdminArea() + " " +
                add.getPostalCode();

        MarkerOptions options = new MarkerOptions()
                .title(add.getFeatureName())
                .position(new LatLng(lat, lng))
                .snippet(address)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                    .icon(BitmapDescriptorFactory.fromResource(R.))

        if (v == null) {
            if (originMarker == null){
                originMarker = mMap.addMarker(options);
            }else if (destMarker == null){
                destMarker = mMap.addMarker(options);
            }else{
                removeEverything();
                originMarker = mMap.addMarker(options);
            }
        } else if (v.getId() == R.id.origin_search_button) {
            if (originMarker != null) {
                originMarker.remove();
            }
            originMarker = mMap.addMarker(options);
        } else if (v.getId() == R.id.dest_search_button) {
            if (destMarker != null) {
                destMarker.remove();
            }
            destMarker = mMap.addMarker(options);
        }

    }

    private void findDirectionAndGo(View view) {
        removeLines();
        FetchDirectionsTask dirTask = new FetchDirectionsTask(getActivity(), mMap, new FetchDirectionsTask.AsyncResponse() {
            @Override
            public void processFinish(List<Polyline> result) {
                lines = result;
            }
        });

        LatLng originPos = originMarker.getPosition();
        LatLng destPos = destMarker.getPosition();
        String origin = originPos.latitude + "," + originPos.longitude;
        String dest = destPos.latitude + "," + destPos.longitude;
        dirTask.execute(origin, dest);


        // get camera update and animate the camera
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(originPos, 15);
        mMap.animateCamera(update);
        findingPlace = true;
    }

    private void removeEverything() {
        originMarker.remove();
        originMarker = null;
        destMarker.remove();
        destMarker = null;
    }

    private void removeLines(){
        if (lines != null && !lines.isEmpty()){
            for (Polyline line: lines) line.remove();
        }
    }

    private void removeMarkers(){
        if (markers != null && !markers.isEmpty()){
            for (Marker marker: markers) marker.remove();
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
            case R.id.origin_search_button:
                try {
                    geoLocate(view);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.dest_search_button:
                try {
                    geoLocate(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.map_go_button:
                findDirectionAndGo(view);
                break;

            default:
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                    ImageView imageView = (ImageView) v.findViewById(R.id.info_image_iv);
                    TextView tvTitle= (TextView) v.findViewById(R.id.info_title_tv);
                    TextView tvAddress = (TextView) v.findViewById(R.id.info_address_tv);
                    TextView tvRating = (TextView) v.findViewById(R.id.info_rating_tv);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.info_snipppet_tv);

                    LatLng latLng = marker.getPosition();
                    tvTitle.setText(marker.getTitle());

                    String snippets[] = marker.getSnippet().split("%%");

                    tvAddress.setText(snippets[0]);

                    if (snippets.length > 1){
                        tvSnippet.setText(snippets[1]);
                        if (snippets.length > 2 && snippets[2] != null && !snippets[2].isEmpty()){
//                            new DownloadImageTask(imageView).execute(snippets[2]);
                        }
                    }

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
                    addMarker(null, address, latLng.latitude, latLng.longitude);
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

    private void showCurrentLocation() {
        Utility.checkPermission(getActivity());
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getActivity(), "Ready to Map", Toast.LENGTH_SHORT).show();

        if (!init) {
            init = true;
            showCurrentLocation();
        }

        mListener = new LocationListener() {

            // tell what to do to listener
            @Override
            public void onLocationChanged(Location location) {

                if (findingPlace) {
                    Toast.makeText(getActivity(),
                            "Location changed: " + location.getLatitude() + ", " +
                                    location.getLongitude(), Toast.LENGTH_SHORT).show();

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                    changeCurrentPosMarker(latLng);

                    mMap.animateCamera(update);

                    // fetch for places only if moved distance is greater than 3200 meters (2 miles)
                    if (lastCheckedPoint == null || location.distanceTo(lastCheckedPoint) > 3200){

                        fetchPlaces(latLng);
                        Toast.makeText(getActivity(), "Fetching places", Toast.LENGTH_SHORT);
                        lastCheckedPoint = location;

                    }

                }
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // updates the map every 5 seconds, and must move more than 20 meters
        request.setInterval(5000);
//        request.setSmallestDisplacement(20);

        Utility.checkPermission(getActivity());
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, request, mListener
        );
    }

    public void fetchPlaces(LatLng latLng){
        removeMarkers();
        FetchPlaceTask placeTask = new FetchPlaceTask(getActivity(), mMap, new FetchPlaceTask.AsyncResponse(){
            @Override
            public void processFinish(List<Marker> places) {
                markers = places;
            }
        });
        placeTask.execute(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
    }

    public void changeCurrentPosMarker(LatLng latLng){
        // add marker
        if (currentPosMarker != null){
            currentPosMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_image_lens))
                .position(latLng);
        currentPosMarker = mMap.addMarker(options);
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
