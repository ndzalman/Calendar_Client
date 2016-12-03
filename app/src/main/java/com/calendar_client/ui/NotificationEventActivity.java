package com.calendar_client.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.Event;

public class NotificationEventActivity extends AppCompatActivity {
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_event_layout);

        if (getIntent().getSerializableExtra("event") != null) {
            event = (Event)getIntent().getSerializableExtra("event");
        }

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvDesc = (TextView) findViewById(R.id.tvDescription);
        TextView tvDateStart = (TextView) findViewById(R.id.tvDateStart);
        TextView tvDateEnd = (TextView) findViewById(R.id.tvDateEnd);

        ListView lvContacts = (ListView) findViewById(R.id.listContacts);
        lvContacts.setVisibility(View.GONE);

        TextView tvList = (TextView) findViewById(R.id.tvList);
        tvList.setVisibility(View.GONE);

        tvTitle.setText(event.getTitle());
        tvDesc.setText(event.getDescription());
        tvDateStart.setText(event.getDateStart().getTime().toString());
        tvDateEnd.setText(event.getDateEnd().getTime().toString());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(NotificationEventActivity.this,SplashScreenActivity.class);
        startActivity(intent);
    }
}
