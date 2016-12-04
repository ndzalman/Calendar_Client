package com.calendar_client.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.User;
import com.calendar_client.utils.Data;
import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;

public abstract class DrawerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvHeaderTitle;
    private MaterialCalendarView calendar;

    protected void onCreateDrawer() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
//        getSupportActionBar().setLogo(getDrawable(R.drawable.logo));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        View view = findViewById(R.id.main);
        calendar = (MaterialCalendarView) drawerLayout.findViewById(R.id.calendarView);

        View headerView = navigationView.getHeaderView(0);

        tvHeaderTitle = (TextView) headerView.findViewById(R.id.tvHeaderTitle);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String userJSON = sharedPreferences.getString("user", "");
        User u = gson.fromJson(userJSON, User.class);
        if (u != null) {
            tvHeaderTitle.setText(u.getUserName());
        }

        ImageView imgviewHeaderImage = (ImageView) headerView.findViewById(R.id.imgviewHeaderImage);
        Data data = Data.getInstance();
        Bitmap b = data.getBitmap();
        if (b != null) {
            imgviewHeaderImage.setImageBitmap(b);
        }


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()){
                    case R.id.navigation_item_month:
                        calendar.state().edit()
                                .setCalendarDisplayMode(CalendarMode.MONTHS)
                                .commit();
                        break;
                    case R.id.navigation_item_week:
                        calendar.state().edit()
                                .setCalendarDisplayMode(CalendarMode.WEEKS)
                                .commit();
                        break;
//                    case R.id.navigation_item_day:
//                        break;
                    case R.id.navigation_item_schedule:
                        Intent eventsActivity = new Intent(DrawerActivity.this,EventsActivity.class);
                        startActivity(eventsActivity);
                        break;
                    case R.id.navigation_item_profile:
                        Intent profileActivity = new Intent(DrawerActivity.this,ProfileActivity.class);
                        startActivity(profileActivity);
                        break;
                    case R.id.navigation_item_about:
                        Intent aboutActivity = new Intent(DrawerActivity.this,AboutActivity.class);
                        startActivity(aboutActivity);
                        break;
                    case R.id.navigation_item_log_out:
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("user");
                        editor.apply();
                        finish();
                        Intent logInIntent = new Intent(DrawerActivity.this,LoginActivity.class);
                        startActivity(logInIntent);
                        break;
                    default:
                        break;
                }
               return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: // home button on toolbar
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.item_update: // update button on toolabr - takes to the current day
                calendar.setCurrentDate(Calendar.getInstance());
                calendar.setSelectedDate(Calendar.getInstance());
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
