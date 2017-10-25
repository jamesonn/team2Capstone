package Firebase;


import com.google.firebase.database.IgnoreExtraProperties;
//new class for Time formats in Java, yeah it's a weird name
import 	java.util.GregorianCalendar;
import android.location.Address;
import java.util.ArrayList;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Event {

    private String _title, _description;
    private GregorianCalendar _date, _startTime, _endTime;
    private Address _location;
    private User _host;
    private int _suggestedAge, _rating;
    private double _cost;
    private ArrayList<Tag> _tags;
    //TODO link to other users https://developer.android.com/training/app-links/deep-linking.html
    //private ArrayList<userLinks> _attendees;


    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Event(String _title, String _description, GregorianCalendar _date,
                 GregorianCalendar _startTime, GregorianCalendar _endTime, Address _location,
                 User _host, int _suggestedAge, int _rating, double _cost, ArrayList<Tag> _tags) {
        this._title = _title;
        this._description = _description;
        this._date = _date;
        this._startTime = _startTime;
        this._endTime = _endTime;
        this._location = _location;
        this._host = _host;
        this._suggestedAge = _suggestedAge;
        this._rating = _rating;
        this._cost = _cost;
        this._tags = _tags;
    }

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

    public User get_host() {
        return _host;
    }

    public void set_host(User _host) {
        this._host = _host;
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


}