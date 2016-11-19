package com.calendar_client.data;

import android.location.Location;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

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
     * User of this event. many events can be related to one user.
     */
    private User user;

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
    public User getUser() {
        return user;
    }

    /**
     * Sets the user of this event
     * @param user the user of this event
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return id == event.id;

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
