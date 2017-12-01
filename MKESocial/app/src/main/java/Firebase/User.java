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

    private String name, email, age, bio, lname, initm; //info fields
    private String address;
    private String eattend, ehost, etog; //display those email and/or events on profile page or not "true" or "false"
    private String attend, host; //layout eventID:eventName eventID:eventName
    private String img; //holds URL of image on firebase storage
    private String uid; //to help with viewing other profiles



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
        setBio("");
        setEattend("true");
        setEhost("true");
        setEmail(fbUser.getEmail());
        setEtog("true");
        setHostEid("");
        setImg("");
        setInitm("");
        setLname("");
        setName(fbUser.getDisplayName());

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
        setBio("");
        setEattend("true");
        setEhost("true");
        setEmail(email);
        setEtog("true");
        setHostEid("");
        setImg("");
        setInitm("");
        setLname("");
        setName(name);


    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", getName());
        result.put("email", getEmail());
        result.put("age", getAge());
        result.put("bio", getBio());
        result.put("lname", getLname());
        result.put("initm", getInitm());
        result.put("eattend", getEattend());
        result.put("ehost", getEhost());
        result.put("etog", getEtog());
        result.put("address", getAddress());
        result.put("attendEid", getAttendEid());
        result.put("hostEid", getHostEid());
        result.put("img", getImg());

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

    public String getLname(){return lname;}

    public void setLname(String lname){this.lname=lname;}

    public String getInitm(){return initm;}

    public void setInitm(String initm){this.initm=initm;}

    public String getEattend(){return eattend;}

    public void setEattend(String eattend){this.eattend=eattend;}

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

    //ToDo: Fix the below
    public String parseEventAttendIDs(String temp){
        //id:name:id:name:id:name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = temp.split(":");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length-1;++i){
            //Parse again, so names will be at the odd even values: 0 2 4 6 ...
            if((i%2)==0){parsed+=parsing[i]+" ";}
        }
        //id id id ....
        return parsed.toString();
    }

    public String parseEventAttendNames(String temp){
        //id:name:id:name:id:name...  <-- layout of information stored at String host
        String parsed = "";;
        String[] parsing = temp.split(":");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length-1;++i){
            //Parse again, so names will be at the odd i values: 1 3 5 ...
            if((i%2)!=0){parsed+=parsing[i]+" ";}
        }
        //name name name ....
        return parsed.toString();
    }

    public String parseEventHostIDs(String temp){
        //id:name:id:name:id:name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = temp.split(":");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length-1;++i){
            //Parse again, so names will be at the odd even values: 0 2 4 6 ...
            if((i%2)==0){parsed+=parsing[i]+" ";}
        }
        //id id id ....
        return parsed;
    }

    public String parseEventHostNames(String temp){
        //id:name:id:name:id:name...  <-- layout of information stored at String host
        String parsed = "";
        String[] parsing = temp.split(":");//id|name|id|name|id|name...
        for(int i=0; i<parsing.length-1;++i){
            //Parse again, so names will be at the odd i values: 1 3 5 ...
            if((i%2)!=0){parsed+=parsing[i]+" ";}
        }
        //name name name ....
        return parsed;
    }

    public String getImg(){return img;}

    public void setImg(String img){this.img=img;}

    @Exclude
    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullAddress(){
        String fullAddress;//0000 Street Name, City, State Zip, Country LatLng:(0,0)
        fullAddress = address.substring(0, address.indexOf("("));//0000 Street Name, City, State Zip, Country LatLng:
        String[] addr = fullAddress.split(",");//0000 Street Name| City| State Zip| Country LatLng:
        String getCountry = addr[3];
        String[] sCountry = getCountry.split(" ");// |Country|LatLng:
        //0000 Street Name
        //City State Zip
        //Country
        return addr[0]+"\n"+addr[1].substring(1)+addr[2]+"\n"+sCountry[1];
    }

    public Double getLat(){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = address.substring(address.indexOf("(") + 1, address.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[0]);
    }

    public Double getLng(){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = address.substring(address.indexOf("(") + 1, address.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[1]);
    }

    public String getUserId() { return uid; }

    @Exclude
    private void setUserId(String id) { uid = id; }

    public static User fromSnapshot(DataSnapshot snapshot)
    {
        User user = snapshot.getValue(User.class);
        user.setUserId(snapshot.getKey());
        return user;
    }

}