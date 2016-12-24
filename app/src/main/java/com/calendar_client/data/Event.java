package com.calendar_client.data;

import android.location.Location;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represent an event object.

 * Created by Nadav on 21-Sep-16.
 */

public class Event implements Serializable{

    /**
     * Id of this event.
     */
    private int id;

    /**
     * Title of this event.
     */
    private String title;

    /**
     * The starting date of this event.
     */
    private Calendar dateStart;

    /**
     * The ending date of this event.
     */
    private Calendar dateEnd;

    /**
     * Description of this event.
     */
    private String description;

    /**
     * Users of this event.
     */
    private Set<User> users = new HashSet<>();

    /**
     * User id of the owner of this event
     */
    private int ownerId;

    /**
     * Location of this event
     */
    private String location;

    /**
     * extra data on the event
     */
    private byte[] image;

    /**
     * Default constructor.initialize an empty event object.
     */
    public Event(){

    }

    /**
     * Constructor of event. initialize an event object with title and description.
     * @param title the title of this event
     * @param description description of this event
     */
    public Event(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * Constructor of event. initialize an event object with title,starting date, ending date and description.
     * @param title the title of this event
     * @param dateStart the starting date of this event
     * @param dateEnd the ending date of this event
     * @param description description of this event
     */
    public Event(String title, Calendar dateStart, Calendar dateEnd, Time timeStart, String description) {
        this.title = title;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.description = description;
    }

    /**
     * Returns the id of this event.
     * @return the id of this event
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of this event
     * @param id the id of this event
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the title of this event.
     * @return the title of this event
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this event
     * @param title the title of this event
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the starting date of this event
     * @return the starting date of this event
     */
    public Calendar getDateStart() {
        return dateStart;
    }

    /**
     * Sets the starting date  of this event
     * @param dateStart the starting date  of this event
     */
    public void setDateStart(Calendar dateStart) {
        this.dateStart = dateStart;
    }

    /**
     * Returns the ending date of this event
     * @return the ending date of this event
     */
    public Calendar getDateEnd() {
        return dateEnd;
    }

    /**
     * Sets the ending date of this event
     * @param dateEnd the ending date of this event
     */
    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * Returns the description of this event
     * @return the description of this event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this event
     * @param description the description of this event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the user of this event
     * @return the user of this event
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * Sets the user of this event
     * @param users the user of this event
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user){
        this.users.add(user);
    }

    public void removeUser(User user){
        this.users.remove(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return id == event.id;

    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dateStart=" + dateStart.getTime() +
                ", dateEnd=" + dateEnd.getTime() +
//                ", location=" + location +
                ", description='" + description + '\'' +
                '}';
    }

}
