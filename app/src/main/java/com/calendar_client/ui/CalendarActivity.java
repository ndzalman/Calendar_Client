package com.calendar_client.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventDecorator;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.calendar_client.utils.OnSwipeTouchListener;
import com.calendar_client.utils.SwipeGestureDetector;
import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends DrawerActivity {

    private List<Event> events;
    private ListView lvEvents;
    private FloatingActionButton fabAdd;
    private EventsDBHandler dbHandler;
    private MaterialCalendarView calendar;
    private MyAdapter eventAdapter;
    private SharedPreferences sharedPreferences;
    private User user;
    private HashSet<CalendarDay> dates;
    private EventDecorator eventDecorator;
    private Data data;
    private TextView tvMonthYear;
    private ImageView imgArrowLeft;
    private ImageView imgArrowRight;
    private String monthName;
    private final Locale israel = new Locale("iw");
    private final Locale defaultLocale = Locale.getDefault();
    private boolean isRtl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        super.onCreateDrawer();

        isRtl = Locale.getDefault().getLanguage().toString().equals(israel.getLanguage().toString());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user", "");
        Gson gson = new Gson();
        user = gson.fromJson(userJSON, User.class);

        // decorator color for event
        final int color = ContextCompat.getColor(this, R.color.colorPrimary);

        calendar = (MaterialCalendarView) findViewById(R.id.calendarView);
        tvMonthYear= (TextView) findViewById(R.id.tvMonthYear);
        imgArrowLeft = (ImageView) findViewById(R.id.imgArrowLeft);
        imgArrowRight = (ImageView) findViewById(R.id.imgArrowRight);

        // setting the first day of week
        calendar.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .commit();
        calendar.setSelectedDate(Calendar.getInstance());

        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        lvEvents = (ListView) findViewById(R.id.lvEvents);

        dbHandler = new EventsDBHandler(this);

        //get user events by his id from SQLite
        data = Data.getInstance();
        if (!data.isOnline()) {
            events = dbHandler.getEventByDay(calendar.getSelectedDate().getCalendar(), user.getId());
            dates = dbHandler.getEventByMonth
                    (calendar.getSelectedDate().getCalendar(), user.getId());
        } else {
            events = filterEventsByDay(calendar.getSelectedDate().getCalendar());
            dates = filterEventsByMonth(calendar.getSelectedDate().getCalendar());
        }

        eventAdapter = new MyAdapter(this, R.layout.single_event, events);
        lvEvents.setAdapter(eventAdapter);


        eventDecorator = new EventDecorator(color, dates);
        calendar.addDecorator(eventDecorator);

        // passing to the event details activity the selected day for this event
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newEventIntent = new Intent(CalendarActivity.this, EditEventActivity.class);
                newEventIntent.putExtra("selectedDay", calendar.getSelectedDate().getCalendar());
                startActivity(newEventIntent);
                finish();
            }
        });

        // listener to date change
        // get the new events according to the selected day
        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Log.e("DayChange", "before events size: " + events.size());
                if (!data.isOnline()) {
                    events = dbHandler.getEventByDay(date.getCalendar(), user.getId());
                } else {
                    events = filterEventsByDay(calendar.getSelectedDate().getCalendar());
                }
                Log.e("DayChange", "after events size: " + events.size());
                eventAdapter.getData().clear();
                eventAdapter.getData().addAll(events);
                // update ui list
                eventAdapter.notifyDataSetChanged();
            }
        });

        // listener on event, passing the selected event
        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Event event = (Event) adapterView.getItemAtPosition(position);
                Intent editEvent = new Intent(CalendarActivity.this, EventActivity.class);
                editEvent.putExtra("event", event);
                startActivity(editEvent);

            }
        });

        /**
         * listener for month changes, when month is changed we get a list of all dates
         * with events to inflate the event decorator
         */
        calendar.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                monthName = String.format(Locale.getDefault(),"%tB",calendar.getCurrentDate().getCalendar());
                tvMonthYear.setText(monthName + " " + calendar.getCurrentDate().getYear());

                Log.e("monthChanged", "before dates size " + dates.size());
                if (!data.isOnline()) {
                    dates = dbHandler.getEventByMonth(date.getCalendar(), user.getId());
                } else {
                    dates = filterEventsByMonth(date.getCalendar());
                }

                Log.e("monthChanged", "after dates size " + dates.size());
                calendar.removeDecorator(eventDecorator);
                eventDecorator = new EventDecorator(color, dates);
                calendar.addDecorator(eventDecorator);
                calendar.invalidate();
            }
        });

        if (!data.isOnline()) {
            fabAdd.setVisibility(View.GONE);

        }

        if (isRtl){
            calendar.setPagingEnabled(false);

            final GestureDetector gestureDetector = new GestureDetector(this, new SwipeGestureDetector(calendar));
            calendar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            calendar.setOnTouchListener(new OnSwipeTouchListener(CalendarActivity.this) {
                public void onSwipeRight() {
                    calendar.goToNext();
                }

                public void onSwipeLeft() {
                    calendar.goToPrevious();
                }

            });

        }

        imgArrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRtl){
                    calendar.goToNext();
                }else{
                    calendar.goToPrevious();
                }
            }
        });

        imgArrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRtl){
                    calendar.goToPrevious();
                }else{
                    calendar.goToNext();
                }
            }
        });

        monthName = String.format(Locale.getDefault(),"%tB",calendar.getCurrentDate().getCalendar());
        tvMonthYear.setText(monthName + " " + calendar.getCurrentDate().getYear());
        calendar.setTopbarVisible(false);

    }

    //events adapter
    private class MyAdapter extends BaseAdapter {
        List<Event> events;
        Context context;
        int layout;

        public MyAdapter(Context context, int layout, List<Event> events) {
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
            ViewHolder holder;
            Event event = (Event) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new ViewHolder();
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());

            SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.TIME_FORMAT);
            Date date = event.getDateStart().getTime();
            String startTimetxt = sdf.format(date);
            date = event.getDateEnd().getTime();
            String endTimeTxt = sdf.format(date);
            holder.tvTime.setText(startTimetxt + " - " + endTimeTxt);

            return convertView;

        }

        private class ViewHolder {
            TextView tvTitle;
            TextView tvTime;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getString(R.string.exit_dialog_title))
                .setMessage(getResources().getString(R.string.exit_dialog_message))
                .setPositiveButton(getString(R.string.exit_dialog_postive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton(getString(R.string.exit_dialog_negative), null)
                .show();
    }

    private List<Event> filterEventsByDay(Calendar calendar) {
        List<Event> eventOfToday = new ArrayList<>();
        for (Event event : data.getSharedEvents()) {
            if ((calendar.get(Calendar.YEAR) == event.getDateStart().get(Calendar.YEAR) ||
                    calendar.get(Calendar.YEAR) == event.getDateEnd().get(Calendar.YEAR)) &&
                    (calendar.get(Calendar.MONTH) == event.getDateStart().get(Calendar.MONTH) ||
                            calendar.get(Calendar.MONTH) == event.getDateEnd().get(Calendar.MONTH))) {

                if (calendar.get(Calendar.DAY_OF_MONTH) == event.getDateStart().get(Calendar.DAY_OF_MONTH) ||
                        calendar.get(Calendar.DAY_OF_MONTH) == event.getDateEnd().get(Calendar.DAY_OF_MONTH)) {
                    eventOfToday.add(event);
                } else if (calendar.after(event.getDateStart()) && calendar.before(event.getDateEnd())) {
                    eventOfToday.add(event);
                }
            }
        }
        if (eventOfToday.size() > 0) {
            Log.e("EVENT-USRE", "users in event: " + eventOfToday.get(0).getUsers().toString());
        }
        Log.d("EVENTS", "size of events : " + eventOfToday.size());
        return eventOfToday;
    }

    private HashSet<CalendarDay> filterEventsByMonth(Calendar d) {
        // TODO: change on month listener when changing to week mode
        Calendar between = Calendar.getInstance();
        CalendarDay day;
        HashSet<CalendarDay> dates = new HashSet<>();

        List<Event> eventOfToday = new ArrayList<>();
        for (Event e : data.getSharedEvents()) {
            Log.d("EVENTS", "today: " + d.get(Calendar.MONTH) + " == " + e.getDateStart().get(Calendar.MONTH));
            Log.d("EVENTS", "today: " + d.get(Calendar.DAY_OF_MONTH) + " == " + e.getDateStart().get(Calendar.DAY_OF_MONTH));
            if (e.getDateStart().get(Calendar.MONTH) == d.get(Calendar.MONTH)
                    && e.getDateStart().get(Calendar.YEAR) == d.get(Calendar.YEAR)) {
                day = CalendarDay.from(e.getDateStart());
                dates.add(day);
            }
        }

        for (Event event : data.getSharedEvents()) {
            if (event.getDateEnd().after(event.getDateStart())) {
                between = Calendar.getInstance();
                long diff = (event.getDateEnd().getTimeInMillis()) - (event.getDateStart().getTimeInMillis());
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                between.setTimeInMillis(event.getDateStart().getTimeInMillis());
                Log.e("events", "days: " + days);
                while (days > 0) {
                    between.add(Calendar.DAY_OF_MONTH, 1);
                    day = CalendarDay.from(between);
                    dates.add(day);
                    days--;
                }
            }
        }
        Log.d("EVENTS", "size of dates : " + dates.size());

        return dates;
    }


}
