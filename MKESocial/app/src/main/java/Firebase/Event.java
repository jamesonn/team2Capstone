package Firebase;


import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.joda.time.DateTimeComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Event implements Databasable{

    private static final String TAG = Event.class.getSimpleName();

    private String title, description;
    private long startDate, endDate, startTime, endTime;
    private String location;
    private String hostUid;
    private int suggestedAge, rating;
    private double cost;
    private List<Tag> tags;
    private String attendees; //layout attendeesID:attendeesName attendeesID:attendeesName
    private String eid;
    private String image; //holds URL of image on firebase storage

    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");


    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Event(String title, String description, GregorianCalendar startDate, GregorianCalendar endDate,
                 GregorianCalendar startTime, GregorianCalendar endTime, String location,
                 String hostUid, String attendees, int suggestedAge, int rating, double cost, List<Tag> tags) {
        setTitle(title);
        setDescription(description);
        setStartDate(startDate);
        setEndDate(endDate);
        setStartTime(startTime);
        setEndTime(endTime);
        setLocation(location);
        setHostUid(hostUid);
        setAttendes(attendees);
        setSuggestedAge(suggestedAge);
        setRating(rating);
        setCost(cost);
        setTags(tags);
    }

    public Event(String title, String description, String startDate, String endDate, String startTime,
                 String endTime, String location, String hostUid, String attendees, String suggestedAge, String rating,
                 String cost, String tags) {
        setTitle(title);
        setDescription(description);
        setStartDate(parseDate(startDate));
        setEndDate(parseDate(endDate));
        setStartTime(parseTime(startTime));
        setEndTime(parseTime(endTime));
        setLocation(location);
        setHostUid(hostUid);
        setAttendes(attendees);
        setSuggestedAge(parseInt(suggestedAge));
        setRating(parseInt(rating));
        setCost(parseInt(cost));
        setTags(parseTags(tags));
    }
    public Event(String title, String description, String startDate, String endDate, String startTime,
                 String endTime, String location, String hostUid, String attendees, String suggestedAge, String rating,
                 String cost, List<Tag> tags) {
        setTitle(title);
        setDescription(description);
        setStartDate(parseDate(startDate));
        setEndDate(parseDate(endDate));
        setStartTime(parseTime(startTime));
        setEndTime(parseTime(endTime));
        setLocation(location);
        setHostUid(hostUid);
        setAttendes(attendees);
        setSuggestedAge(parseInt(suggestedAge));
        setRating(parseInt(rating));
        setCost(parseInt(cost));
        setTags(tags);
    }

    /**
     * returns the list of tags split from a given string
     * @param tags
     * @return
     */
    public static List<Tag> parseTags(String tags)
    {
        ArrayList<Tag> tagArray = new ArrayList<Tag>();
        // split by - , or . or /
        String[] tagSplit = tags.split("-|\\.|,|/");
        // add each tag (trimmed) split to an array to return
        for(String tag: tagSplit)
            tagArray.add(new Tag(tag.trim().toLowerCase()));
        return tagArray;
    }

    /**
     * parses an integer from a string, return -1 if NAN
     * @param number
     * @return
     */
    private static int parseInt(String number)
    {
        if(number.isEmpty()) return -1;
        if(isInteger(number, 10))
            return Integer.parseInt(number);
        else
            return -1;
    }

    /**
     * returns true if string consists of character 0-9
     * also can be a negative number starting with '-'
     * @param s
     * @param radix
     * @return
     */
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    /**
     * parses the date from a given string in the format MM/dd/yy
     * @param cDate
     * @return
     */
    public static GregorianCalendar parseDate(String cDate)
    {
        Date date = new Date();
        GregorianCalendar gDate = new GregorianCalendar();
        try {
            date = dateFormat.parse(cDate);
        }
        catch(java.text.ParseException e){
            Log.w(TAG, "Date not converted: "+ cDate);
        }
        gDate.setTime(date);
        return gDate;
    }

    /**
     * parses the time from a given string in the format 'hh:mm a', locale US
     * @param cTime
     * @return
     */
    public static GregorianCalendar parseTime(String cTime)
    {
        Date time = new Date();
        GregorianCalendar gTime = new GregorianCalendar();
        try {
            time = timeFormat.parse(cTime);
        }
        catch(java.text.ParseException e){
            Log.w(TAG, "Date not converted: "+ cTime);
        }
        gTime.setTime(time);
        return gTime;
    }

    /**
     * stores adn returns all the event fields into <String, Obj> HashMap
     * used to store event obj in db
     * @return
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", getTitle());
        result.put("description", getDescription());
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("location", getLocation());
        result.put("hostUid", getHostUid());
        result.put("suggestedAge", getSuggestedAge());
        result.put("rating", getRating());
        result.put("cost", getCost());
        result.put("tags", getTags());
        result.put("attendees", getAttendees());
        result.put("image", getImage());
        return result;
    }

    //GETTERS & SETTERS
    public String getTitle() {
        return title;
    }

    public boolean setTitle(String title) {
        this.title = title;
        return true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * returns start date (long) as gregorian calendar obj
     * @return
     */
    public GregorianCalendar getStartDate() {
        GregorianCalendar gDate = new GregorianCalendar();
        gDate.setTimeInMillis(startDate);
        return gDate;
    }

    /**
     * Sets the start date (long) using a GregorianCalendar obj
     * @param startDate
     */
    @Exclude
    public boolean setStartDate(GregorianCalendar startDate) {

        if(startDate == null || (endDate != 0.0 &&
                MethodOrphanage.compareDates(startDate, getEndDate()) > 0))
            return false;
        this.startDate = startDate.getTimeInMillis();
        return true;
    }

    /**
     * returns end date (long) as gregorian calendar obj
     * @return
     */
    public GregorianCalendar getEndDate() {
        GregorianCalendar gDate = new GregorianCalendar();
        gDate.setTimeInMillis(endDate);
        return gDate;
    }

    /**
     * return true if date was set
     * @param endDate
     * @return
     */
    @Exclude
    public boolean setEndDate(GregorianCalendar endDate) {
        if(endDate == null || (startDate != 0.0 &&
                MethodOrphanage.compareDates(getStartDate(), endDate) > 0))
            return false;
        this.endDate = endDate.getTimeInMillis();
        return true;
    }

    public GregorianCalendar getStartTime() {
        GregorianCalendar gTime = new GregorianCalendar();
        gTime.setTimeInMillis(startTime);
        return gTime;
    }

    /**
     * return true if start date was set correctly
     * @param startTime
     */
    @Exclude
    public boolean setStartTime(GregorianCalendar startTime) {
        if(startTime == null
            // start time is after end, and on the same day
            || (endTime != 0.0 && (MethodOrphanage.compareTimes(startTime, getEndTime()) > 0 && sameDay())))
            return false;
        this.startTime = startTime.getTimeInMillis();
        return true;
    }

    public GregorianCalendar getEndTime() {
        GregorianCalendar gTime = new GregorianCalendar();
        gTime.setTimeInMillis(endTime);
        return gTime;
    }

    @Exclude
    public boolean setEndTime(GregorianCalendar endTime) {

        if(endTime == null
                // end time is before start, and on same day
                || (startTime != 0.0 && (MethodOrphanage.compareTimes(getStartTime()
                , endTime) > 0 && sameDay())))
            return false;
        this.endTime = endTime.getTimeInMillis();
        return true;

    }

    public String getLocation() {
        return location;
    }

    @Exclude
    public void setLocation(String location) {
        this.location=location;
    }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    public int getSuggestedAge() {
        return suggestedAge;
    }

    public void setSuggestedAge(int suggestedAge) {
        this.suggestedAge = suggestedAge;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getAttendees(){return this.attendees;}

    public void setAttendes(String attendees){this.attendees=attendees;}

    public String getEventId() { return eid; }

    @Exclude
    private void setEventId(String id) { eid = id; }

    public String getImage() {
        return image;
    }

    public void setImage(String img) {
        this.image = img;
    }

    public static Event fromSnapshot(DataSnapshot snapshot)
    {
        Event event = snapshot.getValue(Event.class);
        event.setEventId(snapshot.getKey());
        return event;
    }

    public String getFormattedStartDate()
    {
        return dateFormat.format(getStartDate().getTime());
    }
    public String getFormattedEndDate()
    {
        return dateFormat.format(getEndDate().getTime());
    }
    public String getFormattedStartTime()
    {
        return timeFormat.format(getStartTime().getTime());
    }
    public String getFormattedEndTime()
    {
        return timeFormat.format(getEndTime().getTime());
    }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        Event other = (Event) obj;

        if (other == null || getClass() != other.getClass())
            return false;

        if (eid != null)
            return eid.equals(other.eid);

        return title.equals(other.title) &&
                description.equals(other.description) &&
                startDate == other.startDate &&
                endDate == other.endDate &&
                startTime == other.startTime &&
                endTime == other.endTime &&
                location.equals(other.location) &&
                hostUid.equals(other.hostUid) &&
                suggestedAge == other.suggestedAge &&
                rating == other.rating;
    }
    public boolean sameDay()
    {
        if(getStartDate() == null
                || getEndDate() == null)
            return false;
        DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
        return comparator.compare(getStartDate(), getEndDate()) == 0;
    }


}