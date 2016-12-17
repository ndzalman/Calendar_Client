package com.calendar_client.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.PermissionChecker;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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

import static android.app.Activity.RESULT_OK;


public class EventDetailsFragment extends Fragment {
    private static final String TAG = "NEW_EVENT";

    // Edit Event
    private EditText etEventTitle;
    private TextView tvDateStart;
    private TextView tvTimeStart;
    private TextView tvDateEnd;
    private TextView tvTimeEnd;
    private TextView tvPickLocation;
    private TextView tvTitle;
    private EditText etDescription;
    private FloatingActionButton fabDone;

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
    private boolean alarmPermissionGranted = false;
    private boolean locationPermissionGranted = false;

    public static final int ALARM_PERMISSION_REQUEST = 1;
    public static final int LOCATION_PERMISSION_REQUEST = 2;
    public static final int PLACE_PICKER_REQUEST = 1;

    private GoogleApiClient mGoogleApiClient;
    private Activity activity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.event_details_layout, container, false);
        activity = getActivity();

//        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                .getCurrentPlace(mGoogleApiClient, null);
//        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//            @Override
//            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
//                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
//                            placeLikelihood.getPlace().getName(),
//                            placeLikelihood.getLikelihood()));
//                }
//                likelyPlaces.release();
//            }
//        });

        c = Calendar.getInstance();
        dbHandler = new EventsDBHandler(getContext());
        event = new Event();

        // if i get selected day on intent - add event of specific day
        if (getActivity().getIntent().getSerializableExtra("selectedDay") != null) {
            selected = (Calendar) getActivity().getIntent().getSerializableExtra("selectedDay");

            // if i get event - edit event
        } else if (getActivity().getIntent().getSerializableExtra("event") != null) {
            event = (Event) getActivity().getIntent().getSerializableExtra("event");
            isNew = false;
        }
        InitEditViewComponents();

        checkLocationPermission();
        if (locationPermissionGranted) {
            initGoogleLocation();
        }

        return view;
    }

    private void InitEditViewComponents() {
        initEditLayoutComponents();
        initEventDetails();
        Data data = Data.getInstance();
        initDateAndTimePicker();
    }

    private void initEventDetails() {

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

//            autocompleteFragment.setText(event.getLocation());
            if (!tvPickLocation.getText().equals(getString(R.string.new_event_location_hint))) {
                tvPickLocation.setText(event.getLocation());
            }

            tvTimeEnd.setText(timeEnd);
            tvTitle.setText(getResources().getString(R.string.new_event_edit_title));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
                tvPickLocation.setText(place.getName());
            }
        }
    }

    private void initDateAndTimePicker() {

        tvDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                        if (c.get(Calendar.YEAR) > year || c.get(Calendar.MONTH) > month || c.get(Calendar.DAY_OF_MONTH) > day) {
//                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_date_error), Toast.LENGTH_SHORT).show();
//                        } else {
                        tvDateStart.setText(day + "/" + (month + 1) + "/" + year);
                        yearStart = year;
                        monthStart = month;
                        dayStart = day;
//                        }
                    }
                }, yearStart, monthStart, dayStart);

                if (Build.VERSION.SDK_INT < 21) {
                    datePickerDialog.getDatePicker().getCalendarView().setFirstDayOfWeek(Calendar.SUNDAY);
                } else {
                    datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
                }
                datePickerDialog.show();
            }
        });

        tvDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        if (year < yearStart ||
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

                if (Build.VERSION.SDK_INT < 21) {
                    datePickerDialog.getDatePicker().getCalendarView().setFirstDayOfWeek(Calendar.SUNDAY);
                } else {
                    datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
                }
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
        stackBuilder.addParentStack(UpComingEventsActivity.class);
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
                dateEndTxt.isEmpty() || timeEndTxt.isEmpty()) {
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

            if (!tvPickLocation.getText().equals(getString(R.string.new_event_location_hint))) {
                event.setLocation(tvPickLocation.getText().toString());
            } else{
                event.setLocation("");
            }


            return true;
        }
    }

    //link ui components
    private void initEditLayoutComponents() {
        etEventTitle = (EditText) view.findViewById(R.id.etEventTitle);
        tvDateStart = (TextView) view.findViewById(R.id.tvDateStart);
        tvTimeStart = (TextView) view.findViewById(R.id.tvTimeStart);
        tvDateEnd = (TextView) view.findViewById(R.id.tvDateEnd);
        tvTimeEnd = (TextView) view.findViewById(R.id.tvTimeEnd);
        etDescription = (EditText) view.findViewById(R.id.etDescription);
        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvPickLocation = (TextView) view.findViewById(R.id.tvPickLocation);

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = saveEvent();
                if (isValid) {

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    String user = sharedPreferences.getString("user", "");
                    Gson gson = new Gson();
                    User thisUser = gson.fromJson(user, User.class);
                    event.addUser(thisUser); //the current user
                    Data data = Data.getInstance();
                    event.getUsers().addAll(data.getUsers());
                    event.setOwnerId(thisUser.getId());

                    data.getUsers().clear();

                    Log.d("USERS", event.getUsers().toString());
                    Log.d("USERS", "size " + event.getUsers().size());


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
    }

    private void initGoogleLocation() {
        tvPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
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
                User user = gson.fromJson(userJSON, User.class);


                event.setId(id);
                boolean isSuccessful = dbHandler.addEvent(event, user.getId());
                if (isSuccessful) {
                    Log.i(TAG, "Event added successfuly");
                    checkSetAlaramPermission();
                    if (alarmPermissionGranted) {
                        scheduleNotification();
                    }
                } else {
                    Log.e(TAG, "Event Not added");
                }

                Data data = Data.getInstance();
                data.getSharedEvents().add(event);

                getActivity().finish();
                Intent events = new Intent(getActivity(), CalendarActivity.class);
                startActivity(events);
            } else {
                fabDone.setEnabled(true);
            }
        }
    }


    private void checkSetAlaramPermission() {
        int permission;
        if (Build.VERSION.SDK_INT < 23) {
            permission = PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.SET_ALARM);
            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                alarmPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SET_ALARM
                }, ALARM_PERMISSION_REQUEST);
            }
        } else { //api 23 and above
            permission = getActivity().checkSelfPermission(Manifest.permission.SET_ALARM);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        new String[]{Manifest.permission.SET_ALARM},
                        ALARM_PERMISSION_REQUEST);
            } else {
                alarmPermissionGranted = true;
            }
        }
    }

    private void checkLocationPermission() {
        int permission;
        if (Build.VERSION.SDK_INT < 23) {
            permission = PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                }, LOCATION_PERMISSION_REQUEST);
            }
        } else { //api 23 and above
            permission = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            } else {
                locationPermissionGranted = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ALARM_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scheduleNotification();

                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(),
                            "We couldn't schedule notifcation for your event. please approve set alram",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }
            case LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    initGoogleLocation();

                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(),
                            "We couldn't add location for your event. please approve location permission",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }
            default:
                break;

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
                Data data = Data.getInstance();
                data.getSharedEvents().remove(event);
                data.getSharedEvents().add(event);

                getActivity().finish();
                Intent events = new Intent(getActivity(), CalendarActivity.class);
                startActivity(events);
            } else {
                fabDone.setEnabled(true);
            }
        }
    }


}
