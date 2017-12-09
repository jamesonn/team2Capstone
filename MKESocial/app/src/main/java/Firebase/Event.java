package Firebase;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;

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
    private long startDate, endDate, startTime, endTime;
    private String location;
    private String hostUid;
    private int suggestedAge, rating;
    private double cost;
    private List<Tag> tags;
    //TODO link to other users https://developer.android.com/training/app-links/deep-linking.html
    private String attendees; //layout attendeesID:attendeesName attendeesID:attendeesName
    private String eid;


    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Event(String title, String description, GregorianCalendar startDate, GregorianCalendar endDate,
                 GregorianCalendar startTime, GregorianCalendar endTime, String location,
                 String hostUid, int suggestedAge, int rating, double cost, List<Tag> tags) {
        setTitle(title);
        setDescription(description);
        setStartDate(startDate);
        setEndDate(endDate);
        setStartTime(startTime);
        setEndTime(endTime);
        setLocation(location);
        setHostUid(hostUid);
        setSuggestedAge(suggestedAge);
        setRating(rating);
        setCost(cost);
        setTags(tags);
    }

    public Event(String title, String description, String startDate, String endDate, String startTime,
                 String endTime, String location, String hostUid, String suggestedAge, String rating,
                 String cost, String tags) {
        setTitle(title);
        setDescription(description);
        setStartDate(parseDate(startDate));
        setEndDate(parseDate(endDate));
        setStartTime(parseTime(startTime));
        setEndTime(parseTime(endTime));
        setLocation(location);
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

    public static GregorianCalendar parseDate(String cDate)
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

    public static GregorianCalendar parseTime(String cTime)
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

    public GregorianCalendar getStartDate() {
        GregorianCalendar gDate = new GregorianCalendar();
        gDate.setTimeInMillis(startDate);
        return gDate;
    }

    @Exclude
    public void setStartDate(GregorianCalendar date) {
        this.startDate = date.getTimeInMillis();
    }

    public GregorianCalendar getEndDate() {
        GregorianCalendar gDate = new GregorianCalendar();
        gDate.setTimeInMillis(endDate);
        return gDate;
    }

    @Exclude
    public void setEndDate(GregorianCalendar date) {
        this.endDate = date.getTimeInMillis();
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

    public String getLocation() {
        return location;
    }

    @Exclude
    public void setLocation(String location) {
        this.location=location;
    }

    public String getFullAddress(){
        String fullAddress;//0000 Street Name, City, State Zip, Country:LatLng:(0,0)
                           //City, State Zip, Country:LatLng:(0,0)
                           //State Zip, Country:LatLng:(0,0)
        fullAddress = location.substring(0, location.indexOf(":"));
        String[] addr = fullAddress.split(",");
        //for loop to append the stuff together and then return it
        String firstPart = " ";
        for(int i=0;i<addr.length;++i){
            firstPart+=addr[i]+"\n";
        }
        return firstPart;
    }

    public Double getLat(){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = location.substring(location.indexOf("(") + 1, location.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[0]);
    }

    public Double getLng(){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = location.substring(location.indexOf("(") + 1, location.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[1]);
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

    public static Event fromSnapshot(DataSnapshot snapshot)
    {
        Event event = snapshot.getValue(Event.class);
        event.setEventId(snapshot.getKey());
        return event;
    }

    @Exclude
    @Override
    public boolean equals(Object obj)
    {
        Event other = (Event)obj;

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

}