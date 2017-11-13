package Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import android.location.Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import team2.mkesocial.Activities.BaseActivity;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class User implements Databasable{

    private String _name, _email, _age, _bio, _uid;
    private android.location.Address _address;
    // store which events a User is Attending and created/hosting
    private List<String> _eventIDsAttending, _eventIDsHosting;

    //TODO https://firebase.google.com/docs/storage/android/start
    //storing user profile photo
    //private SomePictureType _photo

    // storing user preferences & visibility options of their bio info
    private Settings _userSettings;

    //FIREBASE DB "users" node reference
    final private static DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    final private static DatabaseReference userAttendingEventDatabase = FirebaseDatabase.getInstance()
            .getReference(DB_USER_EVENTS_ATTENDING_NODE_NAME);
    final private static DatabaseReference userHostEventDatabase = FirebaseDatabase.getInstance()
            .getReference(DB_USER_EVENTS_HOSTING_NODE_NAME);

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Creates a user object assigns a unique User ID and saves it to database
     * @param name
     * @param email
     * @param address
     */
    public User(String name, String email, Address address) {
        set_name(name);
        set_email(email);
        set_age("");
        set_bio("");
        set_address(address);
        // give a new user default settings when they are created
        set_userSettings(new Settings());
        // push new user to DB
        String uid = userDatabase.push().getKey();
        set_uid(uid);
        _eventIDsAttending = new ArrayList<String>();
        _eventIDsHosting = new ArrayList<String>();

    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", get_name());
        result.put("email", get_email());
        result.put("age", get_age());
        result.put("bio", get_bio());
        result.put("uid", get_uid());
        result.put("address", get_address().toString());
        result.put("eventIDsAttending", get_eventIDsAttending().toString());
        result.put("eventIDsHosting", get_eventIDsHosting().toString());

        return result;
    }

    /**
     * Adds event ID to user's event's attending list
     *  and resets the user's "Events attending" node containing the IDs of what events
     *      the user is attending
     * @param eventId
     */
    public void attendEvent(String eventId)
    {
        _eventIDsAttending.add(eventId);
        userAttendingEventDatabase.child(get_uid()).setValue(get_eventIDsAttending().toString());

    }

    public void hostEvent(String eventId)
    {
        _eventIDsHosting.add(eventId);
        userHostEventDatabase.child(get_uid()).setValue(get_eventIDsAttending().toString());
    }

    /**
     * Adds a user object into the database under 'users' node under the Firebase Authorization unique
     *  id node
     *      i.e. "/users/FirebaseAuth.getInstance().getCurrentUser().getUid();"
     * @param userObj
     */
    public static void addUser(User userObj)
    {
        //Push user into DB
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = BaseActivity.getUid();
        UserInfo currentUser = mAuth.getCurrentUser();

        // default user object created with Firebase User Authentication info
        if(userObj == null)
        {
            userObj = new User(currentUser.getDisplayName(), currentUser.getEmail(),
                    new Address(new Locale("English")));
        }
        // create hash map from User Java Obj's fields
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + userId, userObj.toMap());
        // add new User child under 'users' node
        userDatabase.updateChildren(userUpdates);
    }


    public static void getUser(String userId)
    {
        // Query User database looking for the matching user ID
        userDatabase.orderByChild("uid").equalTo(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(dataSnapshot.getKey());

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey)
            {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey)
            {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
           {

           }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });


    }


    /**GETTERS
     * & SETTERS*/

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
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

    public String get_uid() {return _uid;}

    public void set_uid(String _uid) { this._uid = _uid;}

    public Address get_address() {
        return _address;
    }

    public void set_address(Address _address) {
        this._address = _address;
    }

    public List<String> get_eventIDsAttending() {
        return _eventIDsAttending;
    }

    public Settings get_userSettings() {
        return _userSettings;
    }

    public void set_userSettings(Settings _userSettings) {
        this._userSettings = _userSettings;
    }

    public List<String> get_eventIDsHosting() { return _eventIDsHosting;}

}
