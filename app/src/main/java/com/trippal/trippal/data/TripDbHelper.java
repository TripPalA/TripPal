package com.trippal.trippal.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Calvin on 5/29/2016.
 */
public class TripDbHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "places.db";

    public TripDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //put your sql create table statements here

        final String SQL_CREATE_PLACES_TABLE =
                        "CREATE TABLE IF NOT EXISTS " + TripContract.PlaceEntry.TABLE_NAME + " ( " +
                        TripContract.PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TripContract.PlaceEntry.COLUMN_PLACE_NAME + " TEXT NOT NULL, " +
                        TripContract.PlaceEntry.COLUMN_PLACE_ADDRESS + " TEXT, " +
                        TripContract.PlaceEntry.COLUMN_COORD_LAT + " REAL, " +
                        TripContract.PlaceEntry.COLUMN_COORD_LONG + " REAL, " +
                        TripContract.PlaceEntry.COLUMN_GOOGLE_PLACE_ID + " TEXT NOT NULL, " +
                        TripContract.PlaceEntry.COLUMN_DATE + " INTEGER NOT NULL " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);

        final String SQL_CREATE_TRIPS_TABLE =
                        "CREATE TABLE IF NOT EXISTS " + TripContract.TripEntry.TABLE_NAME + " ( " +
                        TripContract.TripEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TripContract.TripEntry.COLUMN_ORIGIN_NAME + " TEXT NOT NULL, " +
                        TripContract.TripEntry.COLUMN_ORIGIN_ADDRESS + " TEXT, " +
                        TripContract.TripEntry.COLUMN_ORIGIN_GOOGLE_ID + " TEXT NOT NULL, " +
                        TripContract.TripEntry.COLUMN_DEST_NAME + " TEXT NOT NULL, " +
                        TripContract.TripEntry.COLUMN_DEST_ADDRESS + " TEXT, " +
                        TripContract.TripEntry.COLUMN_DEST_GOOGLE_ID + " TEXT NOT NULL, " +
                        TripContract.TripEntry.COLUMN_DATE + " INTEGER NOT NULL " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_TRIPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TripContract.TripEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TripContract.PlaceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
