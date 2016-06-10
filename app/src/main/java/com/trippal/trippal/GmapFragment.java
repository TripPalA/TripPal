package com.trippal.trippal;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
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

import java.io.IOException;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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

    private Marker destMarker;
    private Location targetLoc;
    private Marker currentPosMarker;
    private GoogleApiClient mLocationClient;
    private LocationListener mListener;

    private boolean findingPlace;
    private boolean init;
    private List<Polyline> lines;
    private List<Marker> markers;
    private Location lastCheckedPoint;
    private EditText dest_et;
    private FloatingActionButton go_button;
    private FloatingActionButton next_fbutton;
    private FloatingActionButton mute_fbutton;
    private FloatingActionButton save_fbutton;
    private TextView placeInfo_tv;
    private TextView placeDuration_tv;
    private boolean mute;
    private int places_page;
    private List<Place> places;
    private MyPlace myPlace;
    private boolean animateToCurrentLocOnListen;
    private ListView autocomplete_lv;
    private int callCounter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;

        setHasOptionsMenu(true);

        if (servicesOK()) {
            rootView = inflater.inflate(R.layout.fragment_maps, container, false);

            // initialize map
            initMap();
            initMapElements(rootView);
            // set buttons on listener
            setButtonListners(rootView);

            // set location client for listening to map changes
            mLocationClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationClient.connect();

        } else {
            rootView = inflater.inflate(R.layout.content_main, container, false);
        }

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.currentLocation:
                showCurrentLocation(true);
                animateToCurrentLocOnListen = true;
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
        Utility.checkPermission(getActivity());
    }

    public static boolean autocomplete_selected = false;

    public void setButtonListners(View view) {

        // reference:
        // http://stackoverflow.com/questions/12142021/how-can-i-do-something-0-5-second-after-text-changed-in-my-edittext
        dest_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocomplete_selected = true;
            }
        });
        dest_et.addTextChangedListener(new TextWatcher() {

            private Timer timer = new Timer();
            private final int DELAY = 500; //milliseconds of delay for timer
            String placeQuery = "";


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (autocomplete_selected) {
                    placeQuery = s.toString();

                    timer.cancel();
                    timer = new Timer();

                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    FetchAutoCompleteTask autoComplete = new FetchAutoCompleteTask(getActivity());
                                    autoComplete.execute(placeQuery);
                                    //  Log.v(LOG_TAG, s.toString());
                                }
                            },
                            DELAY
                    );
                }
            }
        });


        Button dest_search_button = (Button) view.findViewById(R.id.dest_search_button);
        go_button = (FloatingActionButton) view.findViewById(R.id.map_go_button);
        next_fbutton = (FloatingActionButton) view.findViewById(R.id.map_next_fbutton);
        mute_fbutton = (FloatingActionButton) view.findViewById(R.id.map_mute_fbutton);
        save_fbutton = (FloatingActionButton) view.findViewById(R.id.map_save_fbutton);

        go_button.setOnClickListener(this);
        dest_search_button.setOnClickListener(this);
        next_fbutton.setOnClickListener(this);
        mute_fbutton.setOnClickListener(this);
        save_fbutton.setOnClickListener(this);
    }

    private void initMapElements(View view) {
        myPlace = new MyPlace(getActivity());
        findingPlace = false;
        animateToCurrentLocOnListen = true;
        init = false;
        mute = false;
        places_page = 0;
        lines = new ArrayList<>();
        markers = new ArrayList<>();
        dest_et = (EditText) view.findViewById(R.id.map_dest_et);
        placeInfo_tv = (TextView) view.findViewById(R.id.map_placeInfo_tv);
        autocomplete_lv = (ListView) view.findViewById(R.id.autocomplete_list_view);
        placeDuration_tv = (TextView) view.findViewById(R.id.placeDuration_tv);
    }

    // toggle floating action buttons
    private void toggleVisibility(boolean visible) {
        if (visible == true) {
            next_fbutton.setVisibility(View.VISIBLE);
            mute_fbutton.setVisibility(View.VISIBLE);
            save_fbutton.setVisibility(View.VISIBLE);
            placeInfo_tv.setVisibility(View.VISIBLE);
            placeDuration_tv.setVisibility(View.VISIBLE);

        } else {
            next_fbutton.setVisibility(View.INVISIBLE);
            mute_fbutton.setVisibility(View.INVISIBLE);
            save_fbutton.setVisibility(View.INVISIBLE);
            placeInfo_tv.setVisibility(View.GONE);
            placeDuration_tv.setVisibility(View.GONE);
        }
    }

    //     geo location
    public void geoLocate(View v) throws IOException {
        hideSoftKeyboard(v);

        String searchString = dest_et.getText().toString();

        if (searchString != null) {
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

        if (destMarker != null) {
            destMarker.remove();
        }
        destMarker = mMap.addMarker(options);

        // if came from longButtonClick, update editText
        if (v == null)
            dest_et.setText(address);

    }


    private void findDirectionAndGo(View view) {
        removeLines();
        autocomplete_lv.setVisibility(View.GONE);
        placeInfo_tv.setText("fetching places info..");
        Utility.tts(getActivity(), "Let's go");
        FetchDirectionsTask dirTask = new FetchDirectionsTask(getActivity(), mMap, new FetchDirectionsTask.AsyncResponse() {
            @Override
            public void processFinish(List<Polyline> result) {
                lines = result;
            }
        });

        // save current location and animate the camera
        Location currentLoc = showCurrentLocation(true);
        if(destMarker != null) {
            LatLng originPos = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
            LatLng destPos = destMarker.getPosition();

            targetLoc = Utility.convertLatLngToLocation(destPos);

            String origin = originPos.latitude + "," + originPos.longitude;
            String dest = destPos.latitude + "," + destPos.longitude;
            dirTask.execute(origin, dest);

            FetchDurationTask durationTask = new FetchDurationTask(getActivity());
            durationTask.execute(origin, dest);
        }
    }

    private void removeEverything() {
        if (targetLoc != null) {
            destMarker.remove();
            destMarker = null;
            targetLoc = null;
            dest_et.setText("");
            removeLines();
            removeMarkers();
        }
    }

    private void removeLines() {
        if (lines != null && !lines.isEmpty()) {
            for (Polyline line : lines) {
                line.remove();
            }
            lines = null;
        }
    }

    private void removeMarkers() {
        if (markers != null && !markers.isEmpty()) {
            for (Marker marker : markers) marker.remove();
        }
        markers = null;
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
            case R.id.dest_search_button:
                try {
                    geoLocate(view);
                    autocomplete_lv.setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            // on go button, toggle go button icon and invisibility of buttons
            case R.id.map_go_button:
                if (destMarker != null){
                    if (!findingPlace) {
                        findDirectionAndGo(view);
                        findingPlace = true;
                        go_button.setImageResource(R.drawable.ic_navigation_cancel);
                        toggleVisibility(true);
                    } else {
                        findingPlace = true;
//                        initMapElements(view);
                        go_button.setImageResource(R.drawable.ic_maps_directions_car);
                        removeEverything();
                        toggleVisibility(false);
                    }
                    break;
                }else{
                    Toast.makeText(getActivity(), "Destination is empty", Toast.LENGTH_SHORT).show();
                }

            // get next page in places list
            case R.id.map_next_fbutton:
                if (places != null && places.size() > 0) {
                    Place place = places.get(places_page++ % places.size());
                    updatePlaceInfo(place);
                }
                break;

            // toggle mute
            case R.id.map_mute_fbutton:
                if (!mute) {
                    Toast.makeText(getActivity(), "Muted", Toast.LENGTH_SHORT).show();
                    mute = true;
                    mute_fbutton.setImageResource(R.drawable.ic_av_volume_down);
                } else {
                    Toast.makeText(getActivity(), "Unmuted", Toast.LENGTH_SHORT).show();
                    mute = false;
                    mute_fbutton.setImageResource(R.drawable.ic_av_volume_off);
                }
                break;

            // save current place
            case R.id.map_save_fbutton:
                if (targetLoc != null && places != null && places.size() > 0) {
                    Place placeToSave = places.get((places_page - 1) % places.size());
                    Log.v(LOG_TAG, "Current Place Page: " + places_page);
                    myPlace.savePlace(placeToSave);
                    Toast.makeText(getActivity(), "Saving " + placeToSave.getName(), Toast.LENGTH_LONG).show();
                    break;
                }
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
                   // ImageView imageView = (ImageView) v.findViewById(R.id.info_image_iv);
                    TextView tvTitle = (TextView) v.findViewById(R.id.info_title_tv);
                    TextView tvAddress = (TextView) v.findViewById(R.id.info_address_tv);
                    TextView tvRating = (TextView) v.findViewById(R.id.info_rating_tv);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.info_snipppet_tv);

                    tvTitle.setText(marker.getTitle());

                    String snippets[] = marker.getSnippet().split("%%");

                    tvAddress.setText(snippets[0]);

                    if (snippets.length > 1) {
                        tvRating.setText(snippets[1]);
                        if (snippets.length > 2 && snippets[2] != null && !snippets[2].isEmpty()) {
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

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    autocomplete_lv.setVisibility(View.GONE);
                    animateToCurrentLocOnListen = false;
                }
            });

            mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
                @Override
                public void onInfoWindowClose(Marker marker) {
                    animateToCurrentLocOnListen = true;
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    animateToCurrentLocOnListen = false;
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
                    marker.setSnippet(address.toString());
                    marker.showInfoWindow();
                }
            });
        }

    }

    // if animate true, animate; if false, move instantly
    private Location showCurrentLocation(boolean animate) {
        Utility.checkPermission(getActivity());
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(getActivity(), "Couldn't connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            changeCurrentPosMarker(latLng);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            if (animate) mMap.animateCamera(update);
            else mMap.moveCamera(update);
        }
        return currentLocation;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getActivity(), "Ready to Map", Toast.LENGTH_SHORT).show();

        // show current location in the beginning of the program
        if (!init) {
            init = true;
            showCurrentLocation(false);
        }

        mListener = new LocationListener() {

            // tell what to do to listener
            @Override
            public void onLocationChanged(Location location) {

                if (findingPlace) {

                    // if current distance to target is less than 10 meters, end the trip
                    if (targetLoc != null && location.distanceTo(targetLoc) < 10) {
                        findingPlace = false;
                        removeEverything();
                    } else {
//                        Toast.makeText(getActivity(),
//                                "Location changed: " + location.getLatitude() + ", " +
//                                        location.getLongitude(), Toast.LENGTH_SHORT).show();

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        changeCurrentPosMarker(latLng);

                        if (animateToCurrentLocOnListen) {
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                            mMap.animateCamera(update);
                        }


                        // returns the calculated value in meters
                        double radius = Double.parseDouble(Utility.getRadius(getActivity()));

                        if (lastCheckedPoint == null || location.distanceTo(lastCheckedPoint) > radius * 1.5) {
                            fetchPlaces(latLng);
                            Toast.makeText(getActivity(), "Fetching places", Toast.LENGTH_SHORT);
                            lastCheckedPoint = location;
                        }


                        LatLng destPos = destMarker.getPosition();
                        targetLoc = Utility.convertLatLngToLocation(destPos);

                        callCounter++;

                        if (callCounter % 10 == 0){
                            String dest = destPos.latitude + "," + destPos.longitude;
                            String locString = location.getLatitude() + "," + location.getLongitude();
                            FetchDurationTask durationTask = new FetchDurationTask(getActivity());
                            durationTask.execute(locString, dest);
                        }
                        // Update duration time estimate as you drive.
                        // Use new current location to calculate new Duration time

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

    public void fetchPlaces(LatLng latLng) {
        removeMarkers();
        markers = new ArrayList<>();
        FetchPlaceTask placeTask = new FetchPlaceTask(getActivity(), new FetchPlaceTask.AsyncResponse() {
            @Override
            public void processFinish(List<Place> result) {
                if (result != null) {
                    places_page = 0;
                    places = result;
                    AddMarkerToPlaces(result);
                    Place place = result.get(places_page++);
                    String placeFeature = Utility.getPlaceInfoStr(place, currentPosMarker.getPosition());
                    placeInfo_tv.setText(placeFeature);
                }
            }
        });
        placeTask.execute(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude), Utility.getRadius(getActivity()));
    }

    public void updatePlaceInfo(Place place) {
        String placeFeature = Utility.getPlaceInfoStr(place, currentPosMarker.getPosition());
        placeInfo_tv.setText(placeFeature);
        if (!mute)
            Utility.tts(getActivity(), placeFeature);
    }

    public void AddMarkerToPlaces(List<Place> result) {

        for (Place p : result) {

            LatLng latLng = p.getLatLng();

            String address = p.getAddress().toString();
            String rating = "Rating: " + p.getRating();
            String imageUrl = null;
            if (p.getAttributions() != null) {
                imageUrl = p.getAttributions().toString();
            }

            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_store_mall_directory))
                    .title(p.getName().toString())
                    .snippet(address + "%%" + rating + "%%" + imageUrl);

            // Placing a marker on the touched position
            markers.add(mMap.addMarker(markerOptions));
        }
    }

    public void changeCurrentPosMarker(LatLng latLng) {
        // add marker
        if (currentPosMarker != null) {
            currentPosMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_image_lens))
                .snippet("")
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