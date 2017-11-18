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

    private String name, email, age, bio, uid;
    private String address;
    // store which events a User is Attending and created/hosting
    private List<String> eventIDsAttending, eventIDsHosting;

    //TODO https://firebase.google.com/docs/storage/android/start
    //storing user profile photo
    //private SomePictureType _photo

    // storing user preferences & visibility options of their bio info
    private Settings userSettings;

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
        setName(name);
        setEmail(email);
        setAge("");
        setBio("");
        setAddress(address);
        // give a new user default settings when they are created
        setUserSettings(new Settings());
        // push new user to DB
        String uid = userDatabase.push().getKey();
        setUid(uid);
        eventIDsAttending = new ArrayList<String>();
        eventIDsHosting = new ArrayList<String>();

    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", getName());
        result.put("email", getEmail());
        result.put("age", getAge());
        result.put("bio", getBio());
        result.put("uid", getUid());
        result.put("address", address);
        result.put("eventIDsAttending", getEventIDsAttending());
        result.put("eventIDsHosting", getEventIDsHosting());

        return result;
    }

    private Address parseAddress(String addr)
    {
        Address address = new Address(Locale.getDefault());
        address.setAddressLine(0, addr);
        return address;
    }

    /**
     * Adds event ID to user's event's attending list
     *  and resets the user's "Events attending" node containing the IDs of what events
     *      the user is attending
     * @param eventId
     */
    public void attendEvent(String eventId)
    {
        eventIDsAttending.add(eventId);
        userAttendingEventDatabase.child(getUid()).setValue(getEventIDsAttending().toString());

    }

    public void hostEvent(String eventId)
    {
        eventIDsHosting.add(eventId);
        userHostEventDatabase.child(getUid()).setValue(getEventIDsAttending().toString());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUid() {return uid;}

    public void setUid(String uid) { this.uid = uid;}

    public Address getAddress() {
        return parseAddress(address);
    }

    @Exclude
    public void setAddress(Address address) {
        String addr = address.getAddressLine(0);
        this.address = addr != null ? addr : "";
    }

    public List<String> getEventIDsAttending() {
        return eventIDsAttending;
    }

    public Settings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(Settings userSettings) {
        this.userSettings = userSettings;
    }

    public List<String> getEventIDsHosting() { return eventIDsHosting;}

}
