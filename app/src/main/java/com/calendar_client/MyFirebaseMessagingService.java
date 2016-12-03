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
import com.calendar_client.ui.NotificationEventActivity;
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


            Notification notification = getNotification(e);
//            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//            NotificationCompat.Builder mBuilder =
//                    new NotificationCompat.Builder(this)
//                            .setSmallIcon(R.drawable.ic_stat_name)
//                            .setContentTitle( e.getTitle())
//                            .setContentInfo("New event created by")
//                            .setContentText(message)
//                            .setAutoCancel(true)
//                            .setSound(uri);
//
//            Intent resultIntent = new Intent(getApplicationContext(), EventsActivity.class);
//
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
//            stackBuilder.addParentStack(EventsActivity.class);
//            stackBuilder.addNextIntent(resultIntent);
//            PendingIntent resultPendingIntent =
//                    stackBuilder.getPendingIntent(
//                            0,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//            mBuilder.setContentIntent(resultPendingIntent);


            // int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(1, notification);
            Log.d("TEST", "finished notifiation");

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TEST", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }

    private Notification getNotification(Event e) {

        String message = e.getDateStart().get(Calendar.DAY_OF_MONTH) + "/" + e.getDateStart().get(Calendar.MONTH)
                + " " + e.getDateStart().get(Calendar.HOUR_OF_DAY) + ":" + e.getDateStart().get(Calendar.MINUTE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(e.getTitle())
                .setContentText(message)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
//                .setLargeIcon(R.drawable.ic_stat_name)

                // Set ticker text (preview) information for this notification.
                .setTicker(e.getTitle())

                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                this,
                                0,
                                new Intent(getApplicationContext(), NotificationEventActivity.class).putExtra("event",e),
                                PendingIntent.FLAG_UPDATE_CURRENT))

                // Show expanded text content on devices running Android 4.1 or
                // later.
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(e.getDescription())
                        .setBigContentTitle(e.getTitle())
                        .setSummaryText(message))


//                .addAction(
//                        R.drawable.ic_action_stat_share,
//                        res.getString(R.string.action_share),
//                        PendingIntent.getActivity(
//                                context,
//                                0,
//                                Intent.createChooser(new Intent(Intent.ACTION_SEND)
//                                        .setType("text/plain")
//                                        .putExtra(Intent.EXTRA_TEXT, "Dummy text"), "Dummy title"),
//                                PendingIntent.FLAG_UPDATE_CURRENT))
//                .addAction(
//                        R.drawable.ic_action_stat_reply,
//                        res.getString(R.string.action_reply),
//                        null)

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        return mBuilder.build();

    }
}
