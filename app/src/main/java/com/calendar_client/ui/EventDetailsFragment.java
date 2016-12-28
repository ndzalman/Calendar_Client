package com.calendar_client.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.Reminder;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBHandler;
import com.calendar_client.utils.NotificationAlarmReceiver;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
//import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;


public class EventDetailsFragment extends Fragment {
    private static final String TAG = "NEW_EVENT";
    private static final int NO_REMINDER = -2;

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
    private TextView tvAddPicture;
    private CheckBox chkboxReminder;
    private Spinner spinnerReminder;

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
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Activity activity;
    private Calendar oldStartId;

    private DatePickerDialog dpdStart;
    private DatePickerDialog dpdEnd;

    private LatLngBounds selectedPlace;

    private ImageView ivPicture;
    private byte[] picture;

    private boolean reminderChanged = false;

    private final static int PICK_IMAGE_REQUEST = 1235;

    Bitmap selectedImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.event_details_layout, container, false);
        activity = getActivity();

//        PendingResult<PlaceLikelihoodBuffeor> result = Places.PlaceDetectionApi
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

            if (event.getReminder() != NO_REMINDER){
                chkboxReminder.setChecked(true);
                if (event.getReminder() == Integer.parseInt(getString(R.string.reminder_on_time_int))){
                    spinnerReminder.setSelection(0);
                } else if (event.getReminder() == Integer.parseInt(getString(R.string.reminder_10_min_before_int))){
                    spinnerReminder.setSelection(1);
                } else if (event.getReminder() == Integer.parseInt(getString(R.string.reminder_one_day_before_int))){
                    spinnerReminder.setSelection(2);
                }
            }

            if (event.getImage() != null){
                Bitmap b = BitmapFactory.decodeByteArray(event.getImage(),0,event.getImage().length);
                selectedImage = b;
                ivPicture.setImageBitmap(b);
                ivPicture.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                selectedPlace = place.getViewport();
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_LONG).show();
                tvPickLocation.setText(place.getName());
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                selectedImage = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath), 400, 600, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            picture = null;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if( selectedImage != null ) {
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                picture = stream.toByteArray();
                ivPicture.setImageBitmap(selectedImage);
                ivPicture.setVisibility(View.VISIBLE);
            }
//            try {
////                selectedImage = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath), 500, 500, true);
//                ivPicture.setImageBitmap(selectedImage);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }




    }

    private void initDateAndTimePicker() {

        dpdStart = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                        if (c.get(Calendar.YEAR) > year || c.get(Calendar.MONTH) > month || c.get(Calendar.DAY_OF_MONTH) > day) {
//                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_date_error), Toast.LENGTH_SHORT).show();
//                        } else {
                tvDateStart.setText(day + "/" + (month + 1) + "/" + year);
                yearStart = year;
                monthStart = month;
                dayStart = day;
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, day);


                dpdEnd.getDatePicker().setMinDate(selectedDate.getTime().getTime());
//                        }
            }
        }, yearStart, monthStart, dayStart);
        dpdStart.getDatePicker().setMinDate(new Date().getTime());
        dpdStart.setTitle("");
        if (Build.VERSION.SDK_INT < 21) {
            dpdStart.getDatePicker().getCalendarView().setFirstDayOfWeek(Calendar.SUNDAY);
        } else {
            dpdStart.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
        }
        tvDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dpdStart.show();
            }
        });

        dpdEnd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
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
        dpdEnd.getDatePicker().setMinDate(new Date().getTime());
        dpdEnd.setTitle("");
        if (Build.VERSION.SDK_INT < 21) {
            dpdEnd.getDatePicker().getCalendarView().setFirstDayOfWeek(Calendar.SUNDAY);
        } else {
            dpdEnd.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
        }
        tvDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpdEnd.show();
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
                        } else if (selectedHour < hourStart || (selectedHour == hourStart && selectedMinute < minuteStart)){
                            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_time_error), Toast.LENGTH_SHORT).show();
                        }
                        else {
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
//        ComponentName receiver = new ComponentName(getActivity(), NotificationAlarmReceiver.class);
//        PackageManager pm = getActivity().getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);

        int id = (int) event.getDateStart().getTimeInMillis();
        Log.e("schedule", "id is " + id);
//        Intent notificationIntent = new Intent(getActivity().getApplicationContext(), NotificationAlarmReceiver.class);
//        notificationIntent.putExtra(NOTIFICATION_ID,((int)event.getDateStart().getTimeInMillis()));
//        notificationIntent.putExtra(NOTIFICATION,(getNotification()));
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), (int)event.getDateStart().getTimeInMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long reminder = TimeUnit.MINUTES.toMillis(event.getReminder());
        Log.e("ALARAM","reminder: " + reminder);
        long when = event.getDateStart().getTimeInMillis() + reminder;

        AlarmManager am = (AlarmManager) getActivity().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity().getBaseContext(), NotificationAlarmReceiver.class);
        intent.putExtra("event", event);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getBaseContext(), id, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    private void updateNotification(Calendar oldStart) {
        int id = (int) oldStart.getTimeInMillis();

        AlarmManager am = (AlarmManager) getActivity().getBaseContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity().getBaseContext(), NotificationAlarmReceiver.class);
        intent.putExtra("event", event);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getBaseContext(), id, intent, 0);
        am.cancel(pendingIntent);

        id = (int) event.getDateStart().getTimeInMillis();
        long reminder = TimeUnit.MINUTES.toMillis(event.getReminder());
        Log.e("ALARAM","reminder: " + reminder);
        long when = event.getDateStart().getTimeInMillis() + reminder;
        pendingIntent = PendingIntent.getBroadcast(getActivity().getBaseContext(), id, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
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
        }
        if(hourEnd < hourStart || (hourEnd == hourStart && minuteEnd < minuteStart)){
            Toast.makeText(getActivity(), getResources().getString(R.string.new_event_date_error), Toast.LENGTH_SHORT).show();
            return false;
        } else {

            if (isNew == false) {
                oldStartId = event.getDateStart();
            }
            event.setDescription(etDescription.getText().toString());
            event.setTitle(etEventTitle.getText().toString());
            Calendar dateStart = Calendar.getInstance();
            dateStart.set(Calendar.YEAR, yearStart);
            dateStart.set(Calendar.MONTH, monthStart);
            dateStart.set(Calendar.DAY_OF_MONTH, dayStart);
            dateStart.set(Calendar.HOUR_OF_DAY, hourStart);
            dateStart.set(Calendar.MINUTE, minuteStart);
            dateStart.set(Calendar.SECOND, 0);
            dateStart.set(Calendar.MILLISECOND, 0);
            event.setDateStart(dateStart);
            Calendar dateEnd = Calendar.getInstance();
            dateEnd.set(Calendar.YEAR, yearEnd);
            dateEnd.set(Calendar.MONTH, monthEnd);
            dateEnd.set(Calendar.DAY_OF_MONTH, dayEnd);
            dateEnd.set(Calendar.HOUR_OF_DAY, hourEnd);
            dateEnd.set(Calendar.MINUTE, minuteEnd);
            dateEnd.set(Calendar.SECOND, 0);
            dateEnd.set(Calendar.MILLISECOND, 0);
            event.setDateEnd(dateEnd);
            event.setImage(picture);

            if (!tvPickLocation.getText().equals(getString(R.string.new_event_location_hint))) {
                event.setLocation(tvPickLocation.getText().toString());
            } else {
                event.setLocation("");
            }

            if (chkboxReminder.isChecked()){
                Reminder reminder = (Reminder) spinnerReminder.getSelectedItem();
                if (reminder.getReminderTime() != event.getReminder()){
                    reminderChanged = true;
                    event.setReminder(reminder.getReminderTime());
                } else{
                    reminderChanged = false;
                }
            } else{
                event.setReminder(NO_REMINDER);
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
        chkboxReminder = (CheckBox) view.findViewById(R.id.chkboxReminder);
        spinnerReminder = (Spinner) view.findViewById(R.id.spinnerReminder);
        List<Reminder> reminders = new ArrayList<>();
        Reminder reminder = new Reminder();
        reminder.setReminder(getString(R.string.reminder_on_time));
        reminder.setReminderTime(Integer.parseInt(getString(R.string.reminder_on_time_int)));
        reminders.add(reminder);
        reminder = new Reminder();
        reminder.setReminder(getString(R.string.reminder_10_min_before));
        reminder.setReminderTime(Integer.parseInt(getString(R.string.reminder_10_min_before_int)));
        reminders.add(reminder);
        reminder = new Reminder();
        reminder.setReminder(getString(R.string.reminder_one_day_before));
        reminder.setReminderTime(Integer.parseInt(getString(R.string.reminder_one_day_before_int)));
        reminders.add(reminder);

        spinnerReminder.setAdapter(new MyAdapter(getActivity(), reminders, R.layout.spinner_item_reminder));

        ivPicture = (ImageView) view.findViewById(R.id.ivPicture);
        tvAddPicture = (TextView) view.findViewById(R.id.tvAddPicture);
        tvAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), PICK_IMAGE_REQUEST);
            }
        });

        ivPicture.setClickable(true);
        ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final AlertDialog dialog = builder.create();
                LayoutInflater inflater = activity.getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.image_popup, null);
                dialog.setView(dialogLayout);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                ImageView image = (ImageView) dialogLayout.findViewById(R.id.popup_image);
                image.setImageBitmap(selectedImage);

                dialog.show();
                dialog.getWindow().setLayout(650, 800);

            }
        });

        chkboxReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    spinnerReminder.setVisibility(View.VISIBLE);
                } else{
                    spinnerReminder.setVisibility(View.INVISIBLE);
                }
            }
        });


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
                        if (reminderChanged){
                            updateNotification(oldStartId);
                        }
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

                if (selectedPlace != null) {
                    builder.setLatLngBounds(selectedPlace);
                }
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
                    if (alarmPermissionGranted && chkboxReminder.isChecked()) {
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

    public class MyAdapter extends BaseAdapter {

        Context context;
        int resource;
        List<Reminder> reminders;


        public MyAdapter(Context context, List<Reminder> drinks, int resource) {
            this.context = context;
            this.resource = resource;
            this.reminders = drinks;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @Override
        public int getCount() {
            return reminders.size();
        }

        @Override
        public Object getItem(int position) {
            return reminders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return reminders.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tvReminder;
            final Reminder reminder = reminders.get(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, parent, false);
                tvReminder = (TextView) convertView.findViewById(R.id.tvReminder);
                convertView.setTag(tvReminder);

            } else {
                tvReminder = (TextView) convertView.getTag();
            }

            tvReminder.setText(reminder.getReminder());

            return convertView;
        }

    }

}
