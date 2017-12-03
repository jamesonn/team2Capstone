package team2.mkesocial.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.ValueEventListener;


import Firebase.Event;
import Firebase.Tag;
import Firebase.User;
import team2.mkesocial.R;
import Validation.TextValidator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Calendar;
import java.util.Map;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;

/**
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
*/

public class CreateEventActivity extends BaseActivity {

    private static final String TAG = CreateEventActivity.class.getSimpleName();
    private static final String REQUIRED = "Required";

    private EditText titleField, descriptionField, startDateField, endDateField, startTimeField,
        endTimeField, suggestedAgeField, costField;
    private Button cancelButton, createButton;
    private FirebaseDatabase mFirebaseDatabase;
    private Calendar startCalendar, endCalendar, myTime;
    private MultiAutoCompleteTextView tagsField;
    private ArrayList<EditText> objectList = new ArrayList<EditText>();

    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);


    private PlaceAutocompleteFragment autocompleteFragment;
    private Place placePicked;

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
        suggestedAgeField = (EditText) findViewById(R.id.edit_suggested_age);
        costField = (EditText) findViewById(R.id.edit_cost);
        tagsField = (MultiAutoCompleteTextView) findViewById(R.id.edit_tags);
        cancelButton = (Button) findViewById(R.id.button_cancel);
        createButton = (Button) findViewById(R.id.button_create);

        //put all the edit text references in an array for easy verification later
        objectList.add(titleField); objectList.add(descriptionField); objectList.add(startDateField);
        objectList.add(endDateField);objectList.add(startTimeField);objectList.add(endTimeField);
        objectList.add(suggestedAgeField);objectList.add(costField);objectList.add(tagsField);


        // tags
        ArrayAdapter adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1, Tag.get_suggestTags());

        tagsField.setAdapter(adapter);
        tagsField.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        //  Date picker event
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        // Standard Time Display
        myTime = Calendar.getInstance();
        String  myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);


        final DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, monthOfYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            }
        };
        final DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, monthOfYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            }
        };


        //Time calendar format
        Calendar startTime = Calendar.getInstance();
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
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        int hour = hourOfDay % 12;
                        startTimeField.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                minute, hourOfDay < 12 ? "am" : "pm"));
                    }}, hour, minute, false);//No 24 hour time*/
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        // End Time Listener
        endTimeField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = startTime;
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker = new TimePickerDialog(CreateEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar datetime = Calendar.getInstance();
                        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        datetime.set(Calendar.MINUTE, minute);
                        //Make sure endtime is after start time
                        if (datetime.getTimeInMillis() >= startTime.getTimeInMillis()) {
                            //it's after current
                            int hour = hourOfDay % 12;
                            endTimeField.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                    minute, hourOfDay < 12 ? "am" : "pm"));
                        } else {
                            //it's before current'
                            Toast.makeText(getApplicationContext(), "Invalid Time", Toast.LENGTH_LONG).show();
                        }
                    }}, hour, minute, false);//No 24 hour time*/
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        //start date listener
        startDateField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(CreateEventActivity.this, startDate, startCalendar
                        .get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH)){
                    @Override
                    public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        startCalendar.set(year, month, dayOfMonth);
                        startDateField.setText(sdf.format(startCalendar.getTime()));
                        if(startCalendar.getTimeInMillis() >= endCalendar.getTimeInMillis()) {
                            endCalendar.setTimeInMillis(startCalendar.getTimeInMillis());
                            endDateField.setText(sdf.format(endCalendar.getTime()));
                        }
                    }
                };
                // Limit Date picker to current dates
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                mDatePicker.show();

            }
        });
        //end date listener
        endDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(CreateEventActivity.this, endDate, endCalendar
                        .get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH) - 1)
                {
                    @Override
                    public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        endCalendar.set(year, month, dayOfMonth);
                        endDateField.setText(sdf.format(endCalendar.getTime()));
                        Log.d("Date", "Year=" + year + " Month=" + (month + 1) + " day=" + dayOfMonth);
                    }
                };
                // Limit Date picker to current dates
                mDatePicker.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
                mDatePicker.show();
            }
        });

        // Suggested Age Listener input validation
        suggestedAgeField.addTextChangedListener(new TextValidator(suggestedAgeField) {
            @Override
            public void validate(TextView textView, String text) {
                // check if text has an non numeric characters
                if(text.matches("^[0-9]+$"))
                {
                    // check range
                    try{
                        int inputtedAge = Integer.parseInt(text);
                        if(inputtedAge > 120 || inputtedAge < 0) {
                            suggestedAgeField.setError("Please input a valid age");
                        }

                    }catch(NumberFormatException e){
                        suggestedAgeField.setError("Only numeric characters are allowed (0-9)");
                    }
                }
            }
        });

        // Cost field verification
        costField.addTextChangedListener(new TextValidator(costField) {
            @Override
            public void validate(TextView textView, String text) {
                // check price input is valid
                if(text.matches("[0-9]+([,.][0-9]{1,2})?"))
                {
                    // check price range
                    try{
                        double inputtedAge = Double.parseDouble(text);
                        if(inputtedAge > 500 || inputtedAge < 0) {
                            costField.setError("Please input a valid price");
                        }

                    }catch(NumberFormatException e){
                        costField.setError("Expected format: xx.xx");
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //simulate back pressed
                //startActivity(new Intent(CreateEventActivity.this, FeedActivity.class));
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(submitEvent())
                    startActivity(new Intent(CreateEventActivity.this, FeedActivity.class));
            }
        });

       //edit location fragment auto complete
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placePicked = place;
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }


    private boolean submitEvent() {
        //collect filled out info
        final String title = titleField.getText().toString();
        final String description = descriptionField.getText().toString();
        final String date = startDateField.getText().toString();
        final String startTime = startTimeField.getText().toString();
        final String endTime = endTimeField.getText().toString();
        String location = "";
        final String suggestedAge = suggestedAgeField.getText().toString();
        final String cost = costField.getText().toString();
        final String tags = tagsField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            titleField.setError(REQUIRED);
            return false;
        }
        // Description is required
        if (TextUtils.isEmpty(description)) {
            descriptionField.setError(REQUIRED);
            return false;
        }
        // Start Date is required
        if (TextUtils.isEmpty(date)) {
            startDateField.setError(REQUIRED);
            return false;
        }
        // End Date is required
        if (TextUtils.isEmpty(date)) {
            endDateField.setError(REQUIRED);
            return false;
        }
        // Start Time is required
        if (TextUtils.isEmpty(startTime)) {
            startTimeField.setError(REQUIRED);
            return false;
        }
        // End Time is required
        if (TextUtils.isEmpty(endTime)) {
            endTimeField.setError(REQUIRED);
            return false;
        }//TODO on location deleted check
        if (placePicked == null || placePicked.getAddress().toString().isEmpty()) {
            autocompleteFragment.setHint("Please add a location before creating event");
            return false;

        } else
            location = placePicked.getAddress().toString()+" "+placePicked.getLatLng();
        // Tag(s) are required
        if (TextUtils.isEmpty(tags)) {
            tagsField.setError(REQUIRED);
            return false;
        }

        // Check if any fields are in error
        for (EditText x : objectList) {
            if (x.getError() != null) {
                createButton.setError("One or more fields are invalid");
                return false;
            }
        }
        // Disable button
        setEditingEnabled(false);

        // 1) Push event and get a unique Event ID
        // Push an empty node with a unique key under 'events' node in JSON
        final String eventId = eventDatabase.child("events").push().getKey();

        //2) Store event info in DB under it's Event ID
        // Get user ID to tie event to
        final String userId = getUid();
        Event newEvent = new Event(title, description, date, startTime, endTime, location,
                userId, suggestedAge, "", cost, tags);
        // Add event obj to database under its event ID
        eventDatabase.child(eventId).setValue(newEvent.toMap());

        userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                //you can only Host events when you create an event
               if(user.getHostEid() == null)
               {
                   userDatabase.child(userId).child("hostEid").setValue(eventId+":"+title+":");
               }
               else {
                   userDatabase.child(userId).child("hostEid").setValue(user.getHostEid() + eventId+":"+title+":");
               }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        return true;
    }

    private void setEditingEnabled(boolean enabled) {
        if (enabled) {
            createButton.setVisibility(View.VISIBLE);
        } else {
            createButton.setVisibility(View.GONE);
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setIcon(R.mipmap.ic_warning)
            .setTitle("Back Confirmation")
            .setMessage("Are you sure you want to go back without saving?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }


}
