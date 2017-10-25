package Firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import android.location.Address;
import java.util.ArrayList;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class User {

    private String _firstName, _middleInitial, _lastName, _email, _age, _bio;
    private android.location.Address _address;
    private ArrayList<Event> _eventsAttending;
    //TODO https://firebase.google.com/docs/storage/android/start
    //private SomePictureType _photo
    private Settings _userSettings;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String _firstName, String _middleInitial, String _lastName, String _email, String _age,
                String _bio, Address _address, Settings _userSetting) {
        this._firstName = _firstName;
        this._middleInitial = _middleInitial;
        this._lastName = _lastName;
        this._email = _email;
        this._age = _age;
        this._bio = _bio;
        this._address = _address;
        this._userSettings = _userSetting;
    }


    /**GETTERS
     * & SETTERS*/

    public String get_firstName() {
        return _firstName;
    }

    public void set_firstName(String _firstName) {
        this._firstName = _firstName;
    }

    public String get_middleInitial() {
        return _middleInitial;
    }

    public void set_middleInitial(String _middleInitial) {
        this._middleInitial = _middleInitial;
    }

    public String get_lastName() {
        return _lastName;
    }

    public void set_lastName(String _lastName) {
        this._lastName = _lastName;
    }

    public String get_email() {
        return _email;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public String get_age() {
        return _age;
    }

    public void set_age(String _age) {
        this._age = _age;
    }

    public String get_bio() {
        return _bio;
    }

    public void set_bio(String _bio) {
        this._bio = _bio;
    }

    public Address get_address() {
        return _address;
    }

    public void set_address(Address _address) {
        this._address = _address;
    }

    public ArrayList<Event> get_eventsAttending() {
        return _eventsAttending;
    }

    public void set_eventsAttending(ArrayList<Event> _eventsAttending) {
        this._eventsAttending = _eventsAttending;
    }

    public Settings get_userSettings() {
        return _userSettings;
    }

    public void set_userSettings(Settings _userSettings) {
        this._userSettings = _userSettings;
    }



}