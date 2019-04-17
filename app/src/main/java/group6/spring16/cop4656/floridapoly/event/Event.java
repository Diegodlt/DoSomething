package group6.spring16.cop4656.floridapoly.event;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class Event implements Serializable {
    public String eventId;
    public String hostId;

    public String title;
    public Date   dateTime;
    public String description;
    public int    maxAttendees;

    // Location
    public double latitude;
    public double longitude;

    public List<String> attendees = new ArrayList<>();

    // Firestore requires a no-arg constructor for custom objects
    public Event() {

    }

    public Event(String eventId, String hostId, String title, Date dateTime, LatLng location, int maxAttendees) {
        this.title = title;
        this.dateTime = dateTime;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.maxAttendees = maxAttendees;
        this.eventId = eventId;
        this.hostId = hostId;
    }

    public Event(String eventId, String hostId, String title, Date dateTime, LatLng location, int maxAttendees, String description) {
        this(eventId, hostId, title, dateTime, location, maxAttendees);
        this.description = description;
    }

    public String getEventId() {
        return eventId;
    }

    public String getHostId() {
        return hostId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return dateTime;
    }

    public void setDate(Date date) {
        dateTime = date;
    }

    public void day(Date date) {
        Calendar current = Calendar.getInstance();
        Calendar modify = Calendar.getInstance();

        current.setTime(dateTime);
        modify.setTime(date);

        current.set(Calendar.DAY_OF_MONTH, modify.get(Calendar.DAY_OF_MONTH));
        current.set(Calendar.MONTH, modify.get(Calendar.MONTH));
        current.set(Calendar.YEAR, modify.get(Calendar.YEAR));

        dateTime = current.getTime();
    }

    public void time(Date date) {
        Calendar current = Calendar.getInstance();
        Calendar modify = Calendar.getInstance();

        current.setTime(dateTime);
        modify.setTime(date);

        current.set(Calendar.HOUR_OF_DAY, modify.get(Calendar.HOUR_OF_DAY));
        current.set(Calendar.MINUTE, modify.get(Calendar.MINUTE));

        dateTime = current.getTime();
    }

    public LatLng location() {
        return new LatLng(latitude, longitude);
    }

    public void location(LatLng location) {
        latitude = location.latitude;
        longitude = location.longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        longitude = lon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public boolean isUserAttending(String userId) {
        return attendees.contains(userId);
    }

    public void setAttendees(List<String> users) {
        this.attendees = users;
    }

    public void addAttendee(String user) {
        attendees.add(user);
    }

    public void removeAttendee(String user) {
        attendees.remove(user);
    }

    public int numCurrentAttendees() {
        return attendees.size();
    }

    public boolean full() {
        return attendees.size() >= maxAttendees;
    }
}
