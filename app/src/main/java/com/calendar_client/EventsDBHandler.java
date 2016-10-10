package com.calendar_client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.calendar_client.data.Event;

import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.description;

/**
 * Created by Nadav on 26-Sep-16.
 */

public class EventsDBHandler {

    private MySQLiteHelper dbHelper;

    public EventsDBHandler(Context context)
    {
        dbHelper = new MySQLiteHelper(context, EventsDBConstants.EVENTS_DB_NAME, null, EventsDBConstants.EVENTS_DB_VERSION);
    }

    // returns true/false if the addition was successful
    public boolean addEvent(Event newEvent)
    {
        // this opens the connection to the DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues columnValues = new ContentValues();
        columnValues.put(EventsDBConstants.EVENT_TITLE, newEvent.getTitle());
        //columnValues.put(EventsDBConstants.EVENT_START_DATE, newEvent.getDateStart());
       // columnValues.put(EventsDBConstants.EVENT_END_DATE, newEvent.getDateEnd());
        columnValues.put(EventsDBConstants.EVENT_DESCRIPTION, newEvent.getDescription());


        long result = db.insert(EventsDBConstants.EVENTS_TABLE_NAME, null, columnValues);

        db.close();

        // when result is -1 it means the insert has failed, so when NOT -1 it was successful
        return (result != -1);

    }

    public ArrayList<Event> getAllEvents()
    {
        ArrayList<Event> eventsList = new ArrayList<Event>();
        // this opens the connection to the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // select * from BOOKS table
        Cursor eventsCursor = db.query(EventsDBConstants.EVENTS_TABLE_NAME, null, null, null, null, null, null);
        // each round in the loop is a record in the DB
        while(eventsCursor.moveToNext()) {
            String eventTitle = eventsCursor.getString(0);
          //  Calendar eventStartDate = eventsCursor.getString(1);
          //  Calendar eventEndDate = eventsCursor.getString(2);
            String eventDescription = eventsCursor.getString(1);

            Event e = new Event(eventTitle , eventDescription);
            eventsList.add(e);
        }

        return eventsList;

    }

}
