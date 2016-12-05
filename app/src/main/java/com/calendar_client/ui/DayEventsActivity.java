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

        lvEvents = (ListView) findViewById(R.id.lvEvents);

        data = Data.getInstance();
        if (data.isOnline() == false) {
            dbHandler = new EventsDBHandler(this);
            events = dbHandler.getAllEvents(user.getId());
            initList();
        }else{
            new GetUpcomingEvents().execute();
        }



        // listener on event, on click we pass the event on the intent
        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Event event = (Event) adapterView.getItemAtPosition(position);
                Intent editEvent = new Intent(DayEventsActivity.this,MainActivity.class);
                editEvent.putExtra("event",event);
                startActivity(editEvent);
            }
        });

    }

    private void initList() {
        // sort the events by date
//        Collections.sort(events, new Comparator<Event>() {
//            public int compare(Event e1, Event e2) {
//                if (e1.getDateStart() == null || e1.getDateStart() == null)
//                    return 0;
//                return e1.getDateStart().compareTo(e2.getDateStart());
//            }
//        });

        eventAdapter = new MyAdapter(this,R.layout.single_event_order_by_date_layout,events);
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

        public MyAdapter(Context context, int layout, List<Event> events){
            this.context = context;
            this.events = events;
            this.layout = layout;
        }

        public List<Event> getData(){
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
                holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
                holder.locationLayout = (LinearLayout) convertView.findViewById(R.id.locationLayout);

                convertView.setTag(holder);

            } else {
                holder = (MyAdapter.ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());
            SimpleDateFormat sdfs = new SimpleDateFormat(EventsDBConstants.DATE_FORMAT);
            Date date1 = Calendar.getInstance().getTime();
            String today = sdfs.format(date1);
            holder.tvTitle.setText(today);

            holder.tvDescription.setText(event.getDescription());

            if (event.getLocation() == null || event.getLocation().isEmpty()) {
                holder.locationLayout.setVisibility(View.GONE);
            }else{
                holder.tvLocation.setText(event.getLocation());
            }


            SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.TIME_FORMAT);
            Date date = event.getDateStart().getTime();
            sdf = new SimpleDateFormat(EventsDBConstants.DATE_FORMAT);
            date = event.getDateStart().getTime();
            String starDatetxt = sdf.format(date);
            date = event.getDateEnd().getTime();
            String endDateTxt = sdf.format(date);
            holder.tvDate.setText(starDatetxt + " - " + endDateTxt);

            return convertView;

        }

        private class ViewHolder{
            TextView tvTitle;
            TextView tvDescription;
            TextView tvDate;
            TextView tvLocation;
            LinearLayout locationLayout;
        }
    }

    private class GetUpcomingEvents extends AsyncTask<String, Void, String> {
        // executing
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.GET_UPCOMING_EVENTS + "?id=" + user.getId());
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.e("DEBUG", conn.getResponseCode() + "");
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DEBUG", conn.getResponseMessage());
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

                Type listType = new TypeToken<ArrayList<Event>>() {
                }.getType();
                List<Event> upcomingEvents = new Gson().fromJson(response.toString(), listType);
                events = upcomingEvents;


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            initList();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                Intent intent = new Intent(DayEventsActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
