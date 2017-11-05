package team2.mkesocial.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Firebase.Event;
import team2.mkesocial.R;

import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends BaseActivity {

    private static final String TAG = CreateEventActivity.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private EditText titleField, descriptionField, dateField, startTimeField,
        endTimeField, locationField, suggestedAgeField, costField, tagsField;
    private Button createButton;
    private DatabaseReference mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //Init Objects
        titleField = (EditText) findViewById(R.id.edit_title);
        descriptionField = (EditText) findViewById(R.id.edit_description);
        dateField = (EditText) findViewById(R.id.edit_date);
        startTimeField = (EditText) findViewById(R.id.edit_start_time);
        endTimeField = (EditText) findViewById(R.id.edit_end_time);
        locationField = (EditText) findViewById(R.id.edit_location);
        suggestedAgeField = (EditText) findViewById(R.id.edit_suggested_age);
        costField = (EditText) findViewById(R.id.edit_cost);
        tagsField = (EditText) findViewById(R.id.edit_tags);
        createButton = (Button) findViewById(R.id.button_create);

        // get reference to 'users' node
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();


        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEvent();
            }
        });

    }
    private void submitEvent()
    {
        //collect filled out info
        final String title = titleField.getText().toString();
        final String description = descriptionField.getText().toString();
        final String date = dateField.getText().toString();
        final String startTime = startTimeField.getText().toString();
        final String endTime = endTimeField.getText().toString();
        final String location = locationField.getText().toString();
        final String suggestedAge =  suggestedAgeField.getText().toString();
        final String cost = costField.getText().toString();
        final String tags = tagsField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            titleField.setError(REQUIRED);
            return;
        }
        // Description is required
        if (TextUtils.isEmpty(description)) {
            descriptionField.setError(REQUIRED);
            return;
        }
        // Date is required
        if (TextUtils.isEmpty(date)) {
            dateField.setError(REQUIRED);
            return;
        }
        // Start Time is required
        if (TextUtils.isEmpty(startTime)) {
            startTimeField.setError(REQUIRED);
            return;
        }
        // End Time is required
        if (TextUtils.isEmpty(endTime)) {
            endTimeField.setError(REQUIRED);
            return;
        }
        // Location is required
        if (TextUtils.isEmpty(location)) {
            locationField.setError(REQUIRED);
            return;
        }
        // Tag(s) are required
        if (TextUtils.isEmpty(tags)) {
            tagsField.setError(REQUIRED);
            return;
        }

        // Disable button
        setEditingEnabled(false);
        //Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        //store Event under USER
        //TODO CHANGE
        final String userId = getUid();
        mFirebaseDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                       // User user = dataSnapshot.getValue(User.class);

                        /**if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
//                            Toast.makeText(NewPostActivity.this,
//                                    "Error: could not fetch user.",
//                                    Toast.LENGTH_SHORT).show();
                        } else {*/
                            // Write new post
                            // Create new post at /user-posts/$userid/$postid and at
                            // /posts/$postid simultaneously
                            String key = mFirebaseDatabase.child("events").push().getKey();
                            Event event = new Event(title, description, date, startTime, endTime,
                                    location, userId, suggestedAge, "", cost, tags);
                            Map<String, Object> eventValues = event.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/events/" + key, eventValues);
                            childUpdates.put("/user-events/" + userId + "/" + key, eventValues);

                            mFirebaseDatabase.updateChildren(childUpdates);
                        //}

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
//        mTitleField.setEnabled(enabled);
//        mBodyField.setEnabled(enabled);
        if (enabled) {
            createButton.setVisibility(View.VISIBLE);
        } else {
            createButton.setVisibility(View.GONE);
        }
    }

}
