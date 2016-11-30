package com.calendar_client.utils;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.calendar_client.data.ContactDetails;
import com.calendar_client.data.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by anael on 24/11/16.
 */
public class Data {

    private static Data ourInstance = new Data();
    private Set<User> users = new HashSet<>();

    public static Data getInstance() {
        return ourInstance;
    }

    private Data() {
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user){
        this.users.add(user);
    }

    public void removeUser(User user){
        this.users.remove(user);
    }


}
