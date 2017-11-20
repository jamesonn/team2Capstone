package Firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Settings {

    //TODO theme class?
    //private Theme _theme;
    private String notificationEnabled, privateProfile;


    public Settings() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Settings(String notificationEnabled) {
        setNotificationEnabled(notificationEnabled);
    }

    public String getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(String notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
    public String getPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(String privateProfile) {
        this.privateProfile = privateProfile;
    }
}