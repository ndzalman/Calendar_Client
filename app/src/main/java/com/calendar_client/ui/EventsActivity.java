package com.calendar_client.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventsActivity extends DrawerActivity {

    private List<Event> events;
    private ListView lvEvents;
    private FloatingActionButton fabAdd;
    private EventsDBHandler dbHandler;
    private MaterialCalendarView calendar;
    private MyAdapter eventAdapter;
    private SharedPreferences sharedPreferences;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_event);
            super.onCreateDrawer();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        Gson gson = new Gson();
        user = gson.fromJson(userJSON, User.class);

        calendar = (MaterialCalendarView) findViewById(R.id.calendarView);

        calendar.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .commit();
        calendar.setSelectedDate(Calendar.getInstance());

        dbHandler = new EventsDBHandler(this);

            fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
            lvEvents = (ListView) findViewById(R.id.lvEvents);
            events = dbHandler.getEventByDay(calendar.getSelectedDate().getCalendar(),user.getId());

            eventAdapter = new MyAdapter(this,R.layout.single_event,events);
            lvEvents.setAdapter(eventAdapter);

            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newEventIntent = new Intent(EventsActivity.this,NewEventActivity.class);
                    startActivity(newEventIntent);
                    finish();
                }
            });

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                events = dbHandler.getEventByDay(date.getCalendar(),user.getId());
                eventAdapter.getData().clear();
                eventAdapter.getData().addAll(events);
                // fire the event
                eventAdapter.notifyDataSetChanged();}
        });

        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Event event = (Event) adapterView.getItemAtPosition(position);
                Intent editEvent = new Intent(EventsActivity.this,NewEventActivity.class);
                editEvent.putExtra("event",event);
                startActivity(editEvent);
                finish();
            }
        });

    }

    private class MyAdapter extends BaseAdapter{
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


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());
            holder.tvDescription.setText(event.getDescription());

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
        }
    }
}
