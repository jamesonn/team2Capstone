package Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import android.util.Log;

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

    private String name, email, age, bio;
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

    private static final String TAG = User.class.getSimpleName();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(FirebaseUser fbUser)
    {
        //grab Firebase Authentication to fill in user info
        setName(fbUser.getDisplayName());
        setEmail(fbUser.getEmail());
        setAge("");
        setBio("");
        setAddress("");
        // give a new user default settings when they are created
        setUserSettings(new Settings());
        // push new user to DB
        eventIDsAttending = new ArrayList<String>();
        eventIDsHosting = new ArrayList<String>();

    }

    /**
     * Creates a user object assigns a unique User ID and saves it to database
     * @param name
     * @param email
     * @param address
     */
    public User(String name, String email, String address) {
        setName(name);
        setEmail(email);
        setAge("");
        setBio("");
        setAddress(address);
        // give a new user default settings when they are created
        setUserSettings(new Settings());
        // push new user to DB
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
        result.put("address", address);
        result.put("eventIDsAttending", getEventIDsAttending());
        result.put("eventIDsHosting", getEventIDsHosting());
        result.put("settings", getUserSettings().toMap());

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
        userAttendingEventDatabase.child(BaseActivity.getUid()).setValue(getEventIDsAttending().toString());

    }

    public void hostEvent(String eventId)
    {
        eventIDsHosting.add(eventId);
        userHostEventDatabase.child(BaseActivity.getUid()).setValue(getEventIDsAttending().toString());
    }

    /**
     * Adds a user object into the database under 'users' node under the Firebase Authorization unique
     *  id node
     *      i.e. "/users/FirebaseAuth.getInstance().getCurrentUser().getUid();"
     */
    public void add() {
        final String userId = BaseActivity.getUid();
        // Check if the Authenicated user id node already exists in FB DB under "users/$userId"
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(userId)) {
                    // check if any fields have to be updated
                    User currentUser = snapshot.child(userId).getValue(User.class);
                    //TODO updateUser(currentUser);
                } else {
                    // add new user to DB
                    // create hash map from User Java Obj's fields
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("/" + userId, toMap());
                    // add new User child under 'users' node
                    userDatabase.updateChildren(userUpdates);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Exception while getting Datasnapshot after logging in, was:" + databaseError.toString());
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

    public String getAddress() {
        return address;
    }

    @Exclude
    public void setAddress(String address) {
        this.address = address;
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
