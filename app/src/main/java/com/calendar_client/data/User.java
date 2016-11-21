package com.calendar_client.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class represent a user object.
 * Created by Nadav on 21-Sep-16.
 */

public class User {

    /**
     * Id of the user
     */
    private int id;

    /**
     * User name of the user
     */
    private String userName;

    /**
     * Email of the user, this field is unique
     */
    private String email;

    /**
     * Password of the user
     */
    private String password;

    /**
     * Date of birth of the user
     */
    private Calendar dateOfBirth;

    /**
     * token of the user
     */
    private String token;

    /**
     * Events of the user, one user can have many events
     */
    private List<Event> events = new ArrayList<>();

    /**
     * Default constructor
     */
    public User(){

    }

    /**
     * Constructor of the user
     * @param userName the userName of the user
     * @param email the email of the user
     * @param password the password of the user
     * @param dateOfBirth the date of birth of the user
     * @param events the list of events of the user
     */
    public User(String userName, String email, String password, Calendar dateOfBirth, List<Event> events) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns the userName of the user
     * @return the userName of the user
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the userName of the user
     * @param userName the user name of the user
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the email of the user
     * @return the email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user
     * @param email the email of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the password of the user
     * @return the password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user
     * @param password the password of the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the date of birth of the user
     * @return the date of birth of the user
     */
    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the date of birth of the user
     * @param dateOfBirth the date of birth of the user
     */
    public void setDateOfBirth(Calendar dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns the events of the user
     * @return the events of the user
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Sets the events of the user
     * @param events the events of the user
     */
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * Add event to the list events of the user
     * @param event the event to be added
     */
    public void addEvent(Event event){
        this.events.add(event);
    }

    /**
     * Remove event from the list events of the user
     * @param event the event to be removed
     */
    public void removeEvent(Event event){
        this.events.remove(event);
    }

    /**
     * Returns the id of the user
     * @return the id of the user
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the user
     * @param id the id of the user
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the token of the user
     * @return the token of the user
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the token of the user
     * @param token the token of the user
     */
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", events=" + events +
                '}';
    }
}
