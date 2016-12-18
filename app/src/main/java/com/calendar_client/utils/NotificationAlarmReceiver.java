package com.calendar_client.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.ui.AboutActivity;
import com.calendar_client.ui.DayEventsActivity;
import com.calendar_client.ui.EventActivity;
import com.google.gson.Gson;

import java.util.Calendar;

public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
//        //you might want to check what's inside the Intent
        if(intent.getSerializableExtra("event") != null){
            Event e = (Event) intent.getSerializableExtra("event");
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
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
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.calendario_icon_transperent)
                    //example for large icon
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(e.getTitle())
                    .setContentText(message)
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            Intent i = new Intent(context, EventActivity.class);
            i.putExtra("event",e);
            i.putExtra("notification",true);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            i,
                            PendingIntent.FLAG_ONE_SHOT
                    );
            // example for blinking LED
            mBuilder.setLights(0xFFb71c1c, 1000, 2000);
            mBuilder.setSound(uri);
            mBuilder.setContentIntent(pendingIntent);
            mNotifyMgr.notify(12345, mBuilder.build());
        }

    }
}

