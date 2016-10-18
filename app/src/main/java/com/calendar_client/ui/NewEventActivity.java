package com.calendar_client.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import com.calendar_client.utils.EventsDBHandler;

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

    EventsDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        initComponents();
        final Calendar c = Calendar.getInstance();
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

        dbHandler = new EventsDBHandler(this);

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

                Event newEvent = new Event();
                newEvent.setDescription(etDescription.getText().toString());
                newEvent.setTitle(etEventTitle.getText().toString());
                Calendar dateStart = Calendar.getInstance();
                dateStart.set(Calendar.YEAR,yearStart);
                dateStart.set(Calendar.MONTH,monthStart);
                dateStart.set(Calendar.DAY_OF_MONTH,dayStart);
                dateStart.set(Calendar.HOUR_OF_DAY,hourStart);
                dateStart.set(Calendar.MINUTE,minuteStart);
                newEvent.setDateStart(dateStart);
                Calendar dateEnd = Calendar.getInstance();
                dateEnd.set(Calendar.YEAR,yearEnd);
                dateEnd.set(Calendar.MONTH,monthEnd);
                dateEnd.set(Calendar.DAY_OF_MONTH,dayEnd);
                dateEnd.set(Calendar.HOUR_OF_DAY,hourEnd);
                dateEnd.set(Calendar.MINUTE,minuteEnd);
                newEvent.setDateEnd(dateEnd);

                boolean isSuccessful = dbHandler.addEvent(newEvent);
                if(isSuccessful){
                    Log.i(TAG,"Event added successfuly");
                }else{
                    Log.e(TAG,"Event Not added");
                }

                Toast.makeText(NewEventActivity.this,newEvent.toString(),Toast.LENGTH_LONG).show();
                Intent eventsIntent = new Intent(NewEventActivity.this,EventsActivity.class);
                startActivity(eventsIntent);
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

    }
}
