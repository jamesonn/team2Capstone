package team2.mkesocial.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Firebase.Event;
import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;
import static team2.mkesocial.Activities.BaseActivity.getUid;

public class EventActivity extends BaseActivity implements ValueEventListener {

    private FirebaseDatabase _database;
    private Query _dataQuery;
    private String _eventId, attenders;
    private EditText title, description, startDate, endDate, startTime, endTime, location, hostUid, suggestedAge, rating, cost;
    private Button editButton, deleteButton;
    private String[] _keys = { "title=", "description=", "date=", "startTime=", "endTime=", "location=", "hostUid=", "suggestedAge=", "rating=", "cost="};
    private ArrayList<EditText> objectList = new ArrayList<EditText>();
    private boolean editing = false;
    private String fullLocation = "";
    private ImageButton att, maybe, attendees;

    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);

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

        attendees = (ImageButton) findViewById(R.id.attenders_btn);
        attendees.bringToFront();
        att = (ImageButton) findViewById(R.id.attending_btn);
        att.bringToFront();
        maybe = (ImageButton) findViewById(R.id.maybe_btn);
        maybe.bringToFront();

        iconsToDisplay();

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


        // Edit field disabled

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

    private void iconsToDisplay(){
        userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                //User gets to know events he/she is attending
                if(user.getAttendEid().isEmpty()|| !user.getAttendEid().contains(_eventId+"`"+title.getText()+"`")){
                    att.setImageResource(R.mipmap.ic_not_attending_pic);
                }
                else{ att.setImageResource(R.mipmap.ic_attending_pic);}
                if(user.getMaybeEid().isEmpty()|| !user.getMaybeEid().contains(_eventId+"`"+title.getText()+"`")){
                    maybe.setImageResource(R.mipmap.ic_not_maybe_pic);
                }
                else{  maybe.setImageResource(R.mipmap.ic_maybe_pic);}
             }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void populateEventData(DataSnapshot data){
        Event event = Event.fromSnapshot(data);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

        title.setText(event.getTitle());
        description.setText(event.getDescription());
        startDate.setText(dateFormatter.format(event.getStartDate().getTime()));
        endDate.setText(dateFormatter.format(event.getEndDate().getTime()));
        startTime.setText(timeFormatter.format(event.getStartTime().getTime()));
        endTime.setText(timeFormatter.format(event.getEndTime().getTime()));
        location.setText(event.getFullAddress());

        // gotta store the actual location to be able to restore location
        fullLocation = event.getLocation();

        int ageData = event.getSuggestedAge();
        if (ageData != -1)
            suggestedAge.setText(Integer.toString(ageData));

        int ratingData = event.getRating();
        if (ratingData != -1)
            rating.setText(Integer.toString(ratingData));

        double costData = event.getCost();
        if (costData != -1.0f)
            cost.setText(String.format("%.2f", costData));

        // If user is the event host, give them an option to edit their event
        if(event.getHostUid().equals(getUid()))
            editingEvent();
        else {//hide buttons
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
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
        // Save changed fields to DB
        // We'll just trust they don't need any input validation at this time ;)
        // TODO input validation
        while(fullLocation.isEmpty())
            Toast.makeText(getApplicationContext(), "Waiting for DB to retrieve location", Toast.LENGTH_LONG).show();
        Event newEvent = new Event(title.getText().toString(), description.getText().toString(),
                startDate.getText().toString(), endDate.getText().toString(), startTime.getText().toString(),
                endTime.getText().toString(), fullLocation,
                getUid(), "",  suggestedAge.getText().toString(), "", cost.getText().toString(), "");
        // Add event obj to database under its event ID
        FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME).child(_eventId).updateChildren(newEvent.toMap());
        finish();

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
                                                    inspectUser(key);
                                                }
                                                else {
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
                                                            String msg = "The Host has cancelled the event!\nIt will now be removed.";
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
                                                            recreate();
                                                        }
                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {}
                                                    });
                                                }
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

    public void attending_btn_on_click(View v) {
        if (v.getId() == R.id.attending_btn) {
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
                        if(event.getAttendees().isEmpty()){eventDatabase.child(_eventId).child("attendees").setValue(getUid()+"`");}
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
    }

    public void maybe_btn_on_click(View v) {
        if (v.getId() == R.id.maybe_btn) {
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
                        userDatabase.child(getUid()).child("maybeEid").setValue(user.getMaybeEid().replace(_eventId+"`"+title.getText()+"`", ""));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }

    }

}
