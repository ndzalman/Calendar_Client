package com.calendar_client.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.calendar_client.utils.ApplicationConstants;
import com.calendar_client.utils.EventsDBConstants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContactFragment extends Fragment {
    private View view;
    private MyAdapter usersAdapter;
    private List<User> users = new ArrayList<>();
    ListView lvUsers;

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.contacts_layout, container, false);

        lvUsers = (ListView) view.findViewById(R.id.lvUsers);

        new GetUsersTask().execute();

        return view;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            User user = (User) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layout, parent, false);
                holder = new ViewHolder();
                holder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvUserName.setText(user.getUserName());


            return convertView;

        }

        private class ViewHolder {
            TextView tvUserName;
        }
    }

    private class GetUsersTask extends AsyncTask<String, Void, String> {
        // executing
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder response;
            try {
                URL url = new URL(ApplicationConstants.GET_ALL_USERS_URL);
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
                Log.e("users",users.get(0).toString());
                usersAdapter = new MyAdapter(getActivity(), R.layout.single_contant_layout, users);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            lvUsers.setAdapter(usersAdapter);


        }

    }
}