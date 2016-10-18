package com.calendar_client.data;

import android.location.Location;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nadav on 21-Sep-16.
 */

public class Event {

    private int id;
    private String title;
    private Calendar dateStart;
    private Calendar dateEnd;
    private Location location;
    private String description;

    public Event(){

    }

    public Event(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Event(String title, Calendar dateStart, Calendar dateEnd, Time timeStart, String description) {
        this.title = title;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Calendar getDateStart() {
        return dateStart;
    }

    public void setDateStart(Calendar dateStart) {
        this.dateStart = dateStart;
    }

    public Calendar getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
