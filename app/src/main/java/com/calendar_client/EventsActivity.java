package com.calendar_client;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.calendar_client.data.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventsActivity extends AppCompatActivity {

    List<Event> events;
    ListView lvEvents;
    private FloatingActionButton fabAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        getSupportActionBar().setTitle(R.string.app_name);

        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        events = new ArrayList<>();
        lvEvents = (ListView) findViewById(R.id.lvEvents);
        Event event = new Event();
        event.setId(1);
        event.setTitle("HomeWork");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2016);
        calendar.set(Calendar.MONTH,8);
        calendar.set(Calendar.DAY_OF_MONTH,2);
        calendar.set(Calendar.HOUR_OF_DAY,12);
        calendar.set(Calendar.MINUTE,02);
        event.setDateStart(calendar);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2016);
        calendar.set(Calendar.MONTH,8);
        calendar.set(Calendar.DAY_OF_MONTH,4);
        calendar.set(Calendar.HOUR_OF_DAY,12);
        calendar.set(Calendar.MINUTE,10);
        event.setDateEnd(calendar);
        event.setDescription("Algebra HomeWork");
        events.add(event);

        lvEvents.setAdapter(new MyAdapter(this,R.layout.single_event,events));

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newEventIntent = new Intent(EventsActivity.this,NewEventActivity.class);
                startActivity(newEventIntent);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm");

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new ViewHolder();
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvFullDate = (TextView) convertView.findViewById(R.id.tvFullDate);
                holder.imageViewEdit = (ImageView) convertView.findViewById(R.id.imageViewEdit);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(event.getTitle());
            holder.tvFullDate.setText(sdf.format(event.getDateStart().getTime()));
            holder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            return convertView;

        }

        private class ViewHolder{
            TextView tvTitle;
            TextView tvFullDate;
            ImageView imageViewEdit;
        }
    }
}
