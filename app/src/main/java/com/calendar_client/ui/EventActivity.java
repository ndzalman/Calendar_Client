package com.calendar_client.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.calendar_client.R;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.calendar_client.utils.EventsDBConstants;
import com.calendar_client.utils.EventsDBHandler;
import com.calendar_client.utils.NotificationAlarmReceiver;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EventActivity extends AppCompatActivity {
    private FloatingActionButton fabEventEdit;
    private FloatingActionButton fabEventDelete;
    private TextView tvEventDateStart;
    private TextView tvEventDateEnd;
    private TextView tvEventTitle;
    private TextView tvEventDescription;
    private TextView tvEventLocation;
    private LinearLayout descriptionLayout;
    private LinearLayout locationLayout;
    private LinearLayout imageLayout;
    private TextView tvContacts;
    private Button btnShowMore;
    private ListView listContacts;
    private Event event;
    private EventsDBHandler dbHandler;
    private MyAdapter contactAdapter;
    private List<User> usersInEvent;
    private boolean permissionGranted = false;
    private List<UserInEvent> usersNames = new ArrayList<>();
    private boolean isOwner = false;
    private User user;
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        event = (Event) getIntent().getSerializableExtra("event");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(this.getTitle());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userJSON = sharedPreferences.getString("user","");
        if (!userJSON.isEmpty()){
            user = new Gson().fromJson(userJSON,User.class);
        }

        dbHandler = new EventsDBHandler(this);
        initEventViewComponents();

        if (!Data.getInstance().isOnline()){
            fabEventDelete.setVisibility(View.GONE);
            fabEventEdit.setVisibility(View.GONE);
            btnShowMore.setVisibility(View.GONE);
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

    private void deleteNotification() {
        int id = (int)event.getDateStart().getTimeInMillis();

        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getBaseContext(), NotificationAlarmReceiver.class);
        intent.putExtra("event", event);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), id, intent,0 );
        am.cancel(pendingIntent);
    }

    private void initEventViewComponents() {
        tvContacts = (TextView) findViewById(R.id.tvContacts);

        tvEventTitle = (TextView) findViewById(R.id.tvEventTitle);
        tvEventTitle.setText(event.getTitle());

        tvEventDateStart = (TextView) findViewById(R.id.tvEventDateStart);
        SimpleDateFormat sdf = new SimpleDateFormat(EventsDBConstants.DATE_TIME_FORMAT);
        tvEventDateStart.setText(sdf.format(event.getDateStart().getTime()));

        tvEventDateEnd = (TextView) findViewById(R.id.tvEventDateEnd);
        tvEventDateEnd.setText(sdf.format(event.getDateEnd().getTime()));


        tvEventDescription = (TextView) findViewById(R.id.tvEventDescription);
//        descriptionLayout = (LinearLayout) findViewById(R.id.descriptionLayout);

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            tvEventDescription.setText(event.getDescription());
        } else{
            tvEventDescription.setVisibility(View.GONE);
        }

        tvEventLocation = (TextView) findViewById(R.id.tvEventLocation);
//        locationLayout = (LinearLayout) findViewById(R.id.locationLayout);

        if (event.getLocation() != null && !event.getLocation().equals("")) {
            tvEventLocation.setText(event.getLocation());
        } else{
            tvEventLocation.setVisibility(View.GONE);
        }

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        if (event.getImage() != null){
            imageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventActivity.this);
                    final AlertDialog dialog = builder.create();
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogLayout = inflater.inflate(R.layout.image_popup, null);
                    dialog.setView(dialogLayout);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    ImageView image = (ImageView) dialogLayout.findViewById(R.id.popup_image);
                    Bitmap b = BitmapFactory.decodeByteArray(event.getImage(),0,event.getImage().length);
                    image.setImageBitmap(b);

                    dialog.show();
                    dialog.getWindow().setLayout(400, 600);
                }
            });
        } else{
            imageLayout.setVisibility(View.GONE);
        }

        fabEventEdit = (FloatingActionButton) findViewById(R.id.fabEventEdit);
        if (event.getOwnerId() == user.getId()){
            isOwner = true;
            fabEventEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent editEvent = new Intent(EventActivity.this, EditEventActivity.class);
                    editEvent.putExtra("event",event);
                    startActivity(editEvent);
                }
            });

        } else{
            isOwner = false;
            fabEventEdit.setVisibility(View.GONE);
        }

        fabEventDelete = (FloatingActionButton) findViewById(R.id.fabEventDelete);
        fabEventDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabEventDelete.setEnabled(false);
                deleteNotification(); //cancel notfication
                boolean isSuccessful = dbHandler.deleteEvent(event);
                if (isSuccessful) {
                    Log.i("DELETE-EVENT", "Event Deleted successfuly");
                } else {
                    Log.e("DELETE-EVENT", "Event Not edited");
                }

                // if the owner remove the event completely, else just remove the user from the event
                if (isOwner){
                    new DeleteEventTask().execute();
                } else{
                    new RemoveUserFromEventTask().execute();
                }
            }
        });

        btnShowMore = (Button) findViewById(R.id.btnShowMore);
        btnShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listContacts.getVisibility() == View.GONE){
                    tvContacts.setVisibility(View.VISIBLE);
                    listContacts.setVisibility(View.VISIBLE);
                    btnShowMore.setText(getString(R.string.event_btn_show_less));
                } else{
                    tvContacts.setVisibility(View.GONE);
                    listContacts.setVisibility(View.GONE);
                    btnShowMore.setText(getString(R.string.event_btn_show_more));
                }
            }
        });


        listContacts = (ListView) findViewById(R.id.listContacts);

        usersInEvent = new ArrayList<>(event.getUsers()); //set to array list
        checkPermission();
        if (permissionGranted) {
            usersNames = getContacts(usersInEvent);
        }
        contactAdapter = new MyAdapter(this, R.layout.single_contact_in_event, usersNames);
        listContacts.setAdapter(contactAdapter);
    }

    private class DeleteEventTask extends AsyncTask<String, Void, Boolean> {
        boolean result = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            // Request - send the event as json to the server for insertion
            Gson gson = new Gson();
            String eventJSON = gson.toJson(event, Event.class);
            URL url = null;
            try {
                url = new URL(ApplicationConstants.DELETE_EVENT_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "text/plain");
                con.setRequestProperty("Accept", "text/plain");
                con.setRequestMethod("POST");

                OutputStream os = con.getOutputStream();
                os.write(eventJSON.getBytes("UTF-8"));
                os.flush();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // Response
                StringBuilder response = new StringBuilder();
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {
                    response.append(line + "\n");
                }

                input.close();

                con.disconnect();

                if (response.toString().trim().equals("OK")) {
                    result = true;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (result) {
                Data.getInstance().getSharedEvents().remove(event);
                Toast.makeText(EventActivity.this, getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                EventActivity.this.finish();
            } else {
                fabEventDelete.setEnabled(true);
            }
        }
    }

    public List<UserInEvent> getContacts(List<User> existingUsers) {
        usersNames = new ArrayList<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            // first, remove all non digits from phone number, next if the number starts with 972 replace with zero
            // if number length is below 10 its not a valid phone number
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumberBefore = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumberBefore = phoneNumberBefore.replaceAll("[^\\d.]", "");
            String phoneNumber = "";

            if (phoneNumberBefore.startsWith("972")){
                phoneNumberBefore = phoneNumberBefore.substring(3);
                phoneNumber = "0" + phoneNumberBefore;
            } else{
                phoneNumber = phoneNumberBefore;
            }

            if (phoneNumber.length() != 10){ //lenght of a regular mobile phone
                continue;
            }

            for(int i=0; i<existingUsers.size(); i++) {
                if (existingUsers.get(i).getPhoneNumber().equals(phoneNumber)){
                    UserInEvent u = new UserInEvent();
                    u.setUserName(name);
                    if (existingUsers.get(i).getId() == event.getOwnerId()){
                        Log.e("CONTACT","user " + existingUsers.get(i).getId() + " event owner  " + event.getOwnerId());
                        u.setOwner(true);
                    }
                    Log.e("CONTACT","user " + existingUsers.get(i).getUserName() + " user id  " + existingUsers.get(i).getId());
                    usersNames.add(u);
                    existingUsers.remove(existingUsers.get(i));
                    break;
                }
            }
        }
        if (existingUsers.size() > 0){ //meaning that there are users in this event that not exist in your phone
            Log.e("EXISTING-USERS","ADDING OTHER USERS");
            for (User user: existingUsers){
                UserInEvent u = new UserInEvent();
                u.setUserName(user.getUserName());
                usersNames.add(u);
            }
        }

        phones.close();

        return usersNames;
    }

    private class MyAdapter extends BaseAdapter {
        List<UserInEvent> users;
        Context context;
        int layout;

        public MyAdapter(Context context, int layout, List<UserInEvent> users) {
            this.context = context;
            this.users = users;
            this.layout = layout;
        }

        public List<UserInEvent> getData() {
            return this.users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int i) {
            return users.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final MyAdapter.ViewHolder holder;
            final UserInEvent user = (UserInEvent) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new MyAdapter.ViewHolder();
                holder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
                holder.tvEventAdmin = (TextView) convertView.findViewById(R.id.tvEventAdmin);

                convertView.setTag(holder);

            } else {
                holder = (MyAdapter.ViewHolder) convertView.getTag();
            }

            holder.tvUserName.setText(users.get(position).getUserName());
            if (users.get(position).isOwner()){
                Log.e("CONTACT","user " + user.getUserName() + " event owner  " + event.getOwnerId());
                holder.tvEventAdmin.setVisibility(View.VISIBLE);
            }else{
                holder.tvEventAdmin.setVisibility(View.GONE);
            }
            return convertView;

        }

        private class ViewHolder {
            TextView tvUserName;
            TextView tvEventAdmin;
        }

    }


    private void checkPermission() {
        int permission1;
        int permission2;

        if (Build.VERSION.SDK_INT < 23) {
            permission1 = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            permission2 = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            if (permission1 == PermissionChecker.PERMISSION_GRANTED && permission2 == PermissionChecker.PERMISSION_GRANTED) {
                permissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE},
                        1);
            }
        } else { //api 23 and above
            permission1 = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            permission2 =checkSelfPermission(Manifest.permission.READ_PHONE_STATE);

            if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE},
                        1);
            } else {
                permissionGranted = true;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    usersNames = getContacts(usersInEvent);
                    if (usersNames.size() > 0) {
                        Log.e("users", "users size: " + usersNames.size());
                    }
                    contactAdapter = new MyAdapter(this, R.layout.single_contact_in_event, usersNames);
                    listContacts.setAdapter(contactAdapter);

                } else{
                    // Permission Denied
                    Toast.makeText(this,
                            "We couldn't get your contacts for your event. please approve this permission",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }

        }
    }

    private class RemoveUserFromEventTask extends AsyncTask<String, Void, Boolean> {
        boolean result = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.REMOVE_USER_FROM_EVENT + "?userId=" + user.getId() + "&eventId=" + event.getId());
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
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

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            String responseString = response.toString();
            if (responseString.trim().equals("ok")){
                result = true;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (result) {
                Log.d("DELETE-EVENT","delet successful");
                Data.getInstance().getSharedEvents().remove(event);
                Toast.makeText(EventActivity.this, getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                EventActivity.this.finish();
                Intent events = new Intent(EventActivity.this, CalendarActivity.class);
                startActivity(events);
            } else {
                Log.d("DELETE-EVENT","delet failed");
                fabEventDelete.setEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        if (getIntent().getBooleanExtra("notification",false) != false){
            Intent splash = new Intent(this,SplashScreenActivity.class);
            startActivity(splash);
        } else{
            Intent calendar = new Intent(this,CalendarActivity.class);
            startActivity(calendar);
        }
    }

    private class UserInEvent{
        private String userName;
        private boolean isOwner = false;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public boolean isOwner() {
            return isOwner;
        }

        public void setOwner(boolean owner) {
            isOwner = owner;
        }
    }
}
