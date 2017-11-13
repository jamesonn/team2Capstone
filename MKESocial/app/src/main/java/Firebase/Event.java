package Firebase;


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

    private String _title, _description;
    private GregorianCalendar _date, _startTime, _endTime;
    private Address _location;
    private String _hostUid;
    private int _suggestedAge, _rating;
    private double _cost;
    private List<Tag> _tags;
    //TODO link to other users https://developer.android.com/training/app-links/deep-linking.html
    private List<String> _attendeesUids;


    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Event(String title, String description, GregorianCalendar date,
                 GregorianCalendar startTime, GregorianCalendar endTime, Address location,
                 String hostUid, int suggestedAge, int rating, double cost, List<Tag> tags) {
        set_title(title);
        set_description(description);
        set_date(date);
        set_startTime(startTime);
        set_endTime(endTime);
        set_location(location);
        set_hostUid(hostUid);
        set_suggestedAge(suggestedAge);
        set_rating(rating);
        set_cost(cost);
        set_tags(tags);
    }

    public Event(String title, String description, String date, String startTime, String endTime,
                 String location, String hostUid, String suggestedAge, String rating, String cost,
                 String tags) {
        set_title(title);
        set_description(description);
        set_date(parseDate(date));
        set_startTime(parseTime(startTime));
        set_endTime(parseTime(endTime));
        set_location(parseLocation(location));
        set_hostUid(hostUid);
        set_suggestedAge(parseInt(suggestedAge));
        set_rating(parseInt(rating));
        set_cost(parseInt(cost));
        set_tags(parseTags(tags));
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
        if(isInteger(number, number.length()))
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
        Address location = new Address(new Locale(loc));
        return location;
    }

    private GregorianCalendar parseDate(String cDate)
    {
        DateFormat df = new SimpleDateFormat("dd MM yyyy");
        Date date = new Date();
        GregorianCalendar gDate = new GregorianCalendar();
        try {
            date = df.parse(cDate);
        }
        catch(java.text.ParseException e){
            Log.w(TAG, "Date not converted: "+ cDate);
        }
        gDate.setGregorianChange(date);
        return gDate;
    }

    private GregorianCalendar parseTime(String cTime)
    {
        DateFormat df = new SimpleDateFormat("HH:mm");
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
        result.put("title", get_title());
        result.put("description", get_description());
        result.put("date", get_date().toString());
        result.put("startTime", get_startTime().toString());
        result.put("endTime", get_endTime().toString());
        result.put("location", get_location().toString());
        result.put("hostUid", get_hostUid());
        result.put("suggestedAge", get_suggestedAge());
        result.put("rating", get_rating());
        result.put("cost", get_cost());
        result.put("tags", get_tags().toString());

        return result;
    }
    //GETTERS & SETTERS
    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public GregorianCalendar get_date() {
        return _date;
    }

    public void set_date(GregorianCalendar _date) {
        this._date = _date;
    }

    public GregorianCalendar get_startTime() {
        return _startTime;
    }

    public void set_startTime(GregorianCalendar _startTime) {
        this._startTime = _startTime;
    }

    public GregorianCalendar get_endTime() {
        return _endTime;
    }

    public void set_endTime(GregorianCalendar _endTime) {
        this._endTime = _endTime;
    }

    public Address get_location() {
        return _location;
    }

    public void set_location(Address _location) {
        this._location = _location;
    }

    public String get_hostUid() {
        return _hostUid;
    }

    public void set_hostUid(String _hostUid) {
        this._hostUid = _hostUid;
    }

    public int get_suggestedAge() {
        return _suggestedAge;
    }

    public void set_suggestedAge(int _suggestedAge) {
        this._suggestedAge = _suggestedAge;
    }

    public int get_rating() {
        return _rating;
    }

    public void set_rating(int _rating) {
        this._rating = _rating;
    }

    public double get_cost() {
        return _cost;
    }

    public void set_cost(double _cost) {
        this._cost = _cost;
    }

    public List<Tag> get_tags() {
        return _tags;
    }

    public void set_tags(List<Tag> _tags) {
        this._tags = _tags;
    }

    public List<String> get_attendeesUids() {
        return _attendeesUids;
    }

    public void set_attendeesUids(List<String> _attendeesUids) {
        this._attendeesUids = _attendeesUids;
    }


}