package com.calendar_client.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBHandler;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;
    private AlertDialog alertDialog;
    private AlertDialog exitDialog;
    private final static int SPLASH_TIME = 300;
    private Intent intent;
    private Data data;
    private User user;
    private GetSharedEventsTask getSharedEventsTask;
    private RefreshTokenTask refreshTokenTask;
    private boolean permissionGranted = false;
    private boolean logged = false;
    private EventsDBHandler eventsDBHandler;
    private final int NETWORK_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        eventsDBHandler = new EventsDBHandler(this);

        // link the fields in layout
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

//        Typeface typeface = Typeface.createFromAsset(getAssets(), "BreeSerif-Regular.ttf");
//        tvStatus.setTypeface(typeface);

        intent = new Intent(SplashScreenActivity.this, LoginActivity.class);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        if (!userJSON.equals("")){
            user = new Gson().fromJson(userJSON,User.class);
            logged = true;
            intent = new Intent(SplashScreenActivity.this, CalendarActivity.class);
            refreshTokenTask = new RefreshTokenTask();
            refreshTokenTask.execute();
        }


        data = Data.getInstance();
        initAlertDialog(getString(R.string.alert_dialog_title));

        //check if user in online, then if he is logged get the events from server
        // and start the progress bar task
        checkConnectivityAndUserLogged();

    }

    private void initAlertDialog(String title){
        // if no wifi/data connection was found
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title)
                .setMessage(R.string.alert_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (logged) {
                            Data.getInstance().setOnline(false);
                            data.setSharedEvents(eventsDBHandler.getAllEvents(user.getId()));
                            nextActivity();
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
    }

    private void checkConnectivityAndUserLogged(){
        checkPermission();
        if (permissionGranted) {
            data.setOnline(isOnline());
        }

        if (logged){
            if (data.isOnline()){
                getSharedEventsTask = new GetSharedEventsTask();
                getSharedEventsTask.execute();
            } else{
                data.setSharedEvents(eventsDBHandler.getAllEvents(user.getId()));
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

            if (getSharedEventsTask != null){ // if online
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
            if (!logged) {
                nextActivity();
            } else if(isConnected){

            } else if (!isConnected){
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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void checkPermission() {
        int permission1;
        int permission2;

        if (Build.VERSION.SDK_INT < 23) {
            permission1 = PermissionChecker.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.INTERNET);
            permission2 = PermissionChecker.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.ACCESS_NETWORK_STATE);

            if (permission1 == PermissionChecker.PERMISSION_GRANTED && permission2 == PermissionChecker.PERMISSION_GRANTED) {
                permissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE
                }, NETWORK_PERMISSION_REQUEST);
            }
        } else { //api 23 and above
            permission1 = checkSelfPermission(Manifest.permission.INTERNET);
            permission2 = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);

            if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED ) {
                // We don't have permission so prompt the user
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE},
                        NETWORK_PERMISSION_REQUEST);
            } else {
                permissionGranted = true;
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case NETWORK_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    permissionGranted = true;
                } else{
                    // Permission Denied
                    Toast.makeText(SplashScreenActivity.this,
                            "We couldn't determine if you hava internet connection," +
                                    " please approve this permission", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;

        }
    }

    private class GetSharedEventsTask extends AsyncTask<String, Void, String> {
        // executing
        @Override
        protected String doInBackground(String... strings) {
            Log.e("SharedEvents","In Shared Events task url " + ApplicationConstants.GET_ALL_SHARED_EVENTS);
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.GET_ALL_SHARED_EVENTS + "?id=" + user.getId());
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                Log.e("DEBUG", conn.getResponseCode() + "");
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DEBUG", conn.getResponseMessage());
                    return "-3";
                }

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    response.append(line + "\n");
                }

                input.close();

                conn.disconnect();


            }catch (ConnectException ex){
                Log.e("connect-timeout","excption " + ex.getMessage());
                return  "-1";
            } catch (SocketTimeoutException ste) {
                Log.e("socket-timeout","excption " + ste.getMessage());
                return "-2";
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                Log.e("RESPONSE","response is " + response);
                if (response.equals("-1") || response.equals("-2") || response.equals("-3")){
                 initAlertDialog(getString(R.string.alert_dialog_title_server_off));
                 alertDialog.show();
            }else{
                    nextActivity();
                    Type listType = new TypeToken<ArrayList<Event>>() {
                    }.getType();
                    List<Event> sharedEvents = new Gson().fromJson(response.toString(), listType);
                    data.setSharedEvents(sharedEvents);

                }
            }
        }

    }
    private class RefreshTokenTask extends AsyncTask<String, Void, String> {
        // get the email and password - before executing task
        String token="";

        @Override
        protected void onPreExecute() {
            token = FirebaseInstanceId.getInstance().getToken();
            user.setToken(token);
            Log.e("TOKEN", token);
            Log.d("REFRESH", "user id: " + user.getId());
        }

        // executing
        // make http request to the server, send email and password for verification of the user
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.REFRESH_TOKEN_URL + "?id=" + user.getId() + "&token=" + token);
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
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

            String responseString = response.toString();
            return responseString.toString();
        }

    }

    private void nextActivity(){
        finish();
        startActivity(intent);
    }

}
