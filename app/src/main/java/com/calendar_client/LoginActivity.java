package com.calendar_client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity {
    @InjectView(R.id.etPassword)
    EditText etPassword;
    @InjectView(R.id.etEmail)
    EditText etEmail;

    private Button btnLogin;
    private TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);

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

                if (valid){
                    Intent eventIntent = new Intent(LoginActivity.this,EventsActivity.class);
                    startActivity(eventIntent);
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        getSupportActionBar().hide();

    }
}
