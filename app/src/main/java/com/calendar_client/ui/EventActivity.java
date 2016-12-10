package com.calendar_client.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity {
    private FloatingActionButton fabEventEdit;
    private FloatingActionButton fabEventDelete;
    private TextView tvEventDateStart;
    private TextView tvEventDateEnd;
    private TextView tvEventTitle;
    private TextView tvEventDescription;
    private TextView tvEventLocation;
    private Button btnShowMore;
    private ListView listContacts;
    private Event event;
    private EventsDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        event = (Event) getIntent().getSerializableExtra("event");
        dbHandler = new EventsDBHandler(this);
        initEventViewComponents();

    }

    private void initEventViewComponents() {
        tvEventTitle = (TextView) findViewById(R.id.tvEventTitle);
        tvEventTitle.setText(event.getTitle());

        tvEventDateStart = (TextView) findViewById(R.id.tvEventDateStart);
        SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.DATE_TIME_FORMAT);
        tvEventDateStart.setText(sdf.format(event.getDateStart().getTime()));

        tvEventDateEnd = (TextView) findViewById(R.id.tvEventDateEnd);
        tvEventDateEnd.setText(sdf.format(event.getDateEnd().getTime()));


        tvEventDescription = (TextView) findViewById(R.id.tvEventDescription);
        if (event.getDescription() != null && !event.getDescription().equals("")) {
            tvEventDescription.setText(event.getDescription());
        } else{
            tvEventDescription.setVisibility(View.GONE);
        }

        tvEventLocation = (TextView) findViewById(R.id.tvEventLocation);

        if (event.getLocation() != null && !event.getLocation().equals("")) {
            tvEventLocation.setText(event.getLocation());
        } else{
            tvEventLocation.setVisibility(View.GONE);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        User user = new Gson().fromJson(userJSON,User.class);

        fabEventEdit = (FloatingActionButton) findViewById(R.id.fabEventEdit);
        if (event.getOwnerId() == user.getId()){
            fabEventEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent editEvent = new Intent(EventActivity.this, EditEventActivity.class);
                    editEvent.putExtra("event",event);
                    startActivity(editEvent);
                }
            });
        } else{
            fabEventEdit.setVisibility(View.GONE);
        }

        fabEventDelete = (FloatingActionButton) findViewById(R.id.fabEventDelete);
        fabEventDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabEventDelete.setEnabled(false);

                boolean isSuccessful = dbHandler.deleteEvent(event);
                if (isSuccessful) {
                    Log.i("event", "Event Deleted successfuly");
                } else {
                    Log.e("event", "Event Not edited");
                }

                new DeleteEventTask().execute();
            }
        });

        btnShowMore = (Button) findViewById(R.id.btnShowMore);
        listContacts = (ListView) findViewById(R.id.listContacts);
    }

    private class DeleteEventTask extends AsyncTask<String, Void, Boolean> {
        boolean result = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            // Request - send the event as json to the server for insertion
            Gson gson = new Gson();
            String eventJSON = gson.toJson(event, Event.class);
            URL url = null;
            try {
                url = new URL(ApplicationConstants.DELETE_EVENT_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "text/plain");
                con.setRequestProperty("Accept", "text/plain");
                con.setRequestMethod("POST");

                OutputStream os = con.getOutputStream();
                os.write(eventJSON.getBytes("UTF-8"));
                os.flush();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // Response
                StringBuilder response = new StringBuilder();
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    response.append(line + "\n");
                }

                input.close();

                con.disconnect();

                if (response.toString().trim().equals("OK")) {
                    result = true;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (result) {
                Toast.makeText(EventActivity.this, getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                EventActivity.this.finish();
                Intent events = new Intent(EventActivity.this, CalendarActivity.class);
                startActivity(events);
            } else {
                fabEventDelete.setEnabled(true);
            }
        }
    }
}
