package com.calendar_client.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;

import com.calendar_client.data.ContactDetails;
import com.calendar_client.data.Event;
import com.calendar_client.data.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by anael on 24/11/16.
 */
public class Data {

    private static Data ourInstance = new Data();
    private Set<User> users = new HashSet<>();
    private List<Event> sharedEvents = new ArrayList<>();
    private boolean isOnline = false;
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        Log.d("DATAHOLDER-getbitmap","bitmap: " + bitmap);
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setImgByte(byte[] imgByte){
        Bitmap b = BitmapFactory.decodeByteArray(imgByte,0,imgByte.length);
        this.bitmap = Bitmap.createScaledBitmap(b,350,350,false);
        Log.d("DATAHOLDER-setbyte","bitmap: " + bitmap);
    }
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

    public List<Event> getSharedEvents() {
        return sharedEvents;
    }

    public void setSharedEvents(List<Event> sharedEvents) {
        this.sharedEvents = sharedEvents;
    }

    public void addEvent(Event sharedEvent){
        this.sharedEvents.add(sharedEvent);
    }

    public void removeEvent(Event sharedEvents){
        this.sharedEvents.remove(sharedEvents);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}