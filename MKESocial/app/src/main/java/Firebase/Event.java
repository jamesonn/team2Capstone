package Firebase;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;

//new class for Time formats in Java, yeah it's a weird name
import 	java.util.GregorianCalendar;
import android.location.Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Event {

    private String _title, _description;
    private GregorianCalendar _date, _startTime, _endTime;
    private Address _location;
    private String _hostUid;
    private int _suggestedAge, _rating;
    private double _cost;
    private ArrayList<Tag> _tags;
    //TODO link to other users https://developer.android.com/training/app-links/deep-linking.html
    private ArrayList<String> _attendeesUids;


    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Event(String title, String description, GregorianCalendar date,
                 GregorianCalendar startTime, GregorianCalendar endTime, Address location,
                 String hostUid, int suggestedAge, int rating, double cost, ArrayList<Tag> tags) {
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
        //TODO String -> object conversions
//        set_date(date);
//        set_startTime(startTime);
//        set_endTime(endTime);
//        set_location(location);
//        set_hostUid(hostUid);
//        set_suggestedAge(suggestedAge);
//        set_rating(rating);
//        set_cost(cost);
//        set_tags(tags);
    }

    //MAP to store in DB
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", get_title());
        result.put("description", get_description());
//        result.put("date", get_title());
//        result.put("startTime", get_title());
//        result.put("endTime", get_title());
//        result.put("location", get_title());
//        result.put("hostUid", get_title());
//        result.put("suggestedAge", get_title());
//        result.put("rating", get_title());
//        result.put("cost", get_title());
//        result.put("tags", get_title());

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

    public ArrayList<Tag> get_tags() {
        return _tags;
    }

    public void set_tags(ArrayList<Tag> _tags) {
        this._tags = _tags;
    }

    public ArrayList<String> get_attendeesUids() {
        return _attendeesUids;
    }

    public void set_attendeesUids(ArrayList<String> _attendeesUids) {
        this._attendeesUids = _attendeesUids;
    }


}