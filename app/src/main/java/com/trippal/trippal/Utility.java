package com.trippal.trippal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

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

}
