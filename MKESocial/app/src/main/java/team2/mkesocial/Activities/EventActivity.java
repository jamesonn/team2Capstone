package team2.mkesocial.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.ErrorWrappingGlideException;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.sql.DataSource;

import Firebase.Event;
import Firebase.Settings;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;

public class EventActivity extends Activity implements ValueEventListener {

    private FirebaseDatabase _database;
    private Query _dataQuery;
    private String _eventId;
    private EditText title, description, startDate, endDate, startTime, endTime, location, hostUid, suggestedAge, rating, cost;
    private Button editButton, deleteButton;
    private ImageButton insertImage;
    private ImageView eventImage;
    private ArrayList<EditText> objectList = new ArrayList<EditText>();
    private boolean editing = false;
    private Event fetchedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        title = (EditText)findViewById(R.id.event_title);
        description = (EditText)findViewById(R.id.event_description);
        startDate = (EditText)findViewById(R.id.event_start_date);
        endDate = (EditText)findViewById(R.id.event_end_date);
        startTime = (EditText)findViewById(R.id.event_start_time);
        endTime = (EditText)findViewById(R.id.event_end_time);
        location = (EditText)findViewById(R.id.event_location);
        suggestedAge = (EditText)findViewById(R.id.event_suggested_age);
        rating = (EditText)findViewById(R.id.event_rating);
        cost = (EditText)findViewById(R.id.event_cost);
        editButton = (Button) findViewById(R.id.button_edit);
        deleteButton = (Button) findViewById(R.id.button_delete);
        insertImage = (ImageButton) findViewById(R.id.imageButton_insert_image);
        eventImage = (ImageView) findViewById(R.id.imageView_event);

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

        //put all the edit text references in an array for easy access
        objectList.add(title); objectList.add(description); objectList.add(startDate);
        objectList.add(endDate); objectList.add(startTime);objectList.add(endTime);objectList.add(location);
        objectList.add(suggestedAge);objectList.add(rating);objectList.add(cost);

        //are we editing?
        editing = getIntent().getExtras().getBoolean("editing");

        for(EditText e: objectList) {
            if (editing)
                e.setEnabled(true);
            else {
                e.setEnabled(false);
            }
        }
    }

    private void populateEventData(DataSnapshot data){
        fetchedEvent = Event.fromSnapshot(data);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

        title.setText(fetchedEvent.getTitle());
        description.setText(fetchedEvent.getDescription());
        startDate.setText(dateFormatter.format(fetchedEvent.getStartDate().getTime()));
        endDate.setText(dateFormatter.format(fetchedEvent.getEndDate().getTime()));
        startTime.setText(timeFormatter.format(fetchedEvent.getStartTime().getTime()));
        endTime.setText(timeFormatter.format(fetchedEvent.getEndTime().getTime()));
        location.setText(fetchedEvent.getFullAddress());

        //populate image
        if(fetchedEvent.getImage() != null && !fetchedEvent.getImage().isEmpty()) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);

            Glide.with(getApplicationContext())
                    .load(fetchedEvent.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(eventImage);
        }
        else {//hide image view
            android.view.ViewGroup.LayoutParams layoutParams = eventImage.getLayoutParams();
            layoutParams.width = eventImage.getWidth();
            layoutParams.height = 0;
            eventImage.setLayoutParams(layoutParams);
        }
        int ageData = fetchedEvent.getSuggestedAge();
        if (ageData != -1)
            suggestedAge.setText(Integer.toString(ageData));

        int ratingData = fetchedEvent.getRating();
        if (ratingData != -1)
            rating.setText(Integer.toString(ratingData));

        double costData = fetchedEvent.getCost();
        if (costData != -1.0f)
            cost.setText(String.format("%.2f", costData));

        // If user is the event host, give them an option to edit their event
        if(fetchedEvent.getHostUid().equals(BaseActivity.getUid()))
            editingEvent();
        else {//hide buttons
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            insertImage.setVisibility(View.GONE);
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

    /**
     * Edit event functionality
     */
    public void editEvent()
    {
        finish();
        startActivity(getIntent().putExtra("editing", true));

    }
    /**
     * Save event functionality
     */
    public void saveEvent()
    {
        while(fetchedEvent == null)
            Toast.makeText(getApplicationContext(), "Waiting for DB to retrieve event info", Toast.LENGTH_LONG).show();
        // Save changed fields to DB
        // We'll just trust they don't need any input validation at this time ;)
        // TODO input validation
        Event newEvent = new Event(title.getText().toString(), description.getText().toString(),
                startDate.getText().toString(), endDate.getText().toString(), startTime.getText().toString(),
                endTime.getText().toString(), fetchedEvent.getLocation(),
                BaseActivity.getUid(), suggestedAge.getText().toString(), "", cost.getText().toString(),
                fetchedEvent.getTags(), fetchedEvent.getImage());
        // Add event obj to database under its event ID
        FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME).child(_eventId).updateChildren(newEvent.toMap());
        Intent goBackToThisEventPage = new Intent(this, EventActivity.class);
        goBackToThisEventPage.putExtra("EVENT_ID", _eventId);
        finish();
        startActivity(goBackToThisEventPage);

    }

    private void editingEvent()
    {

        // Change the edit button to say save
        if(editing){
            editButton.setText("Save");
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editing)
                    editEvent();
                else
                    saveEvent();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME).child(_eventId).removeValue();
                // Done with activity
                finish();
            }
        });
    }

}
