package com.calendar_client.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.calendar_client.R;
import com.calendar_client.data.ContactDetails;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.Data;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {
    private View view;
    private MyAdapter usersAdapter;
    private List<User> users = new ArrayList<>();
    private ListView lvUsers;
    private List<String> usersNames = new ArrayList<>();
    private Event event;
    private boolean inEdit = false;


    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        Data data = Data.getInstance();

        lvUsers = (ListView) view.findViewById(R.id.lvUsers);

        // if we are in edit event
        if (getActivity().getIntent().getSerializableExtra("event") != null) {
            event = (Event) getActivity().getIntent().getSerializableExtra("event");
            inEdit = true;
            List<User> existingUser = new ArrayList<>(event.getUsers()); //set to array list
            users = getContacts(existingUser);
            usersAdapter = new MyAdapter(getActivity(), R.layout.single_contant_layout, users);
            lvUsers.setAdapter(usersAdapter);
        } else{
            if (data.isOnline() == false){

            } else{
                new GetUsersTask().execute();
            }
        }


        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox c = (CheckBox) view.findViewById(R.id.checkBox);
                if (c.isChecked()){
                    c.setChecked(false);
                }else{
                    c.setChecked(true);
                }

            }
        });


        return view;
    }

    public ArrayList<User> getContacts(List<User> existingUsers)
    {
        ArrayList<User> allContacts = new ArrayList();
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
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

            if (phoneNumber.length() != 10){
               continue;
            }

            for(int i=0; i<existingUsers.size(); i++) {
                if (existingUsers.get(i).getPhoneNUmber().equals(phoneNumber)){
                    usersNames.add(name);
                    allContacts.add(existingUsers.get(i));
                    existingUsers.remove(existingUsers.get(i));
                    break;
                }
            }
        }
        phones.close();

        return allContacts;
    }

    private class MyAdapter extends BaseAdapter {
        List<User> users;
        Context context;
        int layout;

        public MyAdapter(Context context, int layout, List<User> users) {
            this.context = context;
            this.users = users;
            this.layout = layout;
        }

        public List<User> getData() {
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
            final ViewHolder holder;
            final User user = (User) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new ViewHolder();
                holder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvUserName.setText(usersNames.get(position));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Data data = Data.getInstance();
                    if (isChecked){
                        User us = (User) getItem(position);
                        data.addUser(us);

                    }else{
                        User us = (User) getItem(position);
                        data.removeUser(us);
                    }
                    Log.d("USERS","users size: " + data.getUsers().size());
                }
            });

            if (inEdit){
                holder.checkBox.setChecked(true);
            }

            return convertView;

        }

        private class ViewHolder {
            TextView tvUserName;
            CheckBox checkBox;
            ImageView imgViewContact;
        }

    }

    private class GetUsersTask extends AsyncTask<String, Void, String> {
        int id;
        List<User> contactsUsers;

        @Override
        protected void onPreExecute() {

            // get user from shared preference. if doesnt exist return empty string
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            String userJSON = sharedPreferences.getString("user", "");
            Gson gson = new Gson();
            User user = gson.fromJson(userJSON, User.class);
            id = user.getId();
        }

        // executing
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.GET_ALL_USERS_URL +"?id=" + id);
                response = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.e("DEBUG",conn.getResponseCode()+"");
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DEBUG",conn.getResponseMessage());
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

                Type listType = new TypeToken<ArrayList<User>>() {
                }.getType();
                users = new Gson().fromJson(response.toString(), listType);


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

//            users = getContacts();

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            contactsUsers = getContacts(users);
            if (contactsUsers.size() > 0) {
                Log.e("users", "users size: " + contactsUsers.size());
            }
            usersAdapter = new MyAdapter(getActivity(), R.layout.single_contant_layout, contactsUsers);
            lvUsers.setAdapter(usersAdapter);
        }

    }
}