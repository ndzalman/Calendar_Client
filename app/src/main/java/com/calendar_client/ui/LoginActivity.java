package com.calendar_client.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.User;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity {
    @InjectView(R.id.etPassword)
    EditText etPassword;
    @InjectView(R.id.etEmail)
    EditText etEmail;

    private SharedPreferences sharedPreferences;
    private Button btnLogin;
    private TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String user = sharedPreferences.getString("user", "");

        if (user != null && !user.equals("")) {
            finish();
            Intent homeScreen = new Intent(this, EventsActivity.class);
            startActivity(homeScreen);
        }

        tvSignUp = (TextView) findViewById(R.id.tvSignUp);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;

                String password = etPassword.getText().toString();
                if (password.isEmpty()) {
                    etPassword.setError(getString(R.string.login_password_error_empty));
                    valid = false;
                } else if (password.length() < 5) {
                    etPassword.setError(getString(R.string.login_password_error_length));
                    valid = false;
                } else {
                    etPassword.setError(null);
                    valid = true;
                }

                String email = etEmail.getText().toString();
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError(getString(R.string.login_email_error_invalid));
                    valid = false;
                } else if (email.isEmpty()) {
                    etEmail.setError(getString(R.string.login_email_error_empty));
                } else {
                    etEmail.setError(null);
                }

                if (valid) {
                    new LoginTask().execute();
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        getSupportActionBar().hide();

    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        String email;
        String password;

        @Override
        protected void onPreExecute() {
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
        }

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder response;
            try {
                URL url = new URL("http://10.0.0.103:8080/CalendarServer/rest/users/checkUser" + "?email=" + email + "&password=" + password);
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    response.append(line + "\n");
                }

                input.close();

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            String responseString = response.toString();
            return responseString.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                User toSave = gson.fromJson(response, User.class);
                String userJSON = gson.toJson(toSave);

                editor.putString("user", userJSON);
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, EventsActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();

            } else {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_error)
                        , Toast.LENGTH_LONG).show();
            }
        }

    }
}
