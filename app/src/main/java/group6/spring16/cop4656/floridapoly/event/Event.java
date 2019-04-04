package group6.spring16.cop4656.floridapoly.event;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class Event {
    private String   userId;
    private String   title;
    private Date     dateTime;
    private String   description;
    private int      maxAttendees;

    // Location
    private double latitude;
    private double longitude;

    //private List<EventRequirement> requirements = new ArrayList<>();
    //private List<User> attendees = new ArrayList<>();


    // Firestore requires a no-arg constructor for custom objects
    public Event() {

    }

    public Event(String title, Date dateTime, LatLng location, int maxAttendees, String userId) {
        this.title = title;
        this.dateTime = dateTime;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.maxAttendees = maxAttendees;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
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

    public LatLng location() {
        return new LatLng(latitude, longitude);
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
}
