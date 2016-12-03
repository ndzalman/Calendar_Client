package com.calendar_client.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;
    private AlertDialog alertDialog;
    private AlertDialog exitDialog;
    private final static int SPLASH_TIME = 500;
    private Intent intent;
    private Data data;
    private User user;
    private GetSharedEventsTask getSharedEventsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        Typeface typeface = Typeface.createFromAsset(getAssets(), "BreeSerif-Regular.ttf");

        // link the fields in layout
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

//        tvStatus.setTypeface(typeface);

        // if no wifi/data connection was found
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(this);



        // if no wifi/data connection was found
        alertDialogBuilder.setTitle(R.string.alert_dialog_title)
                .setMessage(R.string.alert_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String userJSON = sharedPreferences.getString("user", "");
                        if (userJSON != null && !userJSON.equals("")) {
                            NextActivity();
                        } else{
                            dialog.cancel();
                            // if user decided to continue offline but there is no user in the shared preferences
                            AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(SplashScreenActivity.this);
                            exitDialogBuilder.setTitle(R.string.exit_alert_dialog_title)
                                    .setCancelable(false)
                                    .setNegativeButton(R.string.alert_dialog_negative, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            finish();
                                        }
                                    });
                            exitDialog = exitDialogBuilder.create();
                            exitDialog.show();
                        }
                    }
                })
                .setNegativeButton(R.string.alert_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                })
                .setNeutralButton(getString(R.string.alert_dialog_neutral), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                checkConnectivityAndUserLogged();
            }});


        alertDialog = alertDialogBuilder.create();

        data = Data.getInstance();

        //check if user in online, then if he is logged get the events from server
        // and start the progress bar task
        checkConnectivityAndUserLogged();

    }

    private void checkConnectivityAndUserLogged(){
        data.setOnline(isOnline());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        if (userJSON != null && !userJSON.equals("")){
            user = new Gson().fromJson(userJSON, User.class);
            if (data.isOnline()){
                getSharedEventsTask = new GetSharedEventsTask();
                getSharedEventsTask.execute();
            }
        }
        new ProgressBarTask().execute();
    }

    private class ProgressBarTask extends AsyncTask<Void, Integer, Boolean> {


        @Override
        protected void onPreExecute() {
            tvStatus.setText(R.string.splash_screen_status_start);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            int i = 0;

            if (getSharedEventsTask != null){
                while (i < 4) {
                    i=2;
                    if (getSharedEventsTask.getStatus() == Status.FINISHED){
                        i = 4;
                    }
                    publishProgress(i);
                    try {
                        Thread.sleep(SPLASH_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            } else{
                while (i < 4) {
                    i += 2;
                    publishProgress(i);
                    try {
                        Thread.sleep(SPLASH_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

            return data.isOnline();
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected) {
                NextActivity();
            } else{
                alertDialog.show();
            }
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] <= 2) {
                tvStatus.setText(R.string.splash_screen_status_in_progress);
            } else if (values[0] == 4) {
                tvStatus.setText(R.string.splash_screen_status_end);
            }
        }
    }

    private void NextActivity(){
        intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private class GetSharedEventsTask extends AsyncTask<String, Void, String> {
        // executing
        @Override
        protected String doInBackground(String... strings) {
            Log.e("SharedEvents","In Shared Events task");
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.GET_ALL_SHARED_EVENTS + "?id=" + user.getId());
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

                Type listType = new TypeToken<ArrayList<Event>>() {
                }.getType();
                List<Event> sharedEvents = new Gson().fromJson(response.toString(), listType);
                data.setSharedEvents(sharedEvents);


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d("EVENT-SIZE", "events size: " + data.getSharedEvents().size());
            if (data.getSharedEvents().size() > 0) {
                Log.d("EVENT-SIZE", "event: " + data.getSharedEvents().get(0).toString());
                Log.d("EVENT-SIZE", "event: " + data.getSharedEvents().get(0).getUsers().toString());
            }

        }

    }


}
