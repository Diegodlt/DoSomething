package group6.spring16.cop4656.floridapoly.event;

import android.graphics.drawable.Icon;

public class EventRequirement {
    private String name;
    private String detail;
    private Icon   icon;

    public EventRequirement(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
    public String getDetail() {
        return detail;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    public Icon getIcon() {
        return icon;
    }
}