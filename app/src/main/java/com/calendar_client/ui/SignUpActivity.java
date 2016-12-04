package com.calendar_client.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.google.firebase.iid.FirebaseInstanceIdService;
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
import com.google.firebase.iid.FirebaseInstanceId;

public class SignUpActivity extends AppCompatActivity {

    private int year, month, day;

    private EditText etUserName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvDateOfBirth;
    private Button btnSignUp;
    private TextView tvLoginLink;
    private EditText etPhoneNumber;
    private User user;
    private PopupWindow popupWindow;
    private ScrollView layout;
    private boolean numberValidate = false;
    private LinearLayout progressLayout;
    private LinearLayout formLayout;
    private boolean permissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        layout = (ScrollView) findViewById(R.id.layout_sign_up);

        initComponents();

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
                    checkPermission();
                    if (permissionGranted) {
                        initDialog();
                    }
                    btnSignUp.setEnabled(false);
                }
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void checkPermission() {
        int permission;
        if (Build.VERSION.SDK_INT < 23) {
            permission = PermissionChecker.checkSelfPermission(SignUpActivity.this, Manifest.permission.SEND_SMS);
            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                permissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.SEND_SMS
                }, 1);
            }
        } else { //api 23 and above
            permission = checkSelfPermission(Manifest.permission.SEND_SMS);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        new String[]{Manifest.permission.SEND_SMS},
                        1);
            } else {
                permissionGranted = true;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initDialog();

                } else{
                    // Permission Denied
                    Toast.makeText(SignUpActivity.this,
                            "We couldn't confirm your phone number. please approve sending sms",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }

        }
    }

    private void initDialog(){
        String strPhone = etPhoneNumber.getText().toString();
        Data data = Data.getInstance();
        final String key = data.generateKey();
        String strMessage = "Your verification code is: " + key;

        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(strPhone, null, strMessage, null, null);

        final Dialog dialog = new Dialog(SignUpActivity.this);
//        dialog.setTitle("Title");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.sms_verification_popup);


        Button btnConfirm = (Button) dialog.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText etCode =(EditText)dialog.findViewById(R.id.etCode);

                Log.d("key", "input is: " + etCode.getText().toString() + " key is: " + key);

                if (key.equals(etCode.getText().toString())) {
                    dialog.dismiss();
                    progressLayout = (LinearLayout) findViewById(R.id.progress_layout);
                    formLayout = (LinearLayout) findViewById(R.id.form_layout);
                    formLayout.setVisibility(View.GONE);
                    progressLayout.setVisibility(View.VISIBLE);

                    new SignUpTask().execute();
                } else{
                    etCode.setError(getResources().getString(R.string.popup_key_not_match));
                }
            }
        });
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height =  WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }


    public boolean validate() {
        boolean valid = true;

        String userName = etUserName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String date = tvDateOfBirth.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();


        // user name validation
        if (userName.isEmpty()) {
            etUserName.setError(getResources().getString(R.string.sign_up_username_error_empty));
            valid = false;
        } else if (userName.length() < 5) {
            etUserName.setError(getResources().getString(R.string.sign_up_username_error_length));
            valid = false;
        } else {
            etUserName.setError(null);
        }

        // email validation
        if (email.isEmpty()) {
            etEmail.setError(getResources().getString(R.string.sign_up_email_error_empty));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getResources().getString(R.string.sign_up_email_error_invalid));
            valid = false;
        } else {
            etEmail.setError(null);
        }

        // password validation
        if (password.isEmpty()) {
            etPassword.setError(getResources().getString(R.string.sign_up_password_error_empty));
            valid = false;
        } else if (password.length() < 5) {
            etPassword.setError(getResources().getString(R.string.sign_up_password_error_length));
            valid = false;
        } else {
            etPassword.setError(null);
        }

        // confirm password validation
        if (!confirmPassword.equals(password)) {
            etConfirmPassword.setError(getResources().getString(R.string.sign_up_confirm_password_error_no_match));
            valid = false;
        }else {
            etConfirmPassword.setError(null);
        }

//        // date validation
//        if (date.equals(getResources().getString(R.string.sign_up_date))) {
//            tvDateOfBirth.setError(getResources().getString(R.string.sign_up_date_empty));
//            valid = false;
//        }else {
//            tvDateOfBirth.setError(null);
//        }

        // phone number validation
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError(getResources().getString(R.string.sign_up_phone_number_empty));
            valid = false;
        }else {
            etPhoneNumber.setError(null);
        }

        return valid;
    }

    private void initComponents(){
        etUserName = (EditText) findViewById(R.id.etUserName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        tvDateOfBirth = (TextView) findViewById(R.id.tvDateOfBirth);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        tvLoginLink = (TextView) findViewById(R.id.tvLoginLink);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "BreeSerif-Regular.ttf");

        etUserName.setTypeface(typeface);
        tvDateOfBirth.setTypeface(typeface);
        btnSignUp.setTypeface(typeface);
        tvLoginLink.setTypeface(typeface);
    }

    // if details validation was successful we sent the new user to the server
    private class SignUpTask extends AsyncTask<String,Void,Boolean>{
        boolean result = false;

        @Override
        protected void onPreExecute() {
            user = new User();
            user.setEmail(etEmail.getText().toString());
            user.setPhoneNumber(etPhoneNumber.getText().toString());
            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.set(Calendar.YEAR,year);
            dateOfBirth.set(Calendar.MONTH,month);
            dateOfBirth.set(Calendar.DAY_OF_MONTH,day);
            user.setDateOfBirth(dateOfBirth);
            user.setPassword(etPassword.getText().toString());
            user.setUserName(etUserName.getText().toString());

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            // Request - send the user as json to the server for insertion
            Gson gson = new Gson();
            String jsonUser = gson.toJson(user, User.class);
            URL url = null;
            try {
                url = new URL(ApplicationConstants.SIGN_UP_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "text/plain");
                con.setRequestProperty("Accept", "text/plain");
                con.setRequestMethod("POST");

                con.connect();
                OutputStream os = con.getOutputStream();
                os.write(jsonUser.getBytes("UTF-8"));
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

            // if insertion was successful
            if (result) {
                progressLayout.setVisibility(View.GONE);
                formLayout.setVisibility(View.VISIBLE);
                Snackbar snackbar = Snackbar
                        .make(formLayout, getString(R.string.sign_up_successful), Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(ContextCompat.getColor(SignUpActivity.this,R.color.colorPrimary));
//                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                View sbView = snackbar.getView();
                sbView.setBackgroundColor(ContextCompat.getColor(SignUpActivity.this, android.R.color.white));
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(SignUpActivity.this,R.color.colorPrimary));
//                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                snackbar.show();

                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                SignUpActivity.this.finish();
            } else {
                btnSignUp.setEnabled(true);
                Toast.makeText(SignUpActivity.this, getResources().getString(R.string.sign_up_error), Toast.LENGTH_LONG).show();
            }
        }
    }


}
