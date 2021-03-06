package com.calendar_client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.calendar_client.utils.EventsDBConstants.TIME_FORMAT;

public class DayEventsActivity extends AppCompatActivity {
    private List<Event> events;
    private ListView lvEvents;
    private EventsDBHandler dbHandler;
    private SharedPreferences sharedPreferences;
    private User user;
    private MyAdapter eventAdapter;
    private Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_events);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(this.getTitle());

        // get user from shared preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        Gson gson = new Gson();
        user = gson.fromJson(userJSON, User.class);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        SimpleDateFormat sdfs = new SimpleDateFormat(EventsDBConstants.DATE_FORMAT);
        Date date1 = Calendar.getInstance().getTime();
        String today = sdfs.format(date1);
        tvTitle.setText(today);

        lvEvents = (ListView) findViewById(R.id.lvEvents);

        data = Data.getInstance();
        List<Event> eventOfToday = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (Event event : data.getSharedEvents()) {
            if (calendar.get(Calendar.YEAR) == event.getDateStart().get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == event.getDateStart().get(Calendar.MONTH)) {
                if (calendar.get(Calendar.DAY_OF_MONTH) == event.getDateStart().get(Calendar.DAY_OF_MONTH)) {
                    if (calendar.get(Calendar.HOUR_OF_DAY) <= event.getDateStart().get(Calendar.HOUR_OF_DAY)){
                        eventOfToday.add(event);
                    }
                }
            }
        }
        if (eventOfToday.size() > 0) {
            Log.e("EVENT-USRE", "users in event: " + eventOfToday.get(0).getUsers().toString());
        }
        Log.d("EVENTS", "size of events : " + eventOfToday.size());
        events = eventOfToday;
        initList();


        // listener on event, on click we pass the event on the intent
        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Event event = (Event) adapterView.getItemAtPosition(position);
                Intent eventActivity = new Intent(DayEventsActivity.this, EventActivity.class);
                eventActivity.putExtra("event", event);
                startActivity(eventActivity);
            }
        });

    }

    private void initList() {
        // sort the events by date
        Collections.sort(events, new Comparator<Event>() {
            public int compare(Event e1, Event e2) {
                if (e1.getDateStart() == null || e1.getDateStart() == null)
                    return 0;

                if (e1.getDateStart().after(e2.getDateStart())){
                    return 1;
                } else if (e1.getDateStart().before(e2.getDateStart())){
                    return -1;

                } else {
                    return 0;
                }
            }
        });

        eventAdapter = new MyAdapter(this, R.layout.single_day_event_layout, events);
        lvEvents.setAdapter(eventAdapter);
        if (events == null || events.size() <= 0){
            RelativeLayout listLayout = (RelativeLayout) findViewById(R.id.listLayout);
            listLayout.setVisibility(View.GONE);
            CardView noEventsLayout = (CardView) findViewById(R.id.noEventsLayout);
            noEventsLayout.setVisibility(View.VISIBLE);
        }
    }

    private class MyAdapter extends BaseAdapter {
        List<Event> events;
        Context context;
        int layout;

        MyAdapter(Context context, int layout, List<Event> events) {
            this.context = context;
            this.events = events;
            this.layout = layout;
        }

        public List<Event> getData() {
            return this.events;
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public Object getItem(int i) {
            return events.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyAdapter.ViewHolder holder;
            Event event = (Event) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new MyAdapter.ViewHolder();
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
                holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
                holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
//                holder.descriptionLayout = (LinearLayout) convertView.findViewById(R.id.descriptionLayout);
//                holder.locationLayout = (LinearLayout) convertView.findViewById(R.id.locationLayout);

                convertView.setTag(holder);

            } else {
                holder = (MyAdapter.ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());
            if (!event.getLocation().isEmpty()){
                holder.tvLocation.setText(event.getLocation());
            } else{
                holder.tvLocation.setVisibility(View.GONE);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
            holder.tvTime.setText(sdf.format(event.getDateStart().getTime()) + " - " + sdf.format(event.getDateEnd().getTime()));

            if (!event.getDescription().isEmpty()) {
                holder.tvDescription.setText(event.getDescription());
            }else{
                holder.tvDescription.setVisibility(View.GONE);
            }

            return convertView;

        }

        private class ViewHolder {
            TextView tvTitle;
            TextView tvDescription;
            TextView tvLocation;
            TextView tvTime;
            LinearLayout locationLayout;
            LinearLayout descriptionLayout;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
