package com.calendar_client; /**
 * Created by Nadav on 20/11/2016.
 */

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
import com.calendar_client.ui.CalendarActivity;
import com.calendar_client.ui.EventsActivity;
import com.calendar_client.utils.EventsDBHandler;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;

import java.util.Calendar;

public class MyFirebaseMessagingService extends  FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("TEST", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("TEST", "Message data payload: " + remoteMessage.getData());
            Event e = new Gson().fromJson(remoteMessage.getData().toString(),Event.class);
            Log.d("event","event recived: " +e);
            // get user from shared preference. if doesnt exist return empty string
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userJSON = sharedPreferences.getString("user", "");
            User u = new Gson().fromJson(userJSON, User.class);

            EventsDBHandler eventsDBHandler = new EventsDBHandler(this);
            eventsDBHandler.addEvent(e,u.getId());

            NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            int date = e.getDateStart().get(Calendar.DAY_OF_MONTH);
            String message = String.valueOf(date);
            if (date < 10){
                message = "0" + date;
            }
            message += "/";
            date = + e.getDateStart().get(Calendar.MONTH);
            if (date < 10){
                message += "0" + date;
            }
            message += " ";

            date =e.getDateStart().get(Calendar.HOUR_OF_DAY);
            if (date < 10){
                message += "0" + date;
            }
            message += ":";
            date =e.getDateStart().get(Calendar.MINUTE);
            if (date < 10){
                message += "0" + date;
            }

//
//            String message = e.getDateStart().get(Calendar.DAY_OF_MONTH) + "/" + e.getDateStart().get(Calendar.MONTH)
//                    + " " + e.getDateStart().get(Calendar.HOUR_OF_DAY) + ":" + e.getDateStart().get(Calendar.MINUTE);
            Notification notification = getNotification(message);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle( e.getTitle())
                            .setContentInfo("New event created by")
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setSound(uri);


            // int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(1, mBuilder.build());
            Log.d("TEST", "finished notifiation");

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TEST", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }

    private Notification getNotification(String eventTitle) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(eventTitle)
                        .setAutoCancel(true)
                        .setSound(uri);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getApplicationContext(), CalendarActivity.class);

        // TODO - change from CalendarActivity to EventDetailsAct
        // add extra paramter on the intent
        //resultIntent.put

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(EventsActivity.class);
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
