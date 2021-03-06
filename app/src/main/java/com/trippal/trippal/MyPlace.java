package com.trippal.trippal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.location.places.Place;
import com.trippal.trippal.data.TripContract;
import com.trippal.trippal.data.TripDbHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Calvin on 5/29/2016.
 */
public class MyPlace {
    private final Context mContext;

    public MyPlace(Context context) {
        mContext = context;
        init();
    }

    public void init() {
        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //db.execSQL("Drop table if exists saved_places");

        try {
            db.rawQuery("select * from " + TripContract.PlaceEntry.TABLE_NAME, null);

        } catch (Exception e) {
            //create table if does not exist
            dbHelper.onCreate(db);

            Log.i("", "Table does not exist. New table created.");
            init();
        }

        db.close();
    }

    public void savePlace(Place place) {

        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues placeValue = new ContentValues();
        placeValue.put(TripContract.PlaceEntry.COLUMN_PLACE_NAME, place.getName().toString());
        placeValue.put(TripContract.PlaceEntry.COLUMN_PLACE_ADDRESS, place.getAddress().toString());
        placeValue.put(TripContract.PlaceEntry.COLUMN_GOOGLE_PLACE_ID, place.getId());
        //placeValue.put(TripContract.PlaceEntry.COLUMN_COORD_LAT, place.getLatLng().toString());
        //placeValue.put(TripContract.PlaceEntry.COLUMN_COORD_LONG, ");
        placeValue.put(TripContract.PlaceEntry.COLUMN_DATE, System.currentTimeMillis());
        long locationRowId;
        locationRowId = db.insert(TripContract.PlaceEntry.TABLE_NAME, null, placeValue);
        if (locationRowId < 0) {
            Log.e("SAVE PLACE: ", "could not add place into db.");
        } else {
            Log.i("SAVE PLACE: ", "inserted in row id " + locationRowId);
        }

        db.close();

    }

    class SavedPlace {
        private String name, address, googleId;
        private Long date;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return this.address;
        }

        public void setGoogleId(String id) {
            this.googleId = id;
        }

        public String getGoogleId() {
            return this.googleId;
        }

        public void setDate(Long date) {
            this.date = date;
        }

        public Long getDate() {
            return this.date;
        }


    }

    public List<SavedPlace> getPlaces() {
        List<SavedPlace> list = new ArrayList<SavedPlace>();
        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + TripContract.PlaceEntry.TABLE_NAME, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            //return empty values if table is empty
            SavedPlace entry = new SavedPlace();
            entry.setName("");
            entry.setAddress("");
            entry.setGoogleId("");
            entry.setDate(1L);
            list.add(entry);
            return list;
        }

        int colName = c.getColumnIndex(TripContract.PlaceEntry.COLUMN_PLACE_NAME);
        int colAddress = c.getColumnIndex(TripContract.PlaceEntry.COLUMN_PLACE_ADDRESS);
        int colGoogleId = c.getColumnIndex(TripContract.PlaceEntry.COLUMN_GOOGLE_PLACE_ID);
        int colDate = c.getColumnIndex(TripContract.PlaceEntry.COLUMN_DATE);


        do {
            SavedPlace entry = new SavedPlace();
            entry.setName(c.getString(colName));
            entry.setAddress(c.getString(colAddress));
            entry.setGoogleId(c.getString(colGoogleId));
            entry.setDate(c.getLong(colDate));
            list.add(entry);

        } while (c.moveToNext());

        return list;
    }

    public void clearPlaces() {
        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TripContract.PlaceEntry.TABLE_NAME);
        init();
    }
}