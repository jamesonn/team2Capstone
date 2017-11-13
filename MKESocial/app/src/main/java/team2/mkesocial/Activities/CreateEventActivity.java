package team2.mkesocial.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TimePicker;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Firebase.Event;
import Firebase.Tag;
import Firebase.User;
import team2.mkesocial.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Calendar;
import java.util.Map;

/**
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
*/

public class CreateEventActivity extends BaseActivity {

    private static final String TAG = CreateEventActivity.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private EditText titleField, descriptionField, startDateField, endDateField, startTimeField,
        endTimeField, locationField, suggestedAgeField, costField;
    private Button createButton;
    private FirebaseDatabase mFirebaseDatabase;
    private Calendar myCalendar, myTime;
    private MultiAutoCompleteTextView tagsField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Init Objects
        titleField = (EditText) findViewById(R.id.edit_title);
        descriptionField = (EditText) findViewById(R.id.edit_description);
        startDateField = (EditText) findViewById(R.id.edit_start_date);
        endDateField = (EditText) findViewById(R.id.edit_end_date);
        startTimeField = (EditText) findViewById(R.id.edit_start_time);
        endTimeField = (EditText) findViewById(R.id.edit_end_time);
        locationField = (EditText) findViewById(R.id.edit_location);
        suggestedAgeField = (EditText) findViewById(R.id.edit_suggested_age);
        costField = (EditText) findViewById(R.id.edit_cost);
        tagsField = (MultiAutoCompleteTextView) findViewById(R.id.edit_tags);
        createButton = (Button) findViewById(R.id.button_create);

        // get reference to 'users' node
        mFirebaseDatabase = FirebaseDatabase.getInstance();


        // tags
        ArrayAdapter adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1, Tag.get_suggestTags());

        tagsField.setAdapter(adapter);
        tagsField.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        //  Date picker event
        myCalendar = Calendar.getInstance();
        myTime = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            }
        };


        //Time calendar format
        startTimeField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
                        Date date = new Date();
                        try {
                            date = parseFormat.parse(selectedHour + ":" + selectedMinute);
                        }catch (ParseException e)
                        {}
                        startTimeField.setText(displayFormat.format(date));
                    }
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        // End Time Listener
        //Time calendar format
        endTimeField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
                        Date date = new Date();
                        try {
                            date = parseFormat.parse(selectedHour + ":" + selectedMinute);
                        }catch (ParseException e)
                        {}
                        endTimeField.setText(displayFormat.format(date));
                    }
                }, hour, minute, false);//No 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        //start date listener
        startDateField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(CreateEventActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                String  myFormat = "MM/dd/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                startDateField.setText(sdf.format(myCalendar.getTime()));
            }
        });
        //end date listener
        endDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(CreateEventActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                String  myFormat = "MM/dd/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                endDateField.setText(sdf.format(myCalendar.getTime()));
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEvent();
                startActivity(new Intent(CreateEventActivity.this, FeedActivity.class));
            }
        });

/**        //edit location auto complete
        int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

        try {
            Intent intent =
                    new PlaceAutoComplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }*/
    }


    private void submitEvent()
    {
        //collect filled out info
        final String title = titleField.getText().toString();
        final String description = descriptionField.getText().toString();
        final String date = startDateField.getText().toString();
        final String startTime = startTimeField.getText().toString();
        final String endTime = endTimeField.getText().toString();
        final String location = locationField.getText().toString();
        final String suggestedAge =  suggestedAgeField.getText().toString();
        final String cost = costField.getText().toString();
        final String tags = tagsField.getText().toString();

    /**    // Title is required
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
        }**/

        // Disable button
        setEditingEnabled(false);

        // 1) Push event and get a unique Event ID
        // Push an empty node with a unique key under 'events' node in JSON
        final String eventId = mFirebaseDatabase.getReference(Event.DB_EVENT_LOCATIONS_NODE_NAME)
                .child("events").push().getKey();

        //2) Store event info in DB under it's Event ID
        // Get user ID to tie event to
        final String userId = getUid();
        Event newEvent = new Event(title, description, date, startTime, endTime, location,
                userId, suggestedAge, "", cost, tags);
        // Add event obj to database under its event ID
        mFirebaseDatabase.getReference(User.DB_EVENTS_NODE_NAME).child(eventId).setValue(newEvent.toMap());


        //putting new node in "user-events-hosting" with unique ID
        String hostEventId = mFirebaseDatabase.getReference(User.DB_USER_EVENTS_HOSTING_NODE_NAME).child(userId).push().getKey();
        mFirebaseDatabase.getReference(User.DB_USER_EVENTS_HOSTING_NODE_NAME).child(userId).child(hostEventId).setValue(eventId);
    }



    private void setEditingEnabled(boolean enabled) {
        if (enabled) {
            createButton.setVisibility(View.VISIBLE);
        } else {
            createButton.setVisibility(View.GONE);
        }
    }


}
