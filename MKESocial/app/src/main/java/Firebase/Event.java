package Firebase;


import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Validation.WordScrubber;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Event implements Databasable, Cloneable {

    //Stuff to not store
    @Exclude
    private static final String TAG = Event.class.getSimpleName();
    @Exclude
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
    @Exclude
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    //Stuff to store
    private String title, description;
    private long startDate, endDate, startTime, endTime;
    private String location;
    private String hostUid;
    private int suggestedAge, rating;
    private double cost;
    private List<Tag> tags;
    private String attendees; //layout attendeesID:attendeesName attendeesID:attendeesName
    private String eventId;
    private String image; //holds URL of image on firebase storage


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

    @Exclude
    public boolean setTitle(String title) {
        this.title = title;
        return true;
    }

    public String getDescription() {
        return description;
    }

    @Exclude
    public boolean setDescription(String description) {
        if(description == null || description.isEmpty()) return false;
        this.description = description;//new WordScrubber().filterOffensiveWords(description, c);
        return true;
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

    // Needs to be in format: address : latlong
    @Exclude
    public boolean setLocation(String location) {
        try
        {//should be able to format it
            MethodOrphanage.getLat(location);
            MethodOrphanage.getLng(location);
            MethodOrphanage.getFullAddress(location);
        }catch (Exception e)
        {
            return false;
        }
        this.location=location;
        return true;
    }

    @Exclude
    public boolean setLocation(Place placePickerLoc) {
        if(placePickerLoc == null || placePickerLoc.getAddress().toString().isEmpty())
            return false;
        this.location = placePickerLoc.getAddress().toString()+":"+placePickerLoc.getLatLng();
        return true;
    }

    public String getHostUid() {
        return hostUid;
    }

    @Exclude
    public boolean setHostUid(String hostUid) {
        if(hostUid == null || hostUid.isEmpty()) return false;
        this.hostUid = hostUid;
        return true;
    }

    public int getSuggestedAge() {
        return suggestedAge;
    }

    @Exclude
    public boolean setSuggestedAge(int suggestedAge) {
        if(suggestedAge > 120 || suggestedAge < 0)
            return false;
        this.suggestedAge = suggestedAge;
        return true;
    }

    public int getRating() {
        return rating;
    }

    @Exclude
    public boolean setRating(int rating) {
        if(rating < 0 || rating > 5) return false;
        this.rating = rating;
        return true;
    }

    public double getCost() {
        return cost;
    }

    //cost is in double format so 2.00 = 200
    @Exclude
    public boolean setCost(double cost) {
        if(cost < 0 || cost > 50000)
            return false;
        this.cost = cost;
        return true;
    }

    public List<Tag> getTags() {
        return tags;
    }

    @Exclude
    public boolean setTags(List<Tag> tags) {
        if(tags == null || tags.isEmpty()) return false;
        this.tags = tags;
        return true;
    }

    public String getAttendees(){return this.attendees;}

    @Exclude
    public boolean setAttendes(String attendees){
        if(attendees == null || attendees.isEmpty()) return false;
        this.attendees=attendees;
        return true;
    }

    public String getEventId() { return eventId; }

    @Exclude
    private boolean setEventId(String id) {
        if(id == null || id.isEmpty()) return false;
        eventId = id;
        return true;}

    public String getImage() {
        return image;
    }

    @Exclude
    public boolean setImage(String img) {
        if(img == null || img.isEmpty()) return false;
        this.image = img;
        return true;
    }

    /**
     * get event object from snapshot
     * @param snapshot
     * @return
     */
    public static Event fromSnapshot(DataSnapshot snapshot)
    {
        Event event = snapshot.getValue(Event.class);
        //Event is not in database
        if(event == null) return  event;
        event.setEventId(snapshot.getKey());
        return event;
    }

    @Exclude
    public String getFormattedStartDate() { return dateFormat.format(getStartDate().getTime());}
    @Exclude
    public String getFormattedEndDate() {return dateFormat.format(getEndDate().getTime()); }
    @Exclude
    public String getFormattedStartTime() { return timeFormat.format(getStartTime().getTime()); }
    @Exclude
    public String getFormattedEndTime() { return timeFormat.format(getEndTime().getTime()); }

    @Exclude
    @Override
    public boolean equals(Object obj) {
        Event other = (Event) obj;

        if (other == null || getClass() != other.getClass())
            return false;

        if (eventId != null)
            return eventId.equals(other.eventId);

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

    @Override
    public Object clone()
    {
        Event event = null;
        try {
            event = (Event)super.clone();
        } catch (CloneNotSupportedException e) {
            Log.wtf(TAG, e.toString());
        }

        event.tags = new ArrayList<>(tags);
        return event;
    }

    public boolean sameDay()
    {
        if(getStartDate() == null
                || getEndDate() == null)
            return false;
        DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
        return comparator.compare(getStartDate(), getEndDate()) == 0;
    }

    @Exclude
    public boolean overlaps(Event other) {
        Interval thisSpan = new Interval(startDate + startTime, endDate + endTime);
        Interval otherSpan = new Interval(other.startDate + other.startTime, other.endDate + other.endTime);

        return thisSpan.overlaps(otherSpan);
    }

    @Exclude
    public boolean overlaps(BusyTime busyTime) {
        Interval thisSpan = new Interval(startDate + startTime, endDate + endTime);
        Interval otherSpan = new Interval(busyTime.getStartTime(), busyTime.getEndTime());

        return thisSpan.overlaps(otherSpan);
    }
}