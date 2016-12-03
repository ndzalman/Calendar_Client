package com.calendar_client.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calendar_client.R;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;
    private AlertDialog alertDialog;
    private final static int SPLASH_TIME=1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "BreeSerif-Regular.ttf");

        // link the fields in layout
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        tvStatus.setTypeface(typeface);

        // if no wifi/data connection was found
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_dialog_title)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CheckConnectivityTask().execute();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
        alertDialog = alertDialogBuilder.create();

        // task to check connectivity
        new CheckConnectivityTask().execute();
    }

    private class CheckConnectivityTask extends AsyncTask<Void,Integer,Boolean>{
        Intent intent;

        @Override
        protected Boolean doInBackground(Void... params) {
            int i = 0;
            while (i < 6) {
                try {
                    Thread.sleep(SPLASH_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i+=2;
                publishProgress(i);
            }
            Boolean isConnected = isOnline();
            return isConnected;
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected == false){
                alertDialog.show();
            }else{
                intent = new Intent(SplashScreenActivity.this,LoginActivity.class);
                finish();
                startActivity(intent);
            }
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] <= 2 ){
                tvStatus.setText(R.string.splash_screen_status_start);
            } else if (values[0] == 4){
                tvStatus.setText(R.string.splash_screen_status_in_progress);
            } else if (values[0] > 4){
                tvStatus.setText(R.string.splash_screen_status_end);
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
