package group6.spring16.cop4656.floridapoly;

import android.graphics.drawable.Icon;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;


public class Event {
    private String title;
    private Date   date;
    private LatLng location;
    private String description;
    private List<EventRequirement> requirements;

    Event(String title, Date date, LatLng location) {
        this.title = title;
        this.date = date;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EventRequirement> getRequirements() {
        return requirements;
    }

    public void addRequirement(EventRequirement requirement) {
        requirements.add(requirement);
    }
}
