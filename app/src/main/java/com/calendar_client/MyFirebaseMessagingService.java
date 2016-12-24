package com.calendar_client;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.ui.EventActivity;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBHandler;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("TEST", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("TEST", "Message data payload: " + remoteMessage.getData());
//            Event e = new Gson().fromJson(remoteMessage.getData().toString(), Event.class);
//            Log.d("TEST", "event recived: " + e);
            String id = remoteMessage.getData().get("id");
            int eventId = Integer.parseInt(id);
            // get user from shared preference. if doesnt exist return empty string

            sendNotification(eventId);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TEST", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

    private void sendNotification(int id) {
        new GetEventTask(id).execute();

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setSmallIcon(R.drawable.calendario_icon_transperent);
//        mBuilder.setContentTitle(e.getTitle());
//        mBuilder.setContentText(e.getDescription());


    }

    private void notifcation(Event e){
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
        resultIntent.putExtra("notification", true);

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

    private class GetEventTask extends AsyncTask<String, Void, String> {
        int eventId;

        public GetEventTask(int id){
            this.eventId = id;
        }

        // executing
        @Override
        protected String doInBackground(String... strings) {
            Log.d("test","in get event task");
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.GET_EVENT_BY_ID + "?id=" + eventId);
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.e("DEBUG", conn.getResponseCode() + "");
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DEBUG", conn.getResponseMessage());
                    return null;
                }

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    response.append(line + "\n");
                }

                input.close();

                conn.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d("test","in get event task POST");
            if (response != null){
                Log.d("test","in get event task response is " + response);
                Event newEvent = new Gson().fromJson(response.toString(), Event.class);
                Data.getInstance().getSharedEvents().add(newEvent);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String userJSON = sharedPreferences.getString("user", "");
                User u = new Gson().fromJson(userJSON, User.class);

                EventsDBHandler eventsDBHandler = new EventsDBHandler(MyFirebaseMessagingService.this);
                eventsDBHandler.addEvent(newEvent, u.getId());

                notifcation(newEvent);
            }

        }

    }


}
