package com.calendar_client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
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
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class UpComingEventsActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_upcoming_events);

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
        events = new ArrayList<>();

        data = Data.getInstance();
        if (!data.isOnline()) {
            dbHandler = new EventsDBHandler(this);
//            events = dbHandler.getAllEvents(user.getId());
            events = dbHandler.getUpComingEvents(user.getId());
            initList();
        }else{
            int size = 0;
            Calendar calendar = Calendar.getInstance();
            List<Event> sortedEvents = new ArrayList<>();
            sortedEvents.addAll(Data.getInstance().getSharedEvents());
            Collections.sort(sortedEvents, new Comparator<Event>() {
                public int compare(Event e1, Event e2) {
                    if (e1.getDateStart() == null || e1.getDateStart() == null)
                        return 0;
                    return e1.getDateStart().compareTo(e2.getDateStart());
                }
            });

            for (Event event: sortedEvents){
                if (size < 10) {
                    if (event.getDateStart().after(calendar) ||
                            (event.getDateEnd().after(calendar) && !events.contains(event))) {
                        events.add(event);
                        size++;
                    }
                }else{
                    break;
                }
            }
            initList();
        }



        // listener on event, on click we pass the event on the intent
        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Event event = (Event) adapterView.getItemAtPosition(position);
                Intent eventActivity = new Intent(UpComingEventsActivity.this,EventActivity.class);
                eventActivity.putExtra("event",event);
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
                return e1.getDateStart().compareTo(e2.getDateStart());
            }
        });

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

         MyAdapter(Context context, int layout, List<Event> events){
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
                holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
                holder.locationLayout = (LinearLayout) convertView.findViewById(R.id.locationLayout);
                holder.descriptionLayout = (LinearLayout) convertView.findViewById(R.id.descriptionLayout);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());

            if (event.getLocation().isEmpty()) {
                holder.locationLayout.setVisibility(View.GONE);
            }else{
                holder.tvLocation.setText(event.getLocation());
            }

            if (event.getDescription().isEmpty()){
                holder.descriptionLayout.setVisibility(View.GONE);
            }else{
                holder.tvDescription.setText(event.getDescription());
            }

            SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.TIME_FORMAT);
            Date date = event.getDateStart().getTime();
            String startTimetxt = sdf.format(date);
            date = event.getDateEnd().getTime();
            String endTimeTxt = sdf.format(date);
            holder.tvTime.setText(startTimetxt + " - " + endTimeTxt);

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
            TextView tvTime;
            TextView tvDate;
            TextView tvLocation;
            LinearLayout locationLayout;
            LinearLayout descriptionLayout;
        }
    }

//    private class GetUpcomingEvents extends AsyncTask<String, Void, String> {
//        // executing
//        @Override
//        protected String doInBackground(String... strings) {
//            StringBuilder response;
//            try {
//                URL url = new URL(ApplicationConstants.GET_UPCOMING_EVENTS + "?id=" + user.getId());
//                response = new StringBuilder();
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                Log.e("DEBUG", conn.getResponseCode() + "");
//                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                    Log.e("DEBUG", conn.getResponseMessage());
//                    return null;
//                }
//
//                BufferedReader input = new BufferedReader(
//                        new InputStreamReader(conn.getInputStream()));
//
//                String line;
//                while ((line = input.readLine()) != null) {
//                    response.append(line + "\n");
//                }
//
//                input.close();
//
//                conn.disconnect();
//
//                Type listType = new TypeToken<ArrayList<Event>>() {
//                }.getType();
//                List<Event> upcomingEvents = new Gson().fromJson(response.toString(), listType);
//                events = upcomingEvents;
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String response) {
//            initList();
//
//        }
//
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                Intent intent = new Intent(UpComingEventsActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
