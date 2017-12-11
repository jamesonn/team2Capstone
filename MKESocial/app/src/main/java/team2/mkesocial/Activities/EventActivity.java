package team2.mkesocial.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import Firebase.BusyTime;
import Firebase.Event;
import Firebase.MethodOrphanage;
import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;
import static Firebase.Databasable.DB_USER_SETTINGS_NODE_NAME;

public class EventActivity extends BaseActivity implements ValueEventListener {

    private static final String TAG = EventActivity.class.getSimpleName();
    private FirebaseDatabase _database;
    private Query _dataQuery;
    private String _eventId, attenders;
    private EditText title, description, startDate, endDate, startTime, endTime, location, hostUid, suggestedAge, rating, cost;
    private Button editButton, deleteButton;
    private ImageButton insertImage;
    private ImageView eventImage;
    private ProgressBar progressBar;
    private ArrayList<EditText> objectList = new ArrayList<EditText>();
    private boolean editing = false;
    private Event fetchedEvent;
    private ImageButton att, maybe, attendees;
    private Uri filePath;
    private List<BusyTime> busyTimes;
    private ArrayList<Event> interestedEvents = new ArrayList<>();

    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    private DatabaseReference userSettingsDatabase = FirebaseDatabase.getInstance().getReference(DB_USER_SETTINGS_NODE_NAME);

    private static final int IMAGE_PICKER_SELECT = 234;
    private StorageReference storageReference  = FirebaseStorage.getInstance().getReference();

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
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);


        attendees = (ImageButton) findViewById(R.id.attenders_btn);
        attendees.bringToFront();
        att = (ImageButton) findViewById(R.id.attending_btn);
        att.bringToFront();
        maybe = (ImageButton) findViewById(R.id.maybe_btn);
        maybe.bringToFront();

        iconsToDisplay();
        getConflictData();

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
        objectList.add(endDate); objectList.add(startTime);objectList.add(endTime);//objectList.add(location);
        objectList.add(suggestedAge);objectList.add(rating);objectList.add(cost);

        //are we editing?
        editing = getIntent().getExtras().getBoolean("editing");

        location.setEnabled(false);
        for(EditText e: objectList) {
            if (editing)
                e.setEnabled(true);
            else {
                e.setEnabled(false);
            }
        }


    }

    private void iconsToDisplay(){
        userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                //User gets to know events he/she is attending
                if(user != null) {
                    if (user.getAttendEid().isEmpty() || !user.getAttendEid().contains(_eventId + "`" + title.getText() + "`")) {
                        att.setImageResource(R.mipmap.ic_not_attending_pic);
                    } else {
                        att.setImageResource(R.mipmap.ic_attending_pic);
                        att.setContentDescription("true");
                    }
                    if (user.getMaybeEid().isEmpty() || !user.getMaybeEid().contains(_eventId + "`" + title.getText() + "`")) {
                        maybe.setImageResource(R.mipmap.ic_not_maybe_pic);
                    } else {
                        maybe.setImageResource(R.mipmap.ic_maybe_pic);
                        maybe.setContentDescription("true");
                    }
                }
             }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void populateEventData(DataSnapshot data){
        fetchedEvent = Event.fromSnapshot(data);

        title.setText(fetchedEvent.getTitle());
        description.setText(fetchedEvent.getDescription());
        startDate.setText(fetchedEvent.getFormattedStartDate());
        endDate.setText(fetchedEvent.getFormattedEndDate());
        startTime.setText(fetchedEvent.getFormattedStartTime());
        endTime.setText(fetchedEvent.getFormattedEndTime());
        location.setText(MethodOrphanage.getFullAddress(fetchedEvent.getLocation()));

        //populate image
        if(fetchedEvent.getImage() != null && !fetchedEvent.getImage().isEmpty()) {

            Glide.with(getApplicationContext())
                    .load(fetchedEvent.getImage())
                    //.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(eventImage);
            //hide image button
            insertImage.setVisibility(View.GONE);

        }
        else if(!editing){//hide image view if not editing
            android.view.ViewGroup.LayoutParams layoutParams = eventImage.getLayoutParams();
            layoutParams.width = eventImage.getWidth();
            layoutParams.height = 0;
            eventImage.setLayoutParams(layoutParams);
            insertImage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }else //hide progress bar - editing
        {
           progressBar.setVisibility(View.GONE);
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
        Event event = new Event();
        //Input validation -> go through one by one each field and set into event obj

        String badField = validateFields(event);

        if(!badField.isEmpty()) {
            editButton.setError("Invalid " + badField);
            return;
        }

        Event newEvent = new Event(title.getText().toString(), description.getText().toString(),
                startDate.getText().toString(), endDate.getText().toString(), startTime.getText().toString(),
                endTime.getText().toString(), fetchedEvent.getLocation(),
                BaseActivity.getUid(), fetchedEvent.getAttendees(), suggestedAge.getText().toString(), "", cost.getText().toString(),
                fetchedEvent.getTags());

        if(filePath == null)
            try {
                filePath = Uri.parse(fetchedEvent.getImage());

            }catch(Exception e){}
        MethodOrphanage.uploadFile(this, storageReference, filePath, fetchedEvent.getImage()
                , eventDatabase.child(_eventId).child("image"));

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
        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
                startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), IMAGE_PICKER_SELECT);
            }
        });
    }

    public void attenders_btn_on_click(View v){
        if (v.getId() == R.id.attenders_btn) {
            // get a reference to the already created main layout
            RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.event_layout);

            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.pop_up, null);
            TextView text = (TextView) popupView.findViewById(R.id.pop);
            LinearLayout forAtt = (LinearLayout) popupView.findViewById(R.id.for_attendees);

            String title = "List of Attendees";
            text.setText(title);

            //get attendees
            eventDatabase.child(_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = Event.fromSnapshot(dataSnapshot);

                    attenders = event.getAttendees();//id`id`id`
                    if (attenders != null) {
                        String[] sep = attenders.split("`");
                        if (sep.length >= 1) {
                            for (int i = 0; i < sep.length; ++i) {
                                TextView userN = new TextView(getApplicationContext());
                                userN.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                                userDatabase.child(sep[i]).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user2 = User.fromSnapshot(dataSnapshot);
                                        if(user2 != null)
                                            userN.setText(user2.getName());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                userN.setTextSize(20);
                                userN.setTextColor(0xff424242);
                                userN.setAllCaps(false);
                                userN.setClickable(true);
                                userN.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                userN.setVisibility(View.VISIBLE);
                                userN.setId(i);
                                userN.setContentDescription(sep[i]);
                                forAtt.setBackgroundColor(getResources().getColor(R.color.mke_light_blue,getTheme()));
                                forAtt.addView(userN);

                                userN.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String key = userN.getContentDescription().toString();

                                        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                if (snapshot.hasChild(key)) {
                                                    userSettingsDatabase.child(key).child("privateProfile").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(!Boolean.parseBoolean((String)dataSnapshot.getValue()) || getUid().equals(key))
                                                            {
                                                                inspectUser(key);
                                                            }
                                                            else{
                                                                RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.event_layout);

                                                                // inflate the layout of the popup window
                                                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                                                View popupView = inflater.inflate(R.layout.pop_up, null);
                                                                TextView text = (TextView)popupView.findViewById(R.id.pop);
                                                                String msg = "Cannot View Private Profile!";
                                                                text.setText(msg);

                                                                // create the popup window
                                                                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                                                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                                                boolean focusable = true; // lets taps outside the popup also dismiss it
                                                                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                                                                // show the popup window
                                                                popupWindow.showAtLocation(mapLayout, Gravity.TOP, 0, 0);
                                                                // dismiss the popup window when touched
                                                                popupView.setOnTouchListener(new View.OnTouchListener() {
                                                                    @Override
                                                                    public boolean onTouch(View v, MotionEvent event) {
                                                                        popupWindow.dismiss();
                                                                        return true;
                                                                    }
                                                                });
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                                /*else {
                                                    eventDatabase.child(_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Event event = Event.fromSnapshot(dataSnapshot);
                                                            //An event knows the people for sure attending it
                                                            eventDatabase.child(_eventId).child("attendees").setValue(event.getAttendees().replace(key + "`", ""));
                                                            RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_p);

                                                            // inflate the layout of the popup window
                                                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                                            View popupView = inflater.inflate(R.layout.pop_up, null);
                                                            TextView text = (TextView)popupView.findViewById(R.id.pop);
                                                            String msg = "The User no longer exists!";
                                                            text.setText(msg);

                                                            // create the popup window
                                                            int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                                            boolean focusable = true; // lets taps outside the popup also dismiss it
                                                            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                                                            // show the popup window
                                                            popupWindow.showAtLocation(mapLayout, Gravity.TOP, 0, 0);
                                                            // dismiss the popup window when touched
                                                            popupView.setOnTouchListener(new View.OnTouchListener() {
                                                                @Override
                                                                public boolean onTouch(View v, MotionEvent event) {
                                                                    popupWindow.dismiss();
                                                                    return true;
                                                                }
                                                            });
                                                        }
                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {}
                                                    });
                                                }*/
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {}
                                        });
                                    }
                                });
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            popupWindow.showAtLocation(mapLayout, Gravity.TOP, 0, 0);
            // dismiss the popup window when touched
            popupView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
        }
    }

    private void toggleAttend() {
        if (att.getContentDescription().equals("false")) {
            att.setImageResource(R.mipmap.ic_attending_pic); //set to attending
            att.setContentDescription("true");
            maybe.setImageResource(R.mipmap.ic_not_maybe_pic);//set to not maybe
            maybe.setContentDescription("false");
            eventDatabase.child(_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = Event.fromSnapshot(dataSnapshot);
                    //An event knows the people for sure attending it
                    if(event.getAttendees() == null || event.getAttendees().isEmpty()){eventDatabase.child(_eventId).child("attendees").setValue(getUid()+"`");}
                    else if (!event.getAttendees().contains(getUid()+"`")){//1 user can attend 1x (prevent duplicate attendees)
                        eventDatabase.child(_eventId).child("attendees").setValue(event.getAttendees() + getUid()+"`");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = User.fromSnapshot(dataSnapshot);
                    //User gets to know events he/she is attending
                    if(user.getAttendEid().isEmpty()){userDatabase.child(getUid()).child("attendEid").setValue(_eventId+"`"+title.getText()+"`");}
                    else if (!user.getAttendEid().contains(_eventId+"`"+title.getText()+"`")){//only let user add a "new" attending event (don't allow duplicate)
                        userDatabase.child(getUid()).child("attendEid").setValue(user.getAttendEid() + _eventId+"`"+title.getText()+"`");
                    }
                    userDatabase.child(getUid()).child("maybeEid").setValue(user.getMaybeEid().replace(_eventId+"`"+title.getText()+"`", ""));//remove maybe
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
        else {
            att.setImageResource(R.mipmap.ic_not_attending_pic);
            att.setContentDescription("false");
            userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = User.fromSnapshot(dataSnapshot);
                    //User gets to know events he/she is attending
                    userDatabase.child(getUid()).child("attendEid").setValue(user.getAttendEid().replace(_eventId+"`"+title.getText()+"`", ""));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            eventDatabase.child(_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = Event.fromSnapshot(dataSnapshot);
                    //An event knows the people for sure attending it
                    eventDatabase.child(_eventId).child("attendees").setValue(event.getAttendees().replace(getUid()+"`", ""));

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    public void attending_btn_on_click(View v) {
        if (!checkConflicts(v.getId()))
            toggleAttend();
    }

    private void toggleMaybe() {
        if(maybe.getContentDescription().equals("false")) {
            maybe.setImageResource(R.mipmap.ic_maybe_pic);
            maybe.setContentDescription("true");
            att.setImageResource(R.mipmap.ic_not_attending_pic);
            att.setContentDescription("false");
            userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = User.fromSnapshot(dataSnapshot);
                    //Only the user knows the events he MAY BE attending
                    if (user.getMaybeEid().isEmpty()) {userDatabase.child(getUid()).child("maybeEid").setValue(_eventId + "`" + title.getText() + "`");}
                    else if (!user.getMaybeEid().contains(_eventId + "`" + title.getText() + "`")) {
                        userDatabase.child(getUid()).child("maybeEid").setValue(user.getMaybeEid() + _eventId + "`" + title.getText() + "`");
                    }
                    userDatabase.child(getUid()).child("attendEid").setValue(user.getAttendEid().replace(_eventId + "`" + title.getText() + "`", ""));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            eventDatabase.child(_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = Event.fromSnapshot(dataSnapshot);
                    //An event knows the people for sure attending it
                    if(event.getAttendees() != null && !event.getAttendees().isEmpty())
                        eventDatabase.child(_eventId).child("attendees").setValue(event.getAttendees().replace(getUid() + "`", ""));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else{
            maybe.setImageResource(R.mipmap.ic_not_maybe_pic);
            maybe.setContentDescription("false");
            userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = User.fromSnapshot(dataSnapshot);
                    //User gets to know events he/she may be attending
                    if(user.getMaybeEid() != null && !user.getMaybeEid().isEmpty())
                        userDatabase.child(getUid()).child("maybeEid").setValue(user.getMaybeEid().replace(_eventId+"`"+title.getText()+"`", ""));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    public void maybe_btn_on_click(View v) {
        if (!checkConflicts(v.getId()))
            toggleMaybe();
    }

    //
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

    private boolean setOrErrorField(EditText thing, Event event, final BiPredicate<EditText, Event> set_function)
    {
        if(set_function.test(thing, event))
            return true;
        thing.setError("Invalid field");
        return false;
    }

    private void getConflictData() {
        userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);

                if (user != null){
                    busyTimes = user.getBusyTimes();

                    List<String> attendEids = new ArrayList<String>(Arrays.asList(user.getAttendEid().split("`")));
                    List<String> maybeEids = new ArrayList<String>(Arrays.asList(user.getMaybeEid().split("`")));

                    for (int i = 0; i < attendEids.size(); i++) {
                        if (i % 2 == 1)
                            continue;
                        final int index = i;
                        eventDatabase.child(attendEids.get(index)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //TODO
                                Event e = Event.fromSnapshot(dataSnapshot);
                                if (e != null)
                                    interestedEvents.add(e);
                                else {//remove event to deleted event
                                    String attendEvId = attendEids.get(index);
                                    attendEids.remove(index);
                                    attendEids.remove(index + 1);
                                    String newAttendees = MethodOrphanage.convertToDBFormat(attendEids);
                                    //update user's attend list
                                    userDatabase.child(getUid()).child(User.DB_ATTENDING_IDS).setValue(attendEids);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    for (int i = 0; i < maybeEids.size(); i++) {
                        if (i % 2 == 1)
                            continue;
                        final int index = i;
                        eventDatabase.child(maybeEids.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Event e = Event.fromSnapshot(dataSnapshot);
                                if (e != null)
                                    interestedEvents.add(e);
                                else {//remove reference to deleted event
                                    maybeEids.remove(index); //remove key
                                    maybeEids.remove(index + 1); //remove event title
                                    String maybeAttendees = MethodOrphanage.convertToDBFormat(maybeEids);
                                    //update users's maybe list
                                    userDatabase.child(getUid()).child(User.DB_MAYBE_IDS).setValue(maybeAttendees);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean checkConflicts(int buttonId) {
        String conflictName = null;

        if (att.getContentDescription().equals("true") || maybe.getContentDescription().equals("true"))
            return false;

        if (busyTimes != null) {
            for (BusyTime time : busyTimes) {
                if (fetchedEvent.overlaps(time)) {
                    conflictName = getString(R.string.conflict_busy_time);
                    break;
                }
            }
        }

        if (conflictName == null) {
            for (Event e : interestedEvents) {
                if (!fetchedEvent.equals(e) && fetchedEvent.overlaps(e)) {
                    conflictName = String.format(getString(R.string.conflict_event), e.getTitle());
                    break;
                }
            }
        }

        if (conflictName != null) {
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_warning)
                    .setTitle("Time Conflict")
                    .setMessage(String.format(getString(R.string.conflict_warning), conflictName))
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (buttonId == R.id.attending_btn)
                                toggleAttend();
                            else
                                toggleMaybe();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        return conflictName != null;
    }

    private String validateFields(Event event)
    {
        String badField = "";
        //title
        final BiPredicate<EditText, Event> setTitle = (text, e)-> e.setTitle(text.getText().toString().replaceAll("`", ""));
        if(!setOrErrorField(title, event, setTitle))
            badField += "Title, ";
        //description
        final BiPredicate<EditText, Event> setDescription = (text, e)-> e.setDescription(text.getText().toString());
        if(!setOrErrorField(description, event, setDescription))
            badField += "Description, ";
        //start date
        final BiPredicate<EditText, Event> setStartDate = (text, e)-> e.setStartDate(Event.parseDate(text.getText().toString()));
        if(!setOrErrorField(startDate, event, setStartDate))
            badField += "Start Date, ";
        //end date
        final BiPredicate<EditText, Event> setEndDate = (text, e)-> e.setEndDate(Event.parseDate(text.getText().toString()));
        if(!setOrErrorField(endDate, event, setEndDate))
            badField += "End Date, ";
        //start time
        final BiPredicate<EditText, Event> setStartTime = (text, e)-> e.setStartTime(Event.parseTime(text.getText().toString()));
        if(!setOrErrorField(startTime, event, setStartTime))
            badField += "Start Time, ";
        //end time
        final BiPredicate<EditText, Event> setEndTime = (text, e)-> e.setEndTime(Event.parseTime(text.getText().toString()));
        if(!setOrErrorField(endTime, event, setEndTime))
            badField += "End Time, ";
        //suggested age
        final BiPredicate<EditText, Event> setAge = (text, e)-> e.setSuggestedAge(Integer.parseInt(text.getText().toString()));
        if(!setOrErrorField(suggestedAge, event, setAge))
            badField += "Suggested Age, ";
        //cost
        final BiPredicate<EditText, Event> setCost = (text, e)-> e.setCost(Double.parseDouble(text.getText().toString()));
        if(!setOrErrorField(cost, event, setCost))
            badField += "Cost, ";
        //rating
        final BiPredicate<EditText, Event> setRating = (text, e)-> e.setRating(Integer.parseInt(text.getText().toString()));
        if(!setOrErrorField(rating, event, setRating))
            badField += "Rating, ";

        return badField;
    }


}
