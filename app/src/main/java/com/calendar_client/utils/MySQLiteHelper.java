package com.calendar_client.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Nadav on 26-Sep-16.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {


    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // execute all create statements
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + EventsDBConstants.EVENTS_TABLE_NAME +
                "( " + EventsDBConstants.EVENTS_ID + " INTEGER, " +
                EventsDBConstants.EVENTS_USER_ID + " INTEGER, " +
                EventsDBConstants.EVENT_TITLE + " TEXT, " +
                EventsDBConstants.EVENT_DESCRIPTION + " TEXT, " +
                EventsDBConstants.EVENT_START_DATE + " TEXT, " +
                EventsDBConstants.EVENT_END_DATE+ " TEXT)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // after delete the table, get all user events from server and fill the table
        db.delete(EventsDBConstants.EVENTS_TABLE_NAME,null,null);
        onCreate(db);
    }




}