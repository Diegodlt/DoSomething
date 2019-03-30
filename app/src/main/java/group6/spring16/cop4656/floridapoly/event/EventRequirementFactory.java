package group6.spring16.cop4656.floridapoly.event;


public class EventRequirementFactory {

    public static EventRequirement ageRequirement(int minAge) {
        EventRequirement req = new EventRequirement("Minimum Age");
        req.setDetail(String.valueOf(minAge));
        //req.setIcon(AGE_ICON);
        return req;
    }

}