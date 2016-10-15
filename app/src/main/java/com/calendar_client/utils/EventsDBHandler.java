package com.calendar_client.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.calendar_client.data.Event;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.MySQLiteHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
        columnValues.put(EventsDBConstants.EVENT_DESCRIPTION, newEvent.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.DATE_TIME_FORMAT);
        Date date = newEvent.getDateStart().getTime();
        columnValues.put(EventsDBConstants.EVENT_START_DATE, sdf.format(date));
        date = newEvent.getDateEnd().getTime();
        columnValues.put(EventsDBConstants.EVENT_END_DATE, sdf.format(date));

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

        Cursor eventsCursor = db.query(EventsDBConstants.EVENTS_TABLE_NAME, null, null, null, null, null, null);
        // each round in the loop is a record in the DB
        SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.DATE_TIME_FORMAT);
        Calendar calendar = Calendar.getInstance();

        while(eventsCursor.moveToNext()) {
            int eventId = eventsCursor.getInt(0);
            String eventTitle = eventsCursor.getString(1);
            String eventDescription = eventsCursor.getString(2);
            String eventStartDate = eventsCursor.getString(3);
            String eventEndDate = eventsCursor.getString(4);

            Event event = new Event();
            event.setId(eventId);
            event.setTitle(eventTitle);
            event.setDescription(eventDescription);
            try {
                calendar.setTime(sdf.parse(eventStartDate));
                event.setDateStart(calendar);
                calendar.setTime(sdf.parse(eventEndDate));
                event.setDateEnd(calendar);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            eventsList.add(event);
        }

        return eventsList;

    }

}
