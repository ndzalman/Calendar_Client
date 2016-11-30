package com.calendar_client.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBHandler;
import com.calendar_client.utils.NotificationReceiver;
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

public class EventDetailsFragement extends Fragment {
    private static final String TAG = "NEW_EVENT";
    private EditText etEventTitle;
    private TextView tvDateStart;
    private TextView tvTimeStart;
    private TextView tvDateEnd;
    private TextView tvTimeEnd;
    private TextView tvTitle;
    private EditText etDescription;
    private FloatingActionButton fabDone;
    private FloatingActionButton fabDelete;

    private int yearStart, monthStart, dayStart;
    private int hourStart, minuteStart;
    private int yearEnd, monthEnd, dayEnd;
    private int hourEnd, minuteEnd;
    private Event event;
    private boolean isNew = true;
    private EventsDBHandler dbHandler;
    private Calendar selected;
    private Calendar c;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.event_details_layout, container, false);


        initComponents();
        c = Calendar.getInstance();
        dbHandler = new EventsDBHandler(getContext());
        event = new Event();

        // TODO - get the extra paratmer event id from intent
        // usng it - go to server and fetch all details
        // if i get selected day on intent - add event of specific day
        if (getActivity().getIntent().getSerializableExtra("selectedDay") != null) {
            selected = (Calendar) getActivity().getIntent().getSerializableExtra("selectedDay");

            // if i get event - edit event
        } else if (getActivity().getIntent().getSerializableExtra("event") != null) {
            event = (Event) getActivity().getIntent().getSerializableExtra("event");
            isNew = false;
        }


        if (isNew) {
            //set selected date start
            event.setDateStart(selected);
            //set selected date end
            event.setDateEnd(selected);

            yearStart = selected.get(Calendar.YEAR);
            monthStart = selected.get(Calendar.MONTH);
            dayStart = selected.get(Calendar.DAY_OF_MONTH);
            hourStart = c.get(Calendar.HOUR_OF_DAY);
            minuteStart = c.get(Calendar.MINUTE);

            tvDateStart.setText(selected.get(Calendar.DAY_OF_MONTH) + "/" + (selected.get(Calendar.MONTH) + 1) + "/" + selected.get(Calendar.YEAR));
            if (minuteStart < 10) {
                tvTimeStart.setText(hourStart + ":0" + minuteStart);
            } else {
                tvTimeStart.setText(hourStart + ":" + minuteStart);
            }

            yearEnd = selected.get(Calendar.YEAR);
            monthEnd = selected.get(Calendar.MONTH);
            dayEnd = selected.get(Calendar.DAY_OF_MONTH);
            hourEnd = c.get(Calendar.HOUR_OF_DAY);
            minuteEnd = c.get(Calendar.MINUTE);
            tvDateEnd.setText(selected.get(Calendar.DAY_OF_MONTH) + "/" + (selected.get(Calendar.MONTH) + 1) + "/" + selected.get(Calendar.YEAR));
            if (minuteEnd < 10) {
                tvTimeEnd.setText(hourEnd + ":0" + minuteEnd);
            } else {
                tvTimeEnd.setText(hourEnd + ":" + minuteEnd);
            }


        } else { // if this is edit event

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
            String dateStart = dayStart + "/" + (monthStart + 1) + "/" + yearEnd;
            tvDateStart.setText(dateStart);
            String dateEnd = dayEnd + "/" + (monthEnd + 1) + "/" + yearEnd;
            tvDateEnd.setText(dateEnd);
            String timeStart = hourStart + ":" + minuteStart;
            tvTimeStart.setText(timeStart);
            String timeEnd = hourEnd + ":" + minuteEnd;

            tvTimeEnd.setText(timeEnd);
            tvTitle.setText(getResources().getString(R.string.new_event_edit_title));

            fabDelete = (FloatingActionButton) view.findViewById(R.id.fabDelete);
            fabDelete.setVisibility(View.VISIBLE);

            fabDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fabDelete.setEnabled(false);

                    boolean isSuccessful = dbHandler.deleteEvent(event);
                    if (isSuccessful) {
                        Log.i(TAG, "Event Deleted successfuly");
                    } else {
                        Log.e(TAG, "Event Not edited");
                    }

                    new DeleteEventTask().execute();
                }
            });

        }

        initDateAndTimePicker();

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (isNew) {
//                    event = new Event();
//                }
                boolean isValid = saveEvent();
                if (isValid) {

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    String user = sharedPreferences.getString("user", "");
                    Gson gson = new Gson();
                    User thisUser = gson.fromJson(user, User.class);
                    event.addUser(thisUser); //the current user
                    Data data = Data.getInstance();
                    event.getUsers().addAll(data.getUsers());

                    // if this is new event execute the add event task (in post execute we update
                    // the SQLite as well)
                    if (isNew) {
                        fabDone.setEnabled(false);
                        new AddEventTask().execute();

                    } else {
                        // if we editing existing event we update the SQLite and execute the edit
                        // event task
                        fabDone.setEnabled(false);
                        boolean isSuccessful = dbHandler.updateEvent(event);
                        if (isSuccessful) {
                            Log.i(TAG, "Event Edited successfuly");
                        } else {
                            Log.e(TAG, "Event Not edited");
                        }

                        new EditEventTask().execute();
                    }
                }
            }
        });

        return view;
    }

    private void initDateAndTimePicker() {

        tvDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        if (c.get(Calendar.YEAR) > year || c.get(Calendar.MONTH) > month || c.get(Calendar.DAY_OF_MONTH) > day) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_date_error), Toast.LENGTH_SHORT).show();
                        } else {
                            tvDateStart.setText(day + "/" + (month + 1) + "/" + year);
                            yearStart = year;
                            monthStart = month;
                            dayStart = day;
                        }
                    }
                }, yearStart, monthStart, dayStart);

                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
                datePickerDialog.show();
            }
        });

        tvDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        if (c.get(Calendar.YEAR) > year || c.get(Calendar.YEAR) < yearStart ||
                                (c.get(Calendar.YEAR) == yearStart && month < monthStart) || (month == monthStart && day < dayStart)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_date_error), Toast.LENGTH_SHORT).show();
                        } else {
                            tvDateEnd.setText(day + "/" + (month + 1) + "/" + year);
                            yearEnd = year;
                            monthEnd = month;
                            dayEnd = day;
                        }
                    }
                }, yearEnd, monthEnd, dayEnd);

                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
                datePickerDialog.show();
            }
        });


        tvTimeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if ((selectedHour < c.get(Calendar.HOUR_OF_DAY) && c.get(Calendar.DAY_OF_MONTH) == dayStart ||
                                (selectedMinute < c.get(Calendar.MINUTE)) && selectedHour == c.get(Calendar.HOUR_OF_DAY))
                                ) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_time_error), Toast.LENGTH_SHORT).show();
                        } else {
                            hourStart = selectedHour;
                            minuteStart = selectedMinute;
                            if (minuteStart < 10) {
                                tvTimeStart.setText(hourStart + ":0" + minuteStart);
                            } else {
                                tvTimeStart.setText(hourStart + ":" + minuteStart);
                            }
                            hourStart = selectedHour;

                        }
                    }
                }, hourStart, minuteStart, true);
                //timePickerDialog.setTitle("");
                timePickerDialog.show();
            }
        });

        tvTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if ((selectedHour < hourStart && dayEnd == dayStart) ||
                                (selectedMinute < minuteStart && selectedHour == hourStart)
                                ) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_time_error), Toast.LENGTH_SHORT).show();
                        } else {
                            hourEnd = selectedHour;
                            minuteEnd = selectedMinute;
                            if (minuteEnd < 10) {
                                tvTimeEnd.setText(hourEnd + ":0" + minuteEnd);
                            } else {
                                tvTimeEnd.setText(hourEnd + ":" + minuteEnd);
                            }
                            hourEnd = selectedHour;
                        }
                    }
                }, hourEnd, minuteEnd, true);
                //timePickerDialog.setTitle("");
                timePickerDialog.show();
            }
        });
    }

    private void scheduleNotification() {
        Intent notificationIntent = new Intent(getActivity(), NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, event.getId());
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, getNotification());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, event.getDateStart().getTimeInMillis(), pendingIntent);
    }

    private void updateNotifcation() {

    }

    private void deleteNotifcation() {

    }

    private Notification getNotification() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(event.getTitle())
                        .setContentText(event.getDateStart().get(Calendar.HOUR_OF_DAY) + ":" + event.getDateStart().get(Calendar.MINUTE)
                                + " - " + event.getDateEnd().get(Calendar.HOUR_OF_DAY) + ":" + event.getDateEnd().get(Calendar.MINUTE)
                        )
                        .setAutoCancel(true)
                        .setSound(uri);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getActivity(), CalendarActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
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

    private boolean saveEvent() {
        String title = etEventTitle.getText().toString();
        String dateStartTxt = tvDateStart.getText().toString();
        String timeStartTxt = tvTimeStart.getText().toString();
        String dateEndTxt = tvDateEnd.getText().toString();
        String timeEndTxt = tvTimeEnd.getText().toString();
        String description = etDescription.getText().toString();

        if (title.isEmpty() || dateStartTxt.isEmpty() || timeStartTxt.isEmpty() ||
                dateEndTxt.isEmpty() || timeEndTxt.isEmpty() || description.isEmpty()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else {

            event.setDescription(etDescription.getText().toString());
            event.setTitle(etEventTitle.getText().toString());
            Calendar dateStart = Calendar.getInstance();
            dateStart.set(Calendar.YEAR, yearStart);
            dateStart.set(Calendar.MONTH, monthStart);
            dateStart.set(Calendar.DAY_OF_MONTH, dayStart);
            dateStart.set(Calendar.HOUR_OF_DAY, hourStart);
            dateStart.set(Calendar.MINUTE, minuteStart);
            event.setDateStart(dateStart);
            Calendar dateEnd = Calendar.getInstance();
            dateEnd.set(Calendar.YEAR, yearEnd);
            dateEnd.set(Calendar.MONTH, monthEnd);
            dateEnd.set(Calendar.DAY_OF_MONTH, dayEnd);
            dateEnd.set(Calendar.HOUR_OF_DAY, hourEnd);
            dateEnd.set(Calendar.MINUTE, minuteEnd);
            event.setDateEnd(dateEnd);

            return true;
        }
    }

    //link ui components
    private void initComponents() {
        etEventTitle = (EditText) view.findViewById(R.id.etEventTitle);
        tvDateStart = (TextView) view.findViewById(R.id.tvDateStart);
        tvTimeStart = (TextView) view.findViewById(R.id.tvTimeStart);
        tvDateEnd = (TextView) view.findViewById(R.id.tvDateEnd);
        tvTimeEnd = (TextView) view.findViewById(R.id.tvTimeEnd);
        etDescription = (EditText) view.findViewById(R.id.etDescription);
        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);

    }


    private class AddEventTask extends AsyncTask<String, Void, Boolean> {
        boolean result = true;
        int id;

        @Override
        protected Boolean doInBackground(String... strings) {
            // Request - send the event as json to the server for insertion
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

                if (response.toString().trim().equals("-1")) {
                    result = false;
                } else {
                    id = Integer.parseInt(response.toString().trim());
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
            // if result is true - the insertion went well, set the id and add the event
            // in the SQLite and schedule notfication
            if (result) {
                // get user from shared preference. if doesnt exist return empty string
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String userJSON = sharedPreferences.getString("user", "");
                Gson gson = new Gson();
                User user = gson.fromJson(userJSON,User.class);


                event.setId(id);
                boolean isSuccessful = dbHandler.addEvent(event,user.getId());
                if (isSuccessful) {
                    Log.i(TAG, "Event added successfuly");
                    scheduleNotification();
                } else {
                    Log.e(TAG, "Event Not added");
                }
                getActivity().finish();
                Intent events = new Intent(getActivity(), CalendarActivity.class);
                startActivity(events);
            } else {
                fabDone.setEnabled(true);
            }
        }
    }

    private class EditEventTask extends AsyncTask<String, Void, Boolean> {
        boolean result = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            // Request - send the event as json to the server for insertion
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
                getActivity().finish();
                Intent events = new Intent(getActivity(), CalendarActivity.class);
                startActivity(events);
            } else {
                fabDone.setEnabled(true);
            }
        }
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
                Toast.makeText(getActivity(), getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                getActivity().finish();
                Intent events = new Intent(getActivity(), CalendarActivity.class);
                startActivity(events);
            } else {
                fabDelete.setEnabled(true);
            }
        }
    }

}
