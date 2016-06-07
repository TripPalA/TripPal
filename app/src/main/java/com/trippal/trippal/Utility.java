package com.trippal.trippal;

import android.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Calvin on 6/5/2016.
 */
public class Utility {
    private static final String LOG_TAG = "Utility";

    public static String getApiKey(Activity activity) {

        //ref: http://www.coderzheaven.com/2013/10/03/meta-data-android-manifest-accessing-it/
        try {
            ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(
                    activity.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myAPIKey = bundle.getString("com.google.android.maps.v2.API_KEY");
            //   System.out.println("API KEY : " + myAPIKey);

            return myAPIKey;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String value = prefs.getString(context.getString(R.string.pref_units_key), null);
        String metric = prefs.getString(context.getString(R.string.pref_units_metric), context.getString(R.string.pref_units_metric));

        /*Log.v(LOG_TAG, prefs.getString(context.getString(R.string.pref_units_key), null));
        Log.v(LOG_TAG, prefs.getString(context.getString(R.string.pref_units_metric), context.getString(R.string.pref_units_metric)));
        Log.v(LOG_TAG, String.valueOf(key.equalsIgnoreCase(value)));*/

        return value.equalsIgnoreCase(metric);
    }

    public static String getRadius(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // String radius = context.getString(R.string.pref_radius_default);


        String radius = prefs.getString(context.getString(R.string.pref_radius_key), null);
        if (isMetric(context)) {
            Log.v(LOG_TAG, "metric: " + radius + "km");
            return radius;
        } else {
            double r = Double.parseDouble(radius) * .6214;
            Log.v(LOG_TAG, "imperial: " + radius + "mi -> " + r + "km");
            return String.valueOf(r);
        }
    }

    public static void checkPermission(Activity activity) {
        int MY_PERMISSIONS_REQUEST = 0;
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }


}
