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

    final public static String DB_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    final public static String DB_PRIVATE_PROFILE = "privateProfile";
    final public static String DB_THEME = "theme";
    final public static String DB_FULL_NAME = "displayFirstLastName";
    //private Theme _theme;
    private static String notificationEnabled, privateProfile, theme, fullName;
    //DB user setting ref
    final private static DatabaseReference settingsDBReference = FirebaseDatabase.getInstance()
            .getReference(Databasable.DB_USER_SETTINGS_NODE_NAME).child(BaseActivity.getUid());

    private static final String TAG = Settings.class.getSimpleName();

    public Settings() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)

    }

    public static Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DB_NOTIFICATIONS_ENABLED, getNotificationEnabled2());
        result.put(DB_PRIVATE_PROFILE, getPrivateProfile2());
        result.put(DB_THEME, getTheme2());
        result.put(DB_FULL_NAME, getFullName2());

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

                if(userSettings == null)
                    //Give user a settings obj
                    userSettings = new Settings();

                // run passed function using fetched userSetting Obj
                //*****ACCEPT FUNCTION REQUIRES API OF 24 OR GREATER
                if(function_to_run_on_settings_obj != null)
                    function_to_run_on_settings_obj.accept(userSettings);
                if(update)
                    userSettings.update();
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

    @Exclude
    final public static String getNotificationEnabled2() {
        return notificationEnabled;
    }

    public String getNotificationEnabled(){return  notificationEnabled; }

    public void setNotificationEnabled(String notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
    @Exclude
    public static String getPrivateProfile2() {
        return privateProfile;
    }
    public String getPrivateProfile(){return privateProfile;}

    public void setPrivateProfile(String privateProfile) {
        this.privateProfile = privateProfile;
    }

    @Exclude
    public static String getTheme2() {
        return theme;
    }

    public String getTheme(){ return theme;}

    public void setTheme(String theme) {
        Settings.theme = theme;
    }


    //ToDO: (in a way it is EXTRA)
    //Idea: Allow user to determine if he wants FirstName + LastName to be shown publicly .. by default only shows firstName publicly

    @Exclude
    public static String getFullName2(){return fullName;}
    public String getFullName(){return fullName;}

    public void setFullName(String fullName){Settings.fullName=fullName;}

    public static boolean setDarkTheme()
    {
        return Settings.getTheme2() != null && Boolean.parseBoolean(Settings.getTheme2());
    }




}