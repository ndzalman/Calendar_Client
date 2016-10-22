package com.calendar_client.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
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
import java.util.Calendar;

public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = "NEW_EVENT";
    private EditText etEventTitle;
    private TextView tvDateStart;
    private TextView tvTimeStart;
    private TextView tvDateEnd;
    private TextView tvTimeEnd;
    private EditText etDescription;
    private FloatingActionButton fabDone;
    private int yearStart, monthStart, dayStart;
    private int hourStart, minuteStart;
    private int yearEnd, monthEnd, dayEnd;
    private int hourEnd, minuteEnd;
    private Event event;
    private boolean isNew = true;
    private EventsDBHandler dbHandler;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        initComponents();
        final Calendar c = Calendar.getInstance();
        dbHandler = new EventsDBHandler(this);


        if (getIntent().getSerializableExtra("event")!= null){
            event = (Event) getIntent().getSerializableExtra("event");
            isNew = false;
        }
        if (isNew){
            yearStart = c.get(Calendar.YEAR);
            monthStart = c.get(Calendar.MONTH);
            dayStart = c.get(Calendar.DAY_OF_MONTH);
            hourStart = c.get(Calendar.HOUR_OF_DAY);
            minuteStart = c.get(Calendar.MINUTE);

            yearEnd = c.get(Calendar.YEAR);
            monthEnd = c.get(Calendar.MONTH);
            dayEnd = c.get(Calendar.DAY_OF_MONTH);
            hourEnd = c.get(Calendar.HOUR_OF_DAY);
            minuteEnd = c.get(Calendar.MINUTE);

        } else{ // if this is edit event

            yearStart = event.getDateStart().get(Calendar.YEAR);
            monthStart = event.getDateStart().get(Calendar.MONTH);
            dayStart = event.getDateStart().get(Calendar.DAY_OF_MONTH);
            hourStart = event.getDateStart().get(Calendar.HOUR_OF_DAY);
            minuteStart = event.getDateStart().get(Calendar.MINUTE);

            yearEnd = event.getDateEnd().get(Calendar.YEAR);
            monthEnd = event.getDateEnd().get(Calendar.MONTH);
            dayEnd = event.getDateEnd().get(Calendar.DAY_OF_MONTH);
            hourEnd = event.getDateEnd().get(Calendar.HOUR_OF_DAY);
            minuteEnd = event.getDateEnd().get(Calendar.MINUTE);

            etEventTitle.setText(event.getTitle());
            etDescription.setText(event.getDescription());
            String dateStart = dayStart + "/" + monthStart + "/" + yearEnd;
            tvDateStart.setText(dateStart);
            String dateEnd = dayEnd + "/" + monthEnd + "/" + yearEnd;
            tvDateEnd.setText(dateEnd);
            String timeStart = hourStart + ":" + minuteStart;
            tvTimeStart.setText(timeStart);
            String timeEnd = hourEnd + ":" + minuteEnd;

            tvTimeEnd.setText(timeEnd);
            tvTitle.setText(getString(R.string.new_event_edit_title));
        }


        tvDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        tvDateStart.setText(day + "/" + (month + 1) + "/" + year);
                        yearStart = year;
                        monthStart = month;
                        dayStart = day;
                    }
                }, yearStart, monthStart, dayStart);

                datePickerDialog.show();
            }
        });

        tvDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        tvDateEnd.setText(day + "/" + (month + 1) + "/" + year);
                        yearEnd = year;
                        monthEnd = month;
                        dayEnd = day;
                    }
                }, yearEnd, monthEnd, dayEnd);

                datePickerDialog.show();
            }
        });


        tvTimeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        tvTimeStart.setText(selectedHour + ":" + selectedMinute);
                        hourStart = selectedHour;
                        minuteStart = selectedMinute;
                    }
                }, hourStart, minuteStart, true);
                //timePickerDialog.setTitle("");
                timePickerDialog.show();
            }
        });

        tvTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        tvTimeEnd.setText(selectedHour + ":" + selectedMinute);
                        hourEnd = selectedHour;
                        minuteEnd = selectedMinute;
                    }
                }, hourEnd, minuteEnd, true);
                //timePickerDialog.setTitle("");
                timePickerDialog.show();
            }
        });

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etEventTitle.getText().toString();
                String dateStartTxt = tvDateStart.getText().toString();
                String timeStartTxt = tvTimeStart.getText().toString();
                String dateEndTxt = tvDateEnd.getText().toString();
                String timeEndTxt = tvTimeEnd.getText().toString();
                String description = etDescription.getText().toString();

                if (title.isEmpty() || dateStartTxt.isEmpty() || timeStartTxt.isEmpty() ||
                        dateEndTxt.isEmpty() || timeEndTxt.isEmpty() || description.isEmpty()){
                    Toast.makeText(NewEventActivity.this,getString(R.string.new_event_empty),Toast.LENGTH_SHORT).show();
                    return;
                }

                event = new Event();
                event.setDescription(etDescription.getText().toString());
                event.setTitle(etEventTitle.getText().toString());
                Calendar dateStart = Calendar.getInstance();
                dateStart.set(Calendar.YEAR,yearStart);
                dateStart.set(Calendar.MONTH,monthStart);
                dateStart.set(Calendar.DAY_OF_MONTH,dayStart);
                dateStart.set(Calendar.HOUR_OF_DAY,hourStart);
                dateStart.set(Calendar.MINUTE,minuteStart);
                event.setDateStart(dateStart);
                Calendar dateEnd = Calendar.getInstance();
                dateEnd.set(Calendar.YEAR,yearEnd);
                dateEnd.set(Calendar.MONTH,monthEnd);
                dateEnd.set(Calendar.DAY_OF_MONTH,dayEnd);
                dateEnd.set(Calendar.HOUR_OF_DAY,hourEnd);
                dateEnd.set(Calendar.MINUTE,minuteEnd);
                event.setDateEnd(dateEnd);

                if (isNew) {

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String user = sharedPreferences.getString("user", "");
                    Gson gson = new Gson();
                    User thisUser = gson.fromJson(user, User.class);
                    event.setUser(thisUser);

                    boolean isSuccessful = dbHandler.addEvent(event);
                    if (isSuccessful) {
                        Log.i(TAG, "Event added successfuly");
                    } else {
                        Log.e(TAG, "Event Not added");
                    }

                    new AddEventTask().execute();
                } else{

                    boolean isSuccessful = dbHandler.updateEvent(event);
                    if (isSuccessful) {
                        Log.i(TAG, "Event Edited successfuly");
                    } else {
                        Log.e(TAG, "Event Not edited");
                    }

                    new EditEventTask().execute();

                }
            }
        });

    }

    private void initComponents() {
        etEventTitle = (EditText) findViewById(R.id.etEventTitle);
        tvDateStart = (TextView) findViewById(R.id.tvDateStart);
        tvTimeStart = (TextView) findViewById(R.id.tvTimeStart);
        tvDateEnd = (TextView) findViewById(R.id.tvDateEnd);
        tvTimeEnd = (TextView) findViewById(R.id.tvTimeEnd);
        etDescription = (EditText) findViewById(R.id.etDescription);
        fabDone = (FloatingActionButton) findViewById(R.id.fabDone);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

    }

    private class AddEventTask extends AsyncTask<String,Void,Boolean> {
        boolean result = false;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... strings) {


            // Request - send the customer as json to the server for insertion
            Gson gson = new Gson();
            String eventJSON = gson.toJson(event, Event.class);
            URL url = null;
            try {
                url = new URL(ApplicationConstants.ADD_EVENT_URL);
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
                finish();
                Intent eventsIntent = new Intent(NewEventActivity.this,EventsActivity.class);
                startActivity(eventsIntent);
            }
        }
    }

    private class EditEventTask extends AsyncTask<String,Void,Boolean> {
        boolean result = false;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... strings) {


            // Request - send the customer as json to the server for insertion
            Gson gson = new Gson();
            String eventJSON = gson.toJson(event, Event.class);
            URL url = null;
            try {
                url = new URL(ApplicationConstants.EDIT_EVENT_URL);
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
                finish();
                Intent eventsIntent = new Intent(NewEventActivity.this,EventsActivity.class);
                startActivity(eventsIntent);
            }
        }
    }

}
