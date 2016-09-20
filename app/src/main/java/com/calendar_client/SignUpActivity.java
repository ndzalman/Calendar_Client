package com.calendar_client;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    private TextView tvDateOfBirth;
    private DatePicker datePicker;
    private int year,month,day;
    private final static int DATE_DIALOG_ID = 999;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        tvDateOfBirth = (TextView) findViewById(R.id.tvDateOfBirth);
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        datePicker.init(year, month, day, null);

        tvDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });


        //date picker
         datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                showDate(year, monthOfYear + 1, dayOfMonth);

                datePicker.init(year,month,day,null);
            }},year,month,day);


    }



    private void showDate(int year,int month, int day){
        tvDateOfBirth.setText
                (new StringBuilder().append(day).append("/").append(month).append("/").append(year));
    }
}
