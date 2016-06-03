package com.trippal.trippal.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;


/**
 * Created by Calvin on 5/29/2016.
 */
public class TripContract {

    //utility to get the long for the beginning of the day
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }


    public static final class PlaceEntry implements BaseColumns {
        // Table name
        public static final String TABLE_NAME = "saved_places";

        public static final String COLUMN_PLACE_NAME = "place_name";
        public static final String COLUMN_PLACE_ADDRESS = "place_address";
        public static final String COLUMN_GOOGLE_PLACE_ID = "g_place_id";

        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static final String COLUMN_DATE = "date";
    }

    public static final class TripEntry implements BaseColumns {
        // Table name
        public static final String TABLE_NAME = "saved_trips";

        public static final String COLUMN_ORIGIN_NAME = "origin_name";
        public static final String COLUMN_ORIGIN_ADDRESS = "origin_address";
        public static final String COLUMN_ORIGIN_GOOGLE_ID = "origin_g_id";

        public static final String COLUMN_DEST_NAME = "dest_name";
        public static final String COLUMN_DEST_ADDRESS = "dest_address";
        public static final String COLUMN_DEST_GOOGLE_ID = "dest_g_id";

        public static final String COLUMN_DATE = "date";
    }



}
