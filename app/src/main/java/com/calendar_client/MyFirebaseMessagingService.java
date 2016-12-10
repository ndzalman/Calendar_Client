package com.calendar_client;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.ui.EventActivity;
import com.calendar_client.ui.UpComingEventsActivity;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBHandler;
import com.calendar_client.utils.NotificationReceiver;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;

import java.util.Calendar;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("TEST", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("TEST", "Message data payload: " + remoteMessage.getData());
            Event e = new Gson().fromJson(remoteMessage.getData().toString(), Event.class);
            Log.d("event", "event recived: " + e);
            // get user from shared preference. if doesnt exist return empty string
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userJSON = sharedPreferences.getString("user", "");
            User u = new Gson().fromJson(userJSON, User.class);

            EventsDBHandler eventsDBHandler = new EventsDBHandler(this);
            eventsDBHandler.addEvent(e, u.getId());
            Data.getInstance().getSharedEvents().add(e);
            scheduleNotification(e);

            sendNotification(e);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TEST", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

    private void sendNotification(Event e) {

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setSmallIcon(R.drawable.calendario_icon_transperent);
//        mBuilder.setContentTitle(e.getTitle());
//        mBuilder.setContentText(e.getDescription());
        int year = e.getDateStart().get(Calendar.YEAR);
        int month = e.getDateStart().get(Calendar.MONTH);
        int day = e.getDateStart().get(Calendar.DAY_OF_MONTH);
        String monthTxt = String.valueOf(month);
        String dayTxt = String.valueOf(day);
        if (month < 10) {
            monthTxt = '0' + monthTxt;
        }
        if (day < 10) {
            dayTxt = '0' + dayTxt;
        }
        String message = dayTxt + "/" + monthTxt + "/" + year;

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.calendario_icon_transperent)
                        .setContentTitle(e.getTitle())
//                .setContentInfo("New event created by")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(uri);

        Intent resultIntent = new Intent(this, EventActivity.class);
        resultIntent.putExtra("event", e);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(EventActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void scheduleNotification(Event event) {
        Log.e("TEST","schedule notfication");
        Intent notificationIntent = new Intent(MyFirebaseMessagingService.this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, event.getId());
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, getNotification(event));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, event.getDateStart().getTimeInMillis(), pendingIntent);
    }

    private Notification getNotification(Event event) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(event.getTitle())
                        .setContentText(event.getDateStart().get(Calendar.HOUR_OF_DAY) + ":" + event.getDateStart().get(Calendar.MINUTE)
                                + " - " + event.getDateEnd().get(Calendar.HOUR_OF_DAY) + ":" + event.getDateEnd().get(Calendar.MINUTE)
                        )
                        .setAutoCancel(true)
                        .setSound(uri);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, EventActivity.class);
        resultIntent.putExtra("event",event);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(UpComingEventsActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }


}
