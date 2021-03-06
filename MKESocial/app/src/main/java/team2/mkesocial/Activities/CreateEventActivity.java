package team2.mkesocial.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Firebase.Event;
import Firebase.MethodOrphanage;
import Firebase.Settings;
import Firebase.Tag;
import Firebase.User;
import Validation.TextValidator;
import Validation.WordScrubber;
import team2.mkesocial.Adapters.EventAdapter;
import team2.mkesocial.Adapters.SimpleEventAdapter;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;

public class CreateEventActivity extends BaseActivity {

    private static final String TAG = CreateEventActivity.class.getSimpleName();
    private static final String REQUIRED = "Required";
    private static final int IMAGE_PICKER_SELECT = 234;

    private EditText titleField, descriptionField, startDateField, endDateField, startTimeField,
        endTimeField, suggestedAgeField, costField;
    private Button cancelButton, createButton;
    private Calendar startCalendar, endCalendar, myTime;
    private MultiAutoCompleteTextView tagsField;
    private ImageView eventImage;
    private ImageButton editButton;
    private ArrayList<EditText> objectList = new ArrayList<EditText>();
    private WordScrubber wordScrubber;

    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    // Firestore Image DB Ref
    private StorageReference storageReference  = FirebaseStorage.getInstance().getReference();

    private PlaceAutocompleteFragment autocompleteFragment;
    private Place placePicked;

    private Uri filePath;

    private Event thisEvent = new Event();

    private ListView _eventList;
    private EventAdapter _eventAdapter;
    private ArrayList<Event> _events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);
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
        editButton = (ImageButton) findViewById(R.id.imageButton_insert_image);
        eventImage = (ImageView)  findViewById(R.id.imageView_event);
        _eventList = (ListView)findViewById(R.id.list_view);

        wordScrubber = new WordScrubber(getApplicationContext());
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

        //no ` allowed in title
        titleField.addTextChangedListener(new TextWatcher() {
            WordScrubber wordScrubber = new WordScrubber(getApplicationContext());
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (s.charAt(s.length() - 1) == '`') {
                        titleField.setText(s.subSequence(0, s.length() - 1));
                        titleField.setSelection(s.length() - 1);
                    }
                }
                if(!thisEvent.setTitle(s.toString()))
                    titleField.setError("Please input a valid Title");

                if(!s.toString().isEmpty()){
                    titleField.removeTextChangedListener(this);//"pause" checker

                    String cleanString = s.toString();
                    //check as user types if word becomes a swear word!
                    if(s.length() > 1 ){
                        String[] words = cleanString.split("[\\p{Punct}\\s]+");
                        for(int i=0; i<words.length; ++i){
                            if(wordScrubber.isBadWord(words[i])){
                                cleanString=wordScrubber.filterHiddenBadWords(cleanString);
                                titleField.setText(cleanString);
                                titleField.setSelection(cleanString.length());
                            }
                        }
                    }
                    if(cleanString.isEmpty()){  titleField.setError("Please input a valid bio");}

                    titleField.addTextChangedListener(this);//"resume" checker
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //Offensive word check
        descriptionField.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable arg0) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    descriptionField.removeTextChangedListener(this);//"pause" checker

                    String cleanString = s.toString();
                    //check as user types if word becomes a swear word!
                    if(s.length() > 1 ){
                        String[] words = cleanString.split("[\\p{Punct}\\s]+");
                        for(int i=0; i<words.length; ++i){
                            if(wordScrubber.isBadWord(words[i])){
                                cleanString=wordScrubber.filterHiddenBadWords(cleanString);
                                descriptionField.setText(cleanString);
                                descriptionField.setSelection(cleanString.length());
                            }
                        }
                    }
                    if(!thisEvent.setDescription(s.toString()))
                        descriptionField.setError("Please input a valid Title");
                    if(cleanString.isEmpty()){  descriptionField.setError("Please input a valid bio");}

                    descriptionField.addTextChangedListener(this);//"resume" checker
                }
            }
        });

        //Time calendar format
        Calendar startTime = Calendar.getInstance();
        startTimeField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar startTime = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        int hour = hourOfDay % 12;
                        String sTime = String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                minute, hourOfDay < 12 ? "am" : "pm");
                        if(!thisEvent.setStartTime(Event.parseTime(sTime)))
                            startTimeField.setError("Make sure Start Time is before end time");
                        else {
                            startTimeField.setText(sTime);
                            startTimeField.setError(null);
                        }
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
                        Calendar endT = Calendar.getInstance();
                        endT.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endT.set(Calendar.MINUTE, minute);
                        int hour = hourOfDay % 12;
                        String eTime = String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                minute, hourOfDay < 12 ? "am" : "pm");

                        //Make sure endtime is after start time
                        if (!thisEvent.setEndTime(Event.parseTime(eTime)))
                            endTimeField.setError("Make sure end time is after start");
                        else{
                            endTimeField.setText(eTime);
                            endTimeField.setError(null);
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

                    }
                };
                // Limit Date picker to current dates
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                mDatePicker.show();
                // save the date picked
                mDatePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String sDate = Event.dateFormat.format(startCalendar.getTime());

                        if(!thisEvent.setStartDate(Event.parseDate(sDate))) {
                            startDateField.setError("Please set start day to be before end day");
                        }
                        else {
                            startDateField.setText(sDate);
                            startDateField.setError(null);
                            // set end date to match start date if it's empty
                            if(endDateField.getText().toString().isEmpty()) {
                                endCalendar.setTimeInMillis(startCalendar.getTimeInMillis());
                                endDateField.setText(Event.dateFormat.format(endCalendar.getTime()));
                                thisEvent.setEndDate(Event.parseDate(sDate));
                            }
                        }
                    }
                });
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

                    }
                };
                // Limit Date picker to current dates
                mDatePicker.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
                mDatePicker.show();
                // save the date picked
                mDatePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String eDate = Event.dateFormat.format(endCalendar.getTime());

                        if(!thisEvent.setEndDate(Event.parseDate(eDate))) {
                            endDateField.setError("Please set end day to be after the start day");
                        }
                        else {
                            endDateField.setText(eDate);
                            endDateField.setError(null);
                        }

                    }
                });
            }
        });

        // Suggested Age Listener input validation
        suggestedAgeField.addTextChangedListener(new TextValidator(suggestedAgeField) {
            @Override
            public void validate(TextView textView, String text) {
                // check if text has an non numeric characters
                if(text.matches("^[0-9]+$"))
                {
                    try{
                        int inputtedAge = Integer.parseInt(text);
                        if(!thisEvent.setSuggestedAge(inputtedAge)) {
                            suggestedAgeField.setError("Please input a valid age");
                        }
                    }catch(NumberFormatException e){
                        suggestedAgeField.setError("Only numeric characters are allowed (0-9)");
                    }
                }
            }
        });

        // Cost field verification
        costField.addTextChangedListener(new TextWatcher(){
            DecimalFormat dec = new DecimalFormat("0.00");
            @Override
            public void afterTextChanged(Editable arg0) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!s.toString().equals(current)){
                        costField.removeTextChangedListener(this);

                        String cleanString = s.toString().replaceAll("[$,.]", "");

                        double parsed = Double.parseDouble(cleanString) / 100;
                        String formatted = NumberFormat.getCurrencyInstance().format((parsed));

                        current = formatted;
                        costField.setText(formatted);
                        costField.setSelection(formatted.length());
                        if(!thisEvent.setCost(parsed))
                            costField.setError("Please input a valid price");

                        costField.addTextChangedListener(this);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //simulate back pressed
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(submitEvent()) {
                    finish();
                }
            }
        });

        // Click Edit image to add an image
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
            }
        });

       //edit location fragment auto complete
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(!thisEvent.setLocation(place))
                    autocompleteFragment.setHint("Please select a valid location");
                placePicked = place;
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        //fetch user's current hosting events
        userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                String hosting = user.getHostEid();
                //check if they've ever hosted anything
                if(hosting != null && !hosting.isEmpty()){
                    for(String eventKey: user.parseEventHostIDs().split("`"))
                    {
                        //fetch each event from db
                        eventDatabase.child(eventKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Event event = Event.fromSnapshot(dataSnapshot);
                                _events.add(event);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        //SYS Req 3.3.1
        _eventAdapter = new EventAdapter(this, _events);
        _eventList.setAdapter(_eventAdapter);

        _eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event)_eventList.getItemAtPosition(position);
                inspectEvent(selectedEvent.getEventId());
            }
        });
        _eventAdapter.notifyDataSetChanged();

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //For correcting orientation so it displays correctly (on images that where taken sideways/upside-down)
                ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(filePath));
                //get current rotation...
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                //convert to degrees
                int rotationInDegrees = MethodOrphanage.exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                }
                Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;

                bm = MethodOrphanage.resize(bm, eventImage.getWidth(), height);
                eventImage.getLayoutParams().height = bm.getHeight();
                eventImage.requestLayout();
                eventImage.setImageBitmap(bm);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
            }
        }

    }

    private boolean submitEvent() {
        //collect filled out info
        final String title = titleField.getText().toString();
        final String description = descriptionField.getText().toString();
        final String startDate = startDateField.getText().toString();
        final String endDate = endDateField.getText().toString();
        final String startTime = startTimeField.getText().toString();
        final String endTime = endTimeField.getText().toString();

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
        if (TextUtils.isEmpty(startDate)) {
            startDateField.setError(REQUIRED);
            return false;
        }
        // End Date is required
        if (TextUtils.isEmpty(endDate)) {
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
        }//Need a valid location
        if (!thisEvent.setLocation(placePicked)) {
            autocompleteFragment.setHint("Please add a location before creating event");
            return false;
        }
        // Tag(s) are required
        if (!thisEvent.setTags(Event.parseTags(tagsField.getText().toString()))){
            tagsField.setError(REQUIRED);
            return false;
        }

        // Check if any fields are in error
        for (EditText x : objectList) {
            if (x.getError() != null) {
                createButton.setError(x.getError());
                return false;
            }
        }
        // Disable button
        setEditingEnabled(false);

//        //scrub all edit texts and tags before saving
//        thisEvent.setTitle(wordScrubber.filterHiddenBadWords(thisEvent.getTitle()));
//        thisEvent.setDescription(wordScrubber.filterHiddenBadWords(thisEvent.getDescription()));
//        List<Tag> tags = new ArrayList<Tag>();
//        for(Tag tag: Event.parseTags(tagsField.toString()))
//        {
//            tags.add(new Tag(wordScrubber.filterHiddenBadWords(tag.getName())));
//        }
//        thisEvent.setTags(tags);

        // 1) Push event and get a unique Event ID
        // Push an empty node with a unique key under 'events' node in JSON
        final String eventId = eventDatabase.child("events").push().getKey();

        if(filePath != null)//Activity a, StorageReference storageReference, Uri newFilePath, String oldFilePath, DatabaseReference placeToStoreRef) {
            MethodOrphanage.uploadFile(this, storageReference, filePath, "", eventDatabase.child(eventId).child("image"));


        //2) Store event info in DB under it's Event ID
        // Get user ID to tie event to
        final String userId = getUid();

        //set the other areas
        thisEvent.setHostUid(getUid());
        //thisEvent.setAttendes(" ");
        thisEvent.setRating(-1);
        // Add event obj to database under its event ID
        eventDatabase.child(eventId).setValue(thisEvent.toMap());

        userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                //you can only Host events when you create an event
               if(user.getHostEid().isEmpty()){userDatabase.child(userId).child("hostEid").setValue(eventId+"`"+title+"`");}
               else{userDatabase.child(userId).child("hostEid").setValue(user.getHostEid() + eventId+"`"+title+"`");}
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
