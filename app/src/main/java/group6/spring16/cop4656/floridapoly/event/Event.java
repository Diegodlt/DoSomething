package group6.spring16.cop4656.floridapoly.event;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Event {
    private String title;
    private Calendar date;
    private double lat;
    private double lng;
    private String description;
    private List<EventRequirement> requirements = new ArrayList<>();
    private int maxAttendees;
    private String uid;
    //private List<User> attendees = new ArrayList<>();

    // Firestore requires a no-arg constructor for custom objects
    public Event(){

    }

    public Event(String title, Calendar dateTime, double lat, double lng, int maxAttendees, String uid) {
        this.title = title;
        this.date = dateTime;
        this.lat = lat;
        this.lng = lng;
        this.maxAttendees = maxAttendees;
        this.uid = uid;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Calendar getDate() {
        return (Calendar)date.clone();
    }

    public void setDate(Calendar date) { this.date = date; }

    public double getLat() { return lat;}

    public double getLng() { return lng; }

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

    public List<EventRequirement> getRequirements() {
        return requirements;
    }

    public void addRequirement(EventRequirement requirement) {
        requirements.add(requirement);
    }

    public String getUid() { return uid; }

}
