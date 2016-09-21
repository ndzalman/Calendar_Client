package com.calendar_client.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nadav on 21-Sep-16.
 */

public class User {

    private int id;
    private String userName;
    private String email;
    private String password;
    private Date dateOfBirth;
    private List<Event> events = new ArrayList<>();

    public User(){

    }

    public User(String userName, String email, String password, Date dateOfBirth, List<Event> events) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public void addEvent(Event event){
        this.events.add(event);
    }

    public void removeEvent(Event event){
        this.events.remove(event);
    }
}
