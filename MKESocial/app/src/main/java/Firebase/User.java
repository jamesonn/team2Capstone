package Firebase;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

    @Exclude
    public static final String DB_ADDRESS = "address";
    @Exclude
    public static final String DB_AGE = "age";
    @Exclude
    public static final String DB_ATTENDING_IDS = "attendEid";
    @Exclude
    public static final String DB_BIO = "bio";
    @Exclude
    public static final String DB_SHOW_EVENTS_ATTENDING = "eattend";
    @Exclude
    public static final String DB_SHOW_EVENTS_MAYBE = "emaybe";
    @Exclude
    public static final String DB_SHOW_EVENTS_HOSTING = "ehost";
    @Exclude
    public static final String DB_SHOW_EMAIL = "etog";
    @Exclude
    public static final String DB_MAYBE_IDS = "maybeEid";
    @Exclude
    public static final String DB_HOSTING_IDS = "hostEid";
    @Exclude
    public static final String DB_IMAGE = "img";
    @Exclude
    public static final String DB_M_INITIAL = "initm";
    @Exclude
    public static final String DB_L_NAME = "lname";



    private String name, email, age, bio, lname, initm; //info fields
    private String address;
    private String eattend, ehost, etog, emaybe; //display those email and/or events on profile page or not "true" or "false"
    private String attend, host, maybe; //layout eventID:eventName eventID:eventName
    private String img; //holds URL of image on firebase storage
    private String uid; //to help with viewing other profiles
	private List<BusyTime> busyTimes;


    //FIREBASE DB "users" node reference
    final private static DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);

    private static final String TAG = User.class.getSimpleName();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(FirebaseUser fbUser)
    {
        //grab Firebase Authentication to fill in user info
        setAddress("");
        setAge("");
        setAttendEid("");
        setMaybeEid("");
        setBio("");
        setEattend("true");
        setEhost("true");
        setEmaybe("true");
        setEmail(fbUser.getEmail());
        setEtog("true");
        setHostEid("");
        setImg("");
        setInitm("");
        setLname("");
        setName(fbUser.getDisplayName());

    }

    public User(GoogleSignInAccount googleUser)
    {
        setAddress("");
        setAge("");
        setAttendEid("");
        setMaybeEid("");
        setBio("");
        setEattend("true");
        setEhost("true");
        setEmaybe("true");
        setEmail(googleUser.getEmail());
        setEtog("true");
        setHostEid("");
        setImg("");
        setInitm("");
        setLname("");
        setName(googleUser.getDisplayName());
    }

    /**
     * Creates a user object assigns a unique User ID and saves it to database
     * @param name
     * @param email
     * @param address
     */
    public User(String name, String email, String address) {
        setAddress(address);
        setAge("");
        setAttendEid("");
        setMaybeEid("");
        setBio("");
        setEattend("true");
        setEhost("true");
        setEmaybe("true");
        setEmail(email);
        setEtog("true");
        setHostEid("");
        setImg("");
        setInitm("");
        setLname("");
        setName(name);
		busyTimes = new ArrayList<BusyTime>();

    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", getName());
        result.put("email", getEmail());
        result.put("age", getAge());
        result.put("bio", getBio());
        result.put("emaybe", getEmaybe());
        result.put("lname", getLname());
        result.put("initm", getInitm());
        result.put("eattend", getEattend());
        result.put("ehost", getEhost());
        result.put("etog", getEtog());
        result.put("address", getAddress());
        result.put("attendEid", getAttendEid());
        result.put("hostEid", getHostEid());
        result.put("maybeEid", getMaybeEid());
        result.put("img", getImg());
        result.put("busyTimes", getBusyTimes());

        return result;
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

    public boolean setName(String name) {
        if(name == null || name.isEmpty()) return false;
        this.name = name;//new WordScrubber().filterOffensiveWords(description, c);
        return true;
    }

    public String getEmail() {
        return email;
    }

    public boolean setEmail(String email) {
        if(email == null || email.isEmpty()) return false;
        this.email = email;//new WordScrubber().filterOffensiveWords(description, c);
        return true;
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

    public boolean setBio(String bio) {
        if(bio == null || bio.isEmpty()) return false;
        this.bio = bio;//new WordScrubber().filterOffensiveWords(description, c);
        return true;
    }

    public String getLname(){return lname;}

    public boolean setLname(String lname){
        if(lname == null || lname.isEmpty()) return false;
        this.lname = lname;//new WordScrubber().filterOffensiveWords(description, c);
        return true;
    }

    public String getInitm(){return initm;}

    public void setInitm(String initm){this.initm=initm;}

    public String getEattend(){return eattend;}

    public void setEattend(String eattend){this.eattend=eattend;}

    public String getEmaybe(){return emaybe;}

    public void setEmaybe(String eattend){this.emaybe=eattend;}

    public String getEtog(){return etog;}

    public void setEtog(String etog){this.etog=etog;}

    public String getEhost(){return ehost;}

    public void setEhost(String ehost){this.ehost=ehost;}

    public String getAddress() {
        return address;
    }

    public String getAttendEid(){return attend;}

    public void setAttendEid(String attend){this.attend=attend;}

    public String getHostEid(){return host;}

    public void setHostEid(String host){this.host=host;}

    public String getMaybeEid(){return maybe;}

    public void setMaybeEid(String maybe){this.maybe=maybe;}


    public String parseEventAttendNames(){
        //id`name`id`name`id`name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = attend.split("`");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length;++i){
            //names will be at the odd i values: 1 3 5 ...
            if((i%2)==1){parsed+=parsing[i]+"`";}
        }
        //name`name`name` ....
        return parsed;
    }

    public String parseEventAttendIDs(){
        //id`name`id`name`id`name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = attend.split("`");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length;++i){
            //ids will be at the even values: 0 2 4 ...
            if((i%2)==0){parsed+=parsing[i]+"`";}
        }
        //id`id`id` ....
        return parsed;
    }

    public String parseEventHostNames(){
        ///id`name`id`name`id`name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = host.split("`");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length;++i){
            //names will be at the odd i values: 1 3 5 ...
            if((i%2)==1){parsed+=parsing[i]+"`";}
        }
        //name`name`name` ....
        return parsed;
    }

    public String parseEventHostIDs(){
        //id`name`id`name`id`name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = host.split("`");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length;++i){
            //ids will be at the even values: 0 2 4 ...
            if((i%2)==0){parsed+=parsing[i]+"`";}
        }
        //id`id`id` ....
        return parsed;
    }

    public String parseEventMaybeNames(){
        ///id`name`id`name`id`name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = maybe.split("`");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length;++i){
            //names will be at the odd i values: 1 3 5 ...
            if((i%2)==1){parsed+=parsing[i]+"`";}
        }
        //name`name`name` ....
        return parsed;
    }

    public String parseEventMaybeIDs(){
        //id`name`id`name`id`name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = maybe.split("`");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length;++i){
            //ids will be at the even values: 0 2 4 ...
            if((i%2)==0){parsed+=parsing[i]+"`";}
        }
        //id`id`id` ....
        return parsed;
    }


    public String getImg(){return img;}

    public void setImg(String img){this.img=img;}

    @Exclude
    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullAddress(){
        String fullAddress;//0000 Street Name, City, State Zip, Country:LatLng:(0,0)
        //City, State Zip, Country:LatLng:(0,0)
        //State Zip, Country:LatLng:(0,0)
        fullAddress = address.substring(0, address.indexOf(":"));
        String[] addr = fullAddress.split(",");
        //for loop to append the stuff together and then return it
        String firstPart = " ";
        for(int i=0;i<addr.length;++i){
            firstPart+=addr[i]+"\n";
        }
        return firstPart;
    }

	public List<BusyTime> getBusyTimes() { return busyTimes; }

    public String getUserId() { return uid; }

    @Exclude
    private void setUserId(String id) { uid = id; }

    public static User fromSnapshot(DataSnapshot snapshot)
    {
        try {
            User user = snapshot.getValue(User.class);
            user.setUserId(snapshot.getKey());
            return user;
        }catch (Exception e)
        {
            Log.e("","Unable to fetch user");
        }
        return null;
    }

}