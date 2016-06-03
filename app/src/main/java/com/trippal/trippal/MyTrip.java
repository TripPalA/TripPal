package com.trippal.trippal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.trippal.trippal.data.TripContract;
import com.trippal.trippal.data.TripDbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Calvin on 6/2/2016.
 */
public class MyTrip {

    private final Context mContext;

    public MyTrip(Context context) {
        mContext = context;
        init();
    }

    public void init() {
        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.rawQuery("select * from " + TripContract.TripEntry.TABLE_NAME, null);

        } catch (Exception e) {
            //create table if does not exist
            dbHelper.onCreate(db);

            Log.i("", "Table does not exist. New table created.");
            init();
        }

        db.close();
    }

    public void saveTrip(Place origin, Place dest) {

        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues placeValue = new ContentValues();
        placeValue.put(TripContract.TripEntry.COLUMN_ORIGIN_NAME, origin.getName().toString());
        placeValue.put(TripContract.TripEntry.COLUMN_ORIGIN_ADDRESS, origin.getAddress().toString());
        placeValue.put(TripContract.TripEntry.COLUMN_ORIGIN_GOOGLE_ID, origin.getId());
        placeValue.put(TripContract.TripEntry.COLUMN_DEST_NAME, dest.getName().toString());
        placeValue.put(TripContract.TripEntry.COLUMN_DEST_ADDRESS, dest.getAddress().toString());
        placeValue.put(TripContract.TripEntry.COLUMN_DEST_GOOGLE_ID, dest.getId());
        placeValue.put(TripContract.PlaceEntry.COLUMN_DATE, System.currentTimeMillis());
        long locationRowId;
        locationRowId = db.insert(TripContract.TripEntry.TABLE_NAME, null, placeValue);
        if (locationRowId < 0) {
            Log.e("SAVE PLACE: ", "could not add place into db.");
        } else {
            Log.i("SAVE PLACE: ", "inserted in row id " + locationRowId);
        }

        db.close();

    }

    class SavedTrip {
        private String originName, originAddress, originGoogleId;
        private String destName, destAddress, destGoogleId;
        private Long date;

        public String getOriginName() {
            return this.originName;
        }

        public void setOriginName(String name) {
            this.originName = name;
        }

        public void setOriginAddress(String address) {
            this.originAddress = address;
        }

        public String getOriginAddress() {
            return this.originAddress;
        }

        public void setOriginGoogleId(String id) {
            this.originGoogleId = id;
        }

        public String getOriginGoogleId() {
            return this.originGoogleId;
        }

        public String getDestName() {
            return this.destName;
        }

        public void setDestName(String name) {
            this.destName = name;
        }

        public void setDestAddress(String address) {
            this.destAddress = address;
        }

        public String getDestAddress() {
            return this.destAddress;
        }

        public void setDestGoogleId(String id) {
            this.destGoogleId = id;
        }

        public String getDestGoogleId() {
            return this.destGoogleId;
        }

        public void setDate(Long date) {
            this.date = date;
        }

        public Long getDate() {
            return this.date;
        }


    }

    public List<SavedTrip> getTrips() {
        List<SavedTrip> list = new ArrayList<SavedTrip>();
        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + TripContract.TripEntry.TABLE_NAME, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            //return empty values if table is empty
            SavedTrip entry = new SavedTrip();
            entry.setOriginName("");
            entry.setOriginAddress("");
            entry.setOriginGoogleId("");
            entry.setDestName("");
            entry.setDestAddress("");
            entry.setDestGoogleId("");
            entry.setDate(1L);
            list.add(entry);
            return list;
        }


        int colOriginName = c.getColumnIndex(TripContract.TripEntry.COLUMN_ORIGIN_NAME);
        int colOriginAddress = c.getColumnIndex(TripContract.TripEntry.COLUMN_ORIGIN_ADDRESS);
        int colOriginGoogleId = c.getColumnIndex(TripContract.TripEntry.COLUMN_ORIGIN_GOOGLE_ID);
        int colDestName = c.getColumnIndex(TripContract.TripEntry.COLUMN_DEST_NAME);
        int colDestAddress = c.getColumnIndex(TripContract.TripEntry.COLUMN_DEST_ADDRESS);
        int colDestGoogleId = c.getColumnIndex(TripContract.TripEntry.COLUMN_DEST_GOOGLE_ID);
        int colDate = c.getColumnIndex(TripContract.TripEntry.COLUMN_DATE);


        do {
            SavedTrip entry = new SavedTrip();
            entry.setOriginName(c.getString(colOriginName));
            entry.setOriginAddress(c.getString(colOriginAddress));
            entry.setOriginGoogleId(c.getString(colOriginGoogleId));
            entry.setDestName(c.getString(colDestName));
            entry.setDestAddress(c.getString(colDestAddress));
            entry.setDestGoogleId(c.getString(colDestGoogleId));
            entry.setDate(c.getLong(colDate));
            list.add(entry);

        } while (c.moveToNext());

        return list;
    }

    public void clearTrips() {
        TripDbHelper dbHelper = new TripDbHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TripContract.TripEntry.TABLE_NAME);
        init();
    }
}
