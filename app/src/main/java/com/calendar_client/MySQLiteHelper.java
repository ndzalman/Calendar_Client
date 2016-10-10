package com.calendar_client;

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
                "( " + EventsDBConstants.EVENT_TITLE + " TEXT, " +
                //EventsDBConstants.EVENT_START_DATE + " CALENDAR, " +
                //EventsDBConstants.EVENT_END_DATE + " TEXT" +
                EventsDBConstants.EVENT_DESCRIPTION + " TEXT)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

//        db.execSQL("alter table " + EventsDBConstants.EVENTS_TABLE_NAME + " add column " + EventsDBConstants.EVENT_TITLE + " TEXT");
    }




}