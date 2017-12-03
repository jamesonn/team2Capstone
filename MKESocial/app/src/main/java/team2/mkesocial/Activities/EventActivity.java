package team2.mkesocial.Activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Firebase.Event;
import team2.mkesocial.R;

public class EventActivity extends Activity implements ValueEventListener {

    private FirebaseDatabase _database;
    private Query _dataQuery;
    private String _eventId;
    private TextView title, description, date, startTime, endTime, location, hostUid, suggestedAge, rating, cost;
    private String[] _keys = { "title=", "description=", "date=", "startTime=", "endTime=", "location=", "hostUid=", "suggestedAge=", "rating=", "cost="};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        title = (TextView)findViewById(R.id.event_title);
        description = (TextView)findViewById(R.id.event_description);
        date = (TextView)findViewById(R.id.event_date);
        startTime = (TextView)findViewById(R.id.event_start_time);
        endTime = (TextView)findViewById(R.id.event_end_time);
        location = (TextView)findViewById(R.id.event_location);
        suggestedAge = (TextView)findViewById(R.id.event_suggested_age);
        rating = (TextView)findViewById(R.id.event_rating);
        cost = (TextView)findViewById(R.id.event_cost);

        _database = FirebaseDatabase.getInstance();

        _eventId = getIntent().getStringExtra("EVENT_ID");

        _dataQuery = _database.getReference(Event.DB_EVENTS_NODE_NAME).child(_eventId).orderByKey();
        _dataQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                populateEventData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("QUERY RESULTS", _dataQuery.toString());
    }

    private void populateEventData(DataSnapshot data){
        Event event = Event.fromSnapshot(data);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

        title.setText(event.getTitle());
        description.setText(event.getDescription());
        date.setText(dateFormatter.format(event.getDate().getTime()));
        startTime.setText(timeFormatter.format(event.getStartTime().getTime()));
        endTime.setText(timeFormatter.format(event.getEndTime().getTime()));
        location.setText(event.getFullAddress());

        int ageData = event.getSuggestedAge();
        if (ageData != -1)
            suggestedAge.setText(Integer.toString(ageData));

        int ratingData = event.getRating();
        if (ratingData != -1)
            rating.setText(Integer.toString(ratingData));

        double costData = event.getCost();
        if (costData != -1.0f)
            cost.setText(String.format("%.2f", costData));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }
}
