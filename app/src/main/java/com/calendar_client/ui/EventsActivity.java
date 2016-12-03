package com.calendar_client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class EventsActivity extends AppCompatActivity {
    private List<Event> events;
    private ListView lvEvents;
    private EventsDBHandler dbHandler;
    private SharedPreferences sharedPreferences;
    private User user;
    private MyAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // get user from shared preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        Gson gson = new Gson();
        user = gson.fromJson(userJSON, User.class);

        dbHandler = new EventsDBHandler(this);
        lvEvents = (ListView) findViewById(R.id.lvEvents);
        events = dbHandler.getAllEvents(user.getId());

        // sort the events by date
        Collections.sort(events, new Comparator<Event>() {
            public int compare(Event e1, Event e2) {
                if (e1.getDateStart() == null || e1.getDateStart() == null)
                    return 0;
                return e1.getDateStart().compareTo(e2.getDateStart());
            }
        });

        eventAdapter = new MyAdapter(this,R.layout.single_event,events);
        lvEvents.setAdapter(eventAdapter);

        // listener on event, on click we pass the event on the intent
        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Event event = (Event) adapterView.getItemAtPosition(position);
                Intent editEvent = new Intent(EventsActivity.this,MainActivity.class);
                editEvent.putExtra("event",event);
                startActivity(editEvent);
            }
        });

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
            ViewHolder holder;
            Event event = (Event) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new ViewHolder();
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
                holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
                holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());
            holder.tvDescription.setText(event.getDescription());
            holder.tvLocation.setText(event.getLocation());

            SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.TIME_FORMAT);
            Date date = event.getDateStart().getTime();
            String startTimetxt = sdf.format(date);
            date = event.getDateEnd().getTime();
            String endTimeTxt = sdf.format(date);
            holder.tvTime.setText(startTimetxt + " - " + endTimeTxt);


            return convertView;

        }

        private class ViewHolder{
            TextView tvTitle;
            TextView tvDescription;
            TextView tvTime;
            TextView tvLocation;
        }
    }
}
