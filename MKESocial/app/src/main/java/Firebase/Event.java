package Firebase;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;

import android.location.Address;
import java.util.Locale;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.util.Log;
import java.util.List;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Event implements Databasable{

    private static final String TAG = Event.class.getSimpleName();

    private String title, description;
    private long date, startTime, endTime;
    private String location;
    private String hostUid;
    private int suggestedAge, rating;
    private double cost;
    private List<Tag> tags;
    //TODO link to other users https://developer.android.com/training/app-links/deep-linking.html
    private List<String> attendeesUids;
    private String eid;


    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Event(String title, String description, GregorianCalendar date,
                 GregorianCalendar startTime, GregorianCalendar endTime, Address location,
                 String hostUid, int suggestedAge, int rating, double cost, List<Tag> tags) {
        setTitle(title);
        setDescription(description);
        setDate(date);
        setStartTime(startTime);
        setEndTime(endTime);
        setLocation(location);
        setHostUid(hostUid);
        setSuggestedAge(suggestedAge);
        setRating(rating);
        setCost(cost);
        setTags(tags);
    }

    public Event(String title, String description, String date, String startTime, String endTime,
                 String location, String hostUid, String suggestedAge, String rating, String cost,
                 String tags) {
        setTitle(title);
        setDescription(description);
        setDate(parseDate(date));
        setStartTime(parseTime(startTime));
        setEndTime(parseTime(endTime));
        setLocation(parseLocation(location));
        setHostUid(hostUid);
        setSuggestedAge(parseInt(suggestedAge));
        setRating(parseInt(rating));
        setCost(parseInt(cost));
        setTags(parseTags(tags));
    }

    public List<Tag> parseTags(String tags)
    {
        ArrayList<Tag> tagArray = new ArrayList<Tag>();
        String[] tagSplit = tags.split("-|\\.|,");
        for(String tag: tagSplit)
            tagArray.add(new Tag(tag));
        return tagArray;

    }
    private int parseInt(String number)
    {
        if(number.isEmpty()) return -1;
        if(isInteger(number, 10))
            return Integer.parseInt(number);
        else
            return -1;
    }

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
    private Address parseLocation(String loc)
    {
        Address location = new Address(Locale.getDefault());
        double latitude = 0.0, longitude = 0.0;
        String addr = loc;
        String[] coords = loc.split(";", 3);
        try {
            addr = coords[0];
            latitude = Double.parseDouble(coords[1]);
            longitude = Double.parseDouble(coords[2]);
        }
        catch (Exception e) {
            Log.w(TAG, "Location not converted: "+ loc);
        }

        location.setAddressLine(0, addr);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    private GregorianCalendar parseDate(String cDate)
    {
        final String myFormat = "MM/dd/yy";
        DateFormat df = new SimpleDateFormat(myFormat, Locale.US);
        Date date = new Date();
        GregorianCalendar gDate = new GregorianCalendar();
        try {
            date = df.parse(cDate);
        }
        catch(java.text.ParseException e){
            Log.w(TAG, "Date not converted: "+ cDate);
        }
        gDate.setTime(date);
        return gDate;
    }

    private GregorianCalendar parseTime(String cTime)
    {
        final String displayFormat = "hh:mm a";
        DateFormat df = new SimpleDateFormat(displayFormat);
        Date time = new Date();
        GregorianCalendar gTime = new GregorianCalendar();
        try {
            time = df.parse(cTime);
        }
        catch(java.text.ParseException e){
            Log.w(TAG, "Date not converted: "+ cTime);
        }
        gTime.setTime(time);
        return gTime;
    }

    //MAP to store in DB
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", getTitle());
        result.put("description", getDescription());
        result.put("date", date);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("location", location);
        result.put("hostUid", getHostUid());
        result.put("suggestedAge", getSuggestedAge());
        result.put("rating", getRating());
        result.put("cost", getCost());
        result.put("tags", getTags());

        return result;
    }


    //GETTERS & SETTERS
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GregorianCalendar getDate() {
        GregorianCalendar gDate = new GregorianCalendar();
        gDate.setTimeInMillis(date);
        return gDate;
    }

    @Exclude
    public void setDate(GregorianCalendar date) {
        this.date = date.getTimeInMillis();
    }

    public GregorianCalendar getStartTime() {
        GregorianCalendar gTime = new GregorianCalendar();
        gTime.setTimeInMillis(startTime);
        return gTime;
    }

    @Exclude
    public void setStartTime(GregorianCalendar startTime) {
        this.startTime = startTime.getTimeInMillis();
    }

    public GregorianCalendar getEndTime() {
        GregorianCalendar gTime = new GregorianCalendar();
        gTime.setTimeInMillis(endTime);
        return gTime;
    }

    @Exclude
    public void setEndTime(GregorianCalendar endTime) {
        this.endTime = endTime.getTimeInMillis();
    }

    public Address getLocation() {
        return parseLocation(location);
    }

    @Exclude
    public void setLocation(Address location) {
        String addr = location.getAddressLine(0);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        this.location = String.format("%s;%f;%f", addr, latitude, longitude);
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

    public List<String> getAttendeesUids() {
        return attendeesUids;
    }

    public void setAttendeesUids(List<String> attendeesUids) {
        this.attendeesUids = attendeesUids;
    }

    public String getEventId() { return eid; }

    @Exclude
    private void setEventId(String id) { eid = id; }

    public static Event fromSnapshot(DataSnapshot snapshot)
    {
        Event event = snapshot.getValue(Event.class);
        event.setEventId(snapshot.getKey());
        return event;
    }

}