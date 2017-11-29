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
                populateEventData(dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("QUERY RESULTS", _dataQuery.toString());
    }

    private void populateEventData(String data){
        for (String key : _keys) {
            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:MM:SS");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String value = null;
            Pattern p = Pattern.compile("(?<="+key+").*?(?=, )");
            Matcher matcher = p.matcher(data.toString());
            if (matcher.find()) {
                try{
                    value = matcher.group(0);
                }catch(Exception e){

                }
                if(value != null && !value.equals("-1")){
                    if(key.contains("title")){
                        title.setText(value);
                    }else if(key.contains("description")){
                        description.setText(value);
                    }else if(key.contains("date")){
                        Date eventDate = new Date();
                        eventDate.setTime(Long.parseLong(value));
                        date.setText(dateFormatter.format(eventDate));
                    }else if(key.contains("startTime")){
                        Date date = new Date();
                        date.setTime(Long.parseLong(value));
                        startTime.setText(timeFormatter.format(date));
                    }else if(key.contains("endTime")){
                        Date date = new Date();
                        date.setTime(Long.parseLong(value));
                        endTime.setText((timeFormatter.format(date)));
                    }else if(key.contains("location")){
                        Geocoder geocoder;
                        List<Address> addresses = null;
                        geocoder = new Geocoder(this, Locale.getDefault());

                        String[] locationData = value.split(";");
                        try {
                            addresses = geocoder.getFromLocation(Double.parseDouble(locationData[1]), Double.parseDouble(locationData[2]), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        }catch(Exception e){
                            Log.d("failed","failed");
                        }
                        if(addresses != null) {
                            Log.d("address",addresses.toString());
                         //   String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                           // String city = addresses.get(0).getLocality();
                           // String state = addresses.get(0).getAdminArea();
                           // String country = addresses.get(0).getCountryName();
                            //String postalCode = addresses.get(0).getPostalCode();
                            //String knownName = addresses.get(0).getFeatureName();
                        }
                        //TODO have real locations stored
                        location.setText("Address: "+locationData[0]);
                    }else if(key.contains("hostUid")){
                        //hostUid.setText(value);
                    }else if(key.contains("suggestedAge")){
                        suggestedAge.setText(value);
                    }else if(key.contains("rating")){
                        rating.setText(value);
                    }else if(key.contains("cost")){
                        cost.setText(value);
                    }
                }
            }
        }
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
