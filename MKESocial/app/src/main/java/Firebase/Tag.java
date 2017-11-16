package Firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by cfoxj2 on 10/23/2017.
 */
@IgnoreExtraProperties
public class Tag {

    private static String[] _suggestTags = {"outdoors", "family-friendly", "kid-friendly","alcohol",
            "entertainment","music", "18+", "sports", "indoor", "private", "public", "large", "small",
            "lake", "downtown", "tour", "all-day", "free", "cheap", "$$$", "$$", "$", "adult",
            "theater", "water", "winter", "fall", "spring", "summer"}; //TODO add more suggested tag values
    private String name;


    public Tag() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String[] get_suggestTags()
    {
        return _suggestTags;
    }
}