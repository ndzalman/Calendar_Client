package com.calendar_client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represent a user object.
 * Created by Nadav on 21-Sep-16.
 */

public class User implements Serializable{

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
     * Phone number of the user,
     */
    private String phoneNumber;

    /**
     * Password of the user
     */
    private String password;


    /**
     * token of the user
     */
    private String token;

    /**
     * image of the user
     */
    private byte[] image;

    /**
     * Events of the user, one user can have many events
     */
    private Set<Event> events = new HashSet<>();

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
    }

    public User(int id,String userName, String email, String password, Calendar dateOfBirth, List<Event> events) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
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
     * Returns the events of the user
     * @return the events of the user
     */
    public Set<Event> getEvents() {
        return events;
    }

    /**
     * Sets the events of the user
     * @param events the events of the user
     */
    public void setEvents(Set<Event> events) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id == user.id;

    }

    /**
     * Returns the phone number of the user
     * @return the phone number of the user
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the user
     * @param phoneNumber the token of the user
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the image of the user
     * @return the image of the user
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the image of the user
     * @param image the image of the user
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", token=" + token +
                ", events=" + events +
                '}';
    }
}
