package com.calendar_client.utils;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.calendar_client.data.ContactDetails;
import com.calendar_client.data.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
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


    public String generateKey()
    {
        SecureRandom random = new SecureRandom();
        String characters = "0123456879ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 5;
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
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
