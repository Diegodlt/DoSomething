package group6.spring16.cop4656.floridapoly.event;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Event implements Serializable {
    public String eventId = UUID.randomUUID().toString();
    public String hostId;

    public String title;
    public Date   date = new Date(0L);
    public String description;
    public int    maxAttendees;

    // Location
    public double latitude;
    public double longitude;

    public List<String> attendees = new ArrayList<>();

    // Firestore requires a no-arg constructor for custom objects
    public Event() {

    }

    public Event(String hostId, String title, Date date, LatLng location, int maxAttendees) {
        this.title        = title;
        this.date         = date;
        this.latitude     = location.latitude;
        this.longitude    = location.longitude;
        this.maxAttendees = maxAttendees;
        this.hostId       = hostId;
    }

    public Event(String hostId, String title, Date date, LatLng location, int maxAttendees, String description) {
        this(hostId, title, date, location, maxAttendees);
        this.description = description;
    }

    public void day(Date date) {
        Calendar current = Calendar.getInstance();
        Calendar modify = Calendar.getInstance();

        current.setTime(this.date);
        modify.setTime(date);

        current.set(Calendar.DAY_OF_MONTH, modify.get(Calendar.DAY_OF_MONTH));
        current.set(Calendar.MONTH, modify.get(Calendar.MONTH));
        current.set(Calendar.YEAR, modify.get(Calendar.YEAR));

        this.date = current.getTime();
    }

    public void time(Date date) {
        Calendar current = Calendar.getInstance();
        Calendar modify = Calendar.getInstance();

        current.setTime(this.date);
        modify.setTime(date);

        current.set(Calendar.HOUR_OF_DAY, modify.get(Calendar.HOUR_OF_DAY));
        current.set(Calendar.MINUTE, modify.get(Calendar.MINUTE));

        this.date = current.getTime();
    }

    public LatLng location() {
        return new LatLng(latitude, longitude);
    }

    public void location(LatLng location) {
        latitude = location.latitude;
        longitude = location.longitude;
    }

    public boolean isUserAttending(String userId) {
        return attendees.contains(userId);
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
