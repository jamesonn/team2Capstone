package Firebase;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import team2.mkesocial.Activities.BaseActivity;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Settings {

    final public static String DB_SETTINGS = "settings";
    final public static String DB_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    final public static String DB_PRIVATE_PROFILE = "privateProfile";
    //TODO theme class?
    //private Theme _theme;
    private static String notificationEnabled, privateProfile;
    //DB user setting ref
    final private static DatabaseReference settingsDBReference = FirebaseDatabase.getInstance()
            .getReference(Databasable.DB_USERS_NODE_NAME).child(BaseActivity.getUid()).child(DB_SETTINGS);

    private static final String TAG = Settings.class.getSimpleName();

    public Settings() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    @Exclude
    public static Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DB_NOTIFICATIONS_ENABLED, getNotificationEnabled());
        result.put(DB_PRIVATE_PROFILE, getPrivateProfile());

        return result;
    }

    /**
     * Takes a Consumer<Settings> </Settings>function and runs it on the current user's settings obj
     *  also and update argument, whether or not to update obj in DB after running the method
     * @param function_to_run_on_settings_obj
     * @param update
     */
    public static void runMethodOnDBSettingsObj(
            final Consumer<Settings> function_to_run_on_settings_obj, boolean update)
    {
        //fetch user setting from DB
        settingsDBReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Settings userSettings = snapshot.getValue(Settings.class);
                if(userSettings != null){
                    // run passed function using fetched userSetting Obj
                    //*****ACCEPT FUNCTION REQUIRES API OF 24 OR GREATER
                    function_to_run_on_settings_obj.accept(userSettings);
                    if(update)
                        userSettings.update();

                } else {
                    //pass null if unable to run
                    function_to_run_on_settings_obj.accept(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Exception while getting Datasnapshot for user settings" + databaseError.toString());
                function_to_run_on_settings_obj.accept(null);
            }
        });
    }

    public static void update()
    {
        settingsDBReference.setValue(toMap());
    }


    public Settings(String notificationEnabled) {
        setNotificationEnabled(notificationEnabled);
    }

    public static String getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(String notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
    public static String getPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(String privateProfile) {
        this.privateProfile = privateProfile;
    }
}