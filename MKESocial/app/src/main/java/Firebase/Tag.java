package Firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Tag {

    private static String[] _suggestTags = {"outdoors", "family-friendly", "kid-friendly","alcohol",
        "entertainment","music", "18+", "sports", "indoor", "private", "public", "large", "small",
        "lake", "downtown", "tour", "all-day", "free", "cheap", "$$$", "$$", "$",
        "theater", "water"}; //TODO add more suggested tag values
    private String _name;


    public Tag() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Tag(String _name) {
        this._name = _name;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

}