package com.calendar_client;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.data.User;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignUpActivity extends AppCompatActivity {

    private int year, month, day;

    @InjectView(R.id.etUserName)
    EditText etUserName;
    @InjectView(R.id.etEmail)
    EditText etEmail;
    @InjectView(R.id.etPassword)
    EditText etPassword;
    @InjectView(R.id.etConfirmPassword)
    EditText etConfirmPassword;
    @InjectView(R.id.tvDateOfBirth)
    TextView tvDateOfBirth;
    @InjectView(R.id.btnSignUp)
    Button btnSignUp;
    @InjectView(R.id.tvLoginLink)
    TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);
        getSupportActionBar().hide();

        tvDateOfBirth = (TextView) findViewById(R.id.tvDateOfBirth);
        tvDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        tvDateOfBirth.setText(day + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);

                datePickerDialog.show();

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()){
                    Toast.makeText
                            (SignUpActivity.this,getString(R.string.sign_up_successful),Toast.LENGTH_SHORT).show();
                }
                User user = new User();
                user.setEmail(etEmail.getText().toString());
                Calendar dateOfBirth = Calendar.getInstance();
                dateOfBirth.set(Calendar.YEAR,year);
                dateOfBirth.set(Calendar.MONTH,month);
                dateOfBirth.set(Calendar.DAY_OF_MONTH,day);
                user.setDateOfBirth(dateOfBirth);
                user.setPassword(etPassword.getText().toString());
                user.setUserName(etUserName.getText().toString());
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    public boolean validate() {
        boolean valid = true;

        String userName = etUserName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String date = tvDateOfBirth.getText().toString();


        // user name validation
        if (userName.isEmpty()) {
            etUserName.setError(getString(R.string.sign_up_username_error_empty));
            valid = false;
        } else if (userName.length() < 5) {
            etUserName.setError(getString(R.string.sign_up_username_error_length));
            valid = false;
        } else {
            etUserName.setError(null);
        }

        // email validation
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.sign_up_email_error_empty));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.sign_up_email_error_invalid));
            valid = false;
        } else {
            etEmail.setError(null);
        }

        // password validation
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.sign_up_password_error_empty));
            valid = false;
        } else if (password.length() < 5) {
            etPassword.setError(getString(R.string.sign_up_password_error_length));
            valid = false;
        } else {
            etPassword.setError(null);
        }

        // confirm password validation
        if (!confirmPassword.equals(password)) {
            etConfirmPassword.setError(getString(R.string.sign_up_confirm_password_error_no_match));
            valid = false;
        }else {
            etConfirmPassword.setError(null);
        }

        // date validation
        if (date.equals(getString(R.string.sign_up_date))) {
            tvDateOfBirth.setError(getString(R.string.sign_up_date_empty));
            valid = false;
        }else {
            tvDateOfBirth.setError(null);
        }

        return valid;
    }


}
