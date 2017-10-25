package Firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Settings {

    //TODO theme class?
    //private Theme _theme;
    private boolean _notificationEnabled;


    public Settings() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Settings(boolean _notificationEnabled) {
        this._notificationEnabled = _notificationEnabled;
    }

    public boolean is_notificationEnabled() {
        return _notificationEnabled;
    }

    public void set_notificationEnabled(boolean _notificationEnabled) {
        this._notificationEnabled = _notificationEnabled;
    }

}