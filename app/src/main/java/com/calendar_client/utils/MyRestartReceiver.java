package com.calendar_client.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nadav on 27/12/2016.
 */

public class MyRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("TESTAlarm", "inside receiver");
        Toast.makeText(context, "inside rec", Toast.LENGTH_LONG).show();
        User user;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userJSON = sharedPreferences.getString("user", "");
        if (!userJSON.equals("")) {
            Log.e("TESTAlarm", "found user");
            Toast.makeText(context, "found user", Toast.LENGTH_LONG).show();
            user = new Gson().fromJson(userJSON, User.class);

            EventsDBHandler myHandler = new EventsDBHandler(context);
            ArrayList<Event> myEvents = myHandler.getUpComingEvents(user.getId());

            for (Event event : myEvents) {
                Log.e("TESTAlarm", "inside event");
                Toast.makeText(context, "fund event", Toast.LENGTH_LONG).show();
                scehduleEvent(context, event);
            }
        }
    }

    public void scehduleEvent(Context context, Event event)
    {
        int id = (int) event.getDateStart().getTimeInMillis();
        Log.e("schedule", "id is " + id);
        Toast.makeText(context, "scehdlung id " + id, Toast.LENGTH_LONG).show();

        long reminder = TimeUnit.MINUTES.toMillis(event.getReminder());
        Log.e("ALARAM","reminder: " + reminder);
        long when = event.getDateStart().getTimeInMillis() + reminder;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        intent.putExtra("event", event);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        Log.e("TESTAlarm", "scheduled event for " + event.getDateStart().toString());
        Toast.makeText(context, "finish sche", Toast.LENGTH_LONG).show();
    }
}

