package team2.mkesocial.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import Firebase.Event;
import Firebase.MethodOrphanage;
import Firebase.Settings;
import Firebase.User;
import Validation.WordScrubber;
import team2.mkesocial.Constants;
import team2.mkesocial.R;
import java.io.IOException;


import com.bumptech.glide.Glide;
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

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;

import static android.content.ContentValues.TAG;
import static team2.mkesocial.Activities.BaseActivity.getUid;

/**
* Created by IaOng on 10/15/2017.
*/

public class ProfileActivity extends BaseActivity {


private ImageView profile_picture, npic;
private Bitmap adjustedBitmap;
private static final int PICK_IMAGE_REQUEST = 234;
private Uri filePath, oldFilePath;
private String oldImg;

private TextView profile_email, profile_bio, profile_fName, eventsL;
private EditText pro_email, pro_bio, pro_fName, pro_lName, pro_mInit, pro_age;
private Switch email_toggle, attend_toggle, maybe_toggle, host_toggle;
boolean see_email=true, see_attend=true, see_maybe=true, see_host=true, addrC=false, picChange=false;
private LinearLayout events_attend_layout, events_maybe_layout, events_host_layout;
private String aIDs, hIDs, mIDs, userId;

private boolean edit_mode=false;

private PlaceAutocompleteFragment autocompleteFragment;
private Place placePicked;

private StorageReference storageReference  = FirebaseStorage.getInstance().getReference();
private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);

@Override
protected void onCreate(Bundle savedInstanceState) {
    if(Settings.setDarkTheme())
        setTheme(R.style.MKEDarkTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    userId = getIntent().getStringExtra("USER_ID");

    edit_mode=false;
    if(userId == null){userId=getUid();}
    quickUpdatePA();
}

public void edit_btn_on_click(View v){
    //click on Edit Button
    if(v.getId() == R.id.edit_button) {
        edit_mode=true;
        //Set the xml layout to be the editable one: activity_edit_profile
        setContentView(R.layout.activity_edit_profile);
        pro_email=(EditText)findViewById(R.id.email_addr);
        pro_bio =(EditText)findViewById(R.id.about_me_bio);
        pro_fName = (EditText)findViewById(R.id.first_name);
        pro_age = (EditText)findViewById(R.id.age_year);
        pro_lName = (EditText)findViewById(R.id.last_nam);
        pro_mInit = (EditText)findViewById(R.id.middle_init);
        email_toggle = (Switch) findViewById(R.id.tog_email);
        host_toggle = (Switch) findViewById(R.id.tog_host_events);
        attend_toggle = (Switch) findViewById(R.id.tog_events);
        maybe_toggle = (Switch) findViewById(R.id.tog_m_events);
        profile_picture = (ImageView) findViewById(R.id.profile_ebg);
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.addr_autocomplete_fragment);

        filterText();

        userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                if (user != null) {
                    Glide.with(getApplicationContext()).load(user.getImg()).into(profile_picture);
                    oldImg =user.getImg();
                    pro_email.setText(user.getEmail());
                    pro_bio.setText(user.getBio());
                    pro_fName.setText(user.getName());
                    pro_age.setText(user.getAge());
                    pro_lName.setText(user.getLname());
                    pro_mInit.setText(user.getInitm());
                    email_toggle.setChecked(Boolean.parseBoolean(user.getEtog()));
                    attend_toggle.setChecked(Boolean.parseBoolean(user.getEattend()));
                    maybe_toggle.setChecked(Boolean.parseBoolean(user.getEmaybe()));
                    host_toggle.setChecked(Boolean.parseBoolean(user.getEhost()));
                    autocompleteFragment.setHint(user.getAddress());
                    if(user.getAddress().isEmpty())autocompleteFragment.setHint("Search for your home address");

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //edit location fragment auto complete
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                addrC=true;
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
}

    @Override
    public void onBackPressed() {
    if(userId.equals(getUid()) && edit_mode) {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_warning)
                .setTitle("Back Confirmation")
                .setMessage("Are you sure you want to go back without saving?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    else{super.onBackPressed();}
    }
public void edit_pic_on_click(View v) {
    //click on Edit Photo button --> Start pick image intent
    if (v.getId() == R.id.edit_photo) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
}

//Handle the ACTION_GET_CONTENT onActivity Result
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
        oldFilePath=filePath;
        filePath = data.getData();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            //For correcting orientation so it displays correctly (on images that where taken sideways/upside-down)
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(filePath));
            //get current rotation...
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            //convert to degrees
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            //Update on the Edit Profile Page to show user what the new image would look like
            //ToDO: If there's time, implement ability to move image around and save the orientation (EXTRA)
            profile_picture = (ImageView)findViewById(R.id.profile_ebg);
            profile_picture.setImageBitmap(adjustedBitmap);
            picChange=true;
        } catch (IOException e) {
            e.printStackTrace();
            picChange=false;
            Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
        }
    }
}

public void save_btn_on_click(View v){
    //Idea: Update (DB + Profile View) only when a field has been changed; else leave it alone
    if(v.getId() == R.id.save_button) {
        edit_mode=false;
        //Initialize fields that exist only for Profile Edit View
        pro_mInit = (EditText)findViewById(R.id.middle_init);
        pro_lName = (EditText)findViewById(R.id.last_nam);
        pro_age = (EditText)findViewById(R.id.age_year);
        email_toggle = (Switch) findViewById(R.id.tog_email);
        attend_toggle = (Switch) findViewById(R.id.tog_events);
        maybe_toggle = (Switch) findViewById(R.id.tog_m_events);
        host_toggle = (Switch) findViewById(R.id.tog_host_events);
        npic = (ImageView)findViewById(R.id.profile_bg);

        //Get the information entered in each field...
        String fName = pro_fName.getText().toString();
        String bio = pro_bio.getText().toString();
        String email = pro_email.getText().toString();

        ////////////Data in between here never gets shown publicly////////////// <-- AKA: Data here only exist for the Profile Edit View
        String mInit = pro_mInit.getText().toString();
        String lName = pro_lName.getText().toString();
        String age = pro_age.getText().toString();
        ///////////////////////////////////////////////////////////////////////

        //Update Database IFF the field got changed
        if(!fName.isEmpty()){ userDatabase.child(getUid()).child("name").setValue(fName);}
        if(!bio.isEmpty()){userDatabase.child(getUid()).child("bio").setValue(bio);}
        if(!email.isEmpty()){userDatabase.child(getUid()).child("email").setValue(email);}
        if(!mInit.isEmpty()){userDatabase.child(getUid()).child("initm").setValue(mInit);}
        if(!lName.isEmpty()){userDatabase.child(getUid()).child("lname").setValue(lName);}
        if(!age.isEmpty()){userDatabase.child(getUid()).child("age").setValue(age);}

        //Always update the profile view fields to reflect the changes
        fName=pro_fName.getText().toString();
        bio=pro_bio.getText().toString();
        email=pro_email.getText().toString();


        //Check the toggle bars
        see_email = email_toggle.isChecked();
        if(see_email) {userDatabase.child(getUid()).child("etog").setValue("true");}
        else {userDatabase.child(getUid()).child("etog").setValue("false");}

        //Attend Events toggle
        see_attend = attend_toggle.isChecked();
        if(see_attend) {userDatabase.child(getUid()).child("eattend").setValue("true");}
        else {userDatabase.child(getUid()).child("eattend").setValue("false");}

        //Maybe Events toggle
        see_maybe = maybe_toggle.isChecked();
        if(see_maybe) {userDatabase.child(getUid()).child("emaybe").setValue("true");}
        else {userDatabase.child(getUid()).child("emaybe").setValue("false");}

        //Host Events toggle
        see_host = host_toggle.isChecked();
        if(see_host) {userDatabase.child(getUid()).child("ehost").setValue("true");}
        else {userDatabase.child(getUid()).child("ehost").setValue("false");}

        ////////////////////////////////////////Information below is from activity_profile////////////////////////////////////////////////////////
        setContentView(R.layout.activity_profile);
        npic = (ImageView)findViewById(R.id.profile_bg);
        profile_fName = (TextView)findViewById(R.id.first_name);
        profile_bio = (TextView)findViewById(R.id.about_me_bio);
        profile_email = (TextView)findViewById(R.id.email_addr);

        //Update the display values
        npic.setImageBitmap(adjustedBitmap);
        if(picChange){
            uploadFile(); //Upload the newly selected image & delete the old image IFF new image != old image
            picChange=false;//to reset it for the next intent spawn
        }
        profile_fName.setText(fName);
        profile_bio.setText(bio);
        profile_email.setText(email);

        //Handle changes to Address on Fragment
        if(addrC){userDatabase.child(getUid()).child("address").setValue(placePicked.getAddress().toString()+":"+placePicked.getLatLng().toString());}
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.addr_autocomplete_fragment)).commit();
        quickUpdatePA();
    }
}

private void quickUpdatePA() {
    setContentView(R.layout.activity_profile);
    //Default Active Profile shows first name, bio, email, events attending, and events hosting
    profile_email = (TextView) findViewById(R.id.email_addr);
    profile_bio = (TextView) findViewById(R.id.about_me_bio);
    profile_fName = (TextView) findViewById(R.id.first_name);
    events_attend_layout = (LinearLayout) findViewById(R.id.for_att_events);
    events_maybe_layout = (LinearLayout) findViewById(R.id.for_m_events);
    events_host_layout = (LinearLayout) findViewById(R.id.for_host_events);
    npic = (ImageView) findViewById(R.id.profile_bg);

    if(!getUid().equals(userId)){
        //hide edit button
        ImageButton edit = (ImageButton) findViewById(R.id.edit_button);
        edit.setVisibility(View.INVISIBLE);


    }


    //Idea: Retrieve information from DB to know what items should/shouldn't be displayed
    //Other Users who view this profile will only what Owner of the Profile chooses to show (in regards to email and events)
    userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = User.fromSnapshot(dataSnapshot);

            //Idea: If the toggle says off, Header + Fields are all gone from view
            TextView email_head = (TextView) findViewById(R.id.contact_info);
            TextView attend_head = (TextView) findViewById(R.id.shared_events);
            TextView maybe_head = (TextView) findViewById(R.id.shared_m_events);
            TextView host_head = (TextView) findViewById(R.id.shared_host_events);

            if(!getUid().equals(userId) && user!=null) {
                if(user.getEtog()!=null) {
                    if (Boolean.parseBoolean(user.getEtog())) {
                        profile_email.setVisibility(View.VISIBLE);
                        email_head.setVisibility(View.VISIBLE);
                    } else if (!Boolean.parseBoolean(user.getEtog())) {
                        profile_email.setVisibility(View.GONE);
                        email_head.setVisibility(View.GONE);
                    }
                }
                if(user.getEattend()!=null) {
                    if (Boolean.parseBoolean(user.getEattend())) {
                        attend_head.setVisibility(View.VISIBLE);
                        events_attend_layout.setVisibility(View.VISIBLE);
                    } else if (!Boolean.parseBoolean(user.getEattend())) {
                        attend_head.setVisibility(View.GONE);
                        events_attend_layout.setVisibility(View.GONE);
                    }
                }
                if(user.getEmaybe()!=null) {
                    if (Boolean.parseBoolean(user.getEmaybe())) {
                        maybe_head.setVisibility(View.VISIBLE);
                        events_maybe_layout.setVisibility(View.VISIBLE);
                    } else if (!Boolean.parseBoolean(user.getEmaybe())) {
                        maybe_head.setVisibility(View.GONE);
                        events_maybe_layout.setVisibility(View.GONE);
                    }
                }
                if(user.getEhost()!=null) {
                    if (Boolean.parseBoolean(user.getEhost())) {
                        host_head.setVisibility(View.VISIBLE);
                        events_host_layout.setVisibility(View.VISIBLE);
                    } else if (!Boolean.parseBoolean(user.getEhost())) {
                        host_head.setVisibility(View.GONE);
                        events_host_layout.setVisibility(View.GONE);
                    }
                }
            }
            if(user!=null) {
                if (user.getAttendEid() != null && user.parseEventAttendIDs() != null)
                    aIDs = user.parseEventAttendIDs();
                if (user.getHostEid() != null && user.parseEventHostIDs() != null)
                    hIDs = user.parseEventHostIDs();
                if (user.getMaybeEid() != null && user.parseEventHostIDs() != null)
                    mIDs = user.parseEventMaybeIDs();

                populateAttend(user.parseEventAttendNames());
                populateHost(user.parseEventHostNames());
                populateMaybe(user.parseEventMaybeNames());
            }
            Glide.with(getApplicationContext()).load(user.getImg()).into(npic);

            //Set text fields
            profile_email.setText(user.getEmail());
            profile_bio.setText(user.getBio());
            profile_fName.setText(user.getName());

            //Populate the events (Owner of Profile always sees them!)
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    });
}

private void filterText(){
    //filter first name, last name, bio, and email
    pro_fName.addTextChangedListener(new TextWatcher(){
        WordScrubber wordScrubber = new WordScrubber(getApplicationContext());
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().isEmpty()){
                pro_fName.removeTextChangedListener(this);//"pause" checker

                String cleanString = s.toString();
                //check as user types if word becomes a swear word!
                if(s.length() > 1 ){
                    String[] words = cleanString.split("[\\p{Punct}\\s]+");
                    for(int i=0; i<words.length; ++i){
                        if(wordScrubber.isBadWord(words[i])){
                            cleanString=wordScrubber.filterHiddenBadWords(cleanString);
                            pro_fName.setText(cleanString);
                            pro_fName.setSelection(cleanString.length());
                        }
                    }
                }
                if(cleanString.isEmpty()){  pro_fName.setError("Please input a valid bio");}

                pro_fName.addTextChangedListener(this);//"resume" checker
            }
        }
    });

    pro_bio.addTextChangedListener(new TextWatcher(){
        WordScrubber wordScrubber = new WordScrubber(getApplicationContext());
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().isEmpty()){
                pro_bio.removeTextChangedListener(this);//"pause" checker

                String cleanString = s.toString();
                //check as user types if word becomes a swear word!
                if(s.length() > 1 ){
                    String[] words = cleanString.split("[\\p{Punct}\\s]+");
                    for(int i=0; i<words.length; ++i){
                        if(wordScrubber.isBadWord(words[i])){
                            cleanString=wordScrubber.filterHiddenBadWords(cleanString);
                            pro_bio.setText(cleanString);
                            pro_bio.setSelection(cleanString.length());
                        }
                    }
                }
                if(cleanString.isEmpty()){  pro_bio.setError("Please input a valid bio");}

                pro_bio.addTextChangedListener(this);//"resume" checker
            }
        }
    });

    pro_lName.addTextChangedListener(new TextWatcher(){
        WordScrubber wordScrubber = new WordScrubber(getApplicationContext());
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().isEmpty()){
                pro_lName.removeTextChangedListener(this);//"pause" checker

                String cleanString = s.toString();
                //check as user types if word becomes a swear word!
                if(s.length() > 1 ){
                    String[] words = cleanString.split("[\\p{Punct}\\s]+");
                    for(int i=0; i<words.length; ++i){
                        if(wordScrubber.isBadWord(words[i])){
                            cleanString=wordScrubber.filterHiddenBadWords(cleanString);
                            pro_lName.setText(cleanString);
                            pro_lName.setSelection(cleanString.length());
                        }
                    }
                }
                if(cleanString.isEmpty()){  pro_lName.setError("Please input a valid bio");}

                pro_lName.addTextChangedListener(this);//"resume" checker
            }
        }
    });

    pro_email.addTextChangedListener(new TextWatcher(){
        WordScrubber wordScrubber = new WordScrubber(getApplicationContext());
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().isEmpty()){
                pro_email.removeTextChangedListener(this);//"pause" checker

                String cleanString = s.toString();
                //check as user types if word becomes a swear word!
                if(s.length() > 1 ){
                    String[] words = cleanString.split("[\\p{Punct}\\s]+");
                    for(int i=0; i<words.length; ++i){
                        if(wordScrubber.isBadWord(words[i])){
                            cleanString=wordScrubber.filterHiddenBadWords(cleanString);
                            pro_email.setText(cleanString);
                            pro_email.setSelection(cleanString.length());
                        }
                    }
                }
                if(cleanString.isEmpty()){  pro_email.setError("Please input a valid bio");}

                pro_email.addTextChangedListener(this);//"resume" checker
            }
        }
    });

}


private void populateAttend(String names){

    String[] id = aIDs.split("`");
    String[] sep = names.split("`");
    if(sep.length>=1 && id.length>=1) {
        for (int i = 0; i < sep.length; ++i) {
            TextView eventsL = new TextView(this);
            eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            eventsL.setText(sep[i]);
            eventsL.setTextSize(20);
            eventsL.setTextColor(0xff424242);
            eventsL.setAllCaps(false);
            eventsL.setClickable(true);
            eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            eventsL.setVisibility(View.VISIBLE);
            eventsL.setId(i);
            eventsL.setContentDescription(id[i]);
            events_attend_layout.addView(eventsL);

            eventsL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = eventsL.getContentDescription().toString();
                    eventDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(key)) {
                                inspectEvent(key);
                            }
                            else {
                                userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = User.fromSnapshot(dataSnapshot);
                                        //Remove event from user's DB profile
                                        userDatabase.child(userId).child("attendEid").setValue(user.getAttendEid().replace(eventsL.getContentDescription().toString()+"`"+eventsL.getText()+"`", ""));
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
                                        quickUpdatePA();
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

private void populateMaybe(String names){

        String[] id = mIDs.split("`");
        String[] sep = names.split("`");
        if(sep.length>=1 && id.length>=1) {
            for (int i = 0; i < sep.length; ++i) {
                TextView eventsL = new TextView(this);
                eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                eventsL.setText(sep[i]);
                eventsL.setTextSize(20);
                eventsL.setTextColor(0xff424242);
                eventsL.setAllCaps(false);
                eventsL.setClickable(true);
                eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                eventsL.setVisibility(View.VISIBLE);
                eventsL.setId(10000+i);
                eventsL.setContentDescription(id[i]);
                events_maybe_layout.addView(eventsL);

                eventsL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = eventsL.getContentDescription().toString();
                        eventDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild(key)) {
                                    inspectEvent(key);
                                }
                                else {
                                    userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User user = User.fromSnapshot(dataSnapshot);
                                            //Remove event from user's DB profile
                                            userDatabase.child(userId).child("maybeEid").setValue(user.getMaybeEid().replace(eventsL.getContentDescription().toString()+"`"+eventsL.getText()+"`", ""));
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
                                            quickUpdatePA();
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

private void populateHost(String names){
    String[] id = hIDs.split("`");
    String[] sep = names.split("`");
    if(sep.length>=1 && id.length>=1) {
        for (int i = 0; i < sep.length; ++i) {
            TextView eventsL = new TextView(this);
            eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            eventsL.setText(sep[i]);
            eventsL.setTextSize(20);
            eventsL.setTextColor(0xff424242);
            eventsL.setAllCaps(false);
            eventsL.setClickable(true);
            eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            eventsL.setVisibility(View.VISIBLE);
            eventsL.setId(5000 + i);
            eventsL.setContentDescription(id[i]);
            events_host_layout.addView(eventsL);

            eventsL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String key = eventsL.getContentDescription().toString();
                    eventDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(key)) {
                                inspectEvent(key);
                            }
                            else {
                                userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = User.fromSnapshot(dataSnapshot);
                                        //Remove event from user's DB profile
                                        MethodOrphanage.updateUserHosting(userDatabase, userId, user.getHostEid()
                                                , eventsL.getContentDescription().toString(), eventsL.getText().toString());
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
                                        quickUpdatePA();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
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

/*************************************
 * Helper Methods:
 * setting up image picker
 * retrieving image
 * uploading (store new, delete old)
 * ************************************/
//get file extension information from URI info on picture
private String getFileExtension(Uri uri) {
    ContentResolver cR = getContentResolver();
    MimeTypeMap mime = MimeTypeMap.getSingleton();
    return mime.getExtensionFromMimeType(cR.getType(uri));
}

//upload picture to firebase storage and to update storage + database
//ToDo Delete old image from firebase when user decides to upload a new picture
private void uploadFile() {
    //checking if file is available
    if (filePath != null && picChange && !filePath.equals(oldFilePath)) {
        //displaying progress dialog while image is uploading
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating");
        progressDialog.show();

        //getting the storage reference
        StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(filePath));

        //adding the file to reference
        sRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //dismissing the progress dialog
                        progressDialog.dismiss();

                        //displaying success toast
                        Toast.makeText(getApplicationContext(), "Profile Updated ", Toast.LENGTH_LONG).show();
                        userDatabase.child(getUid()).child("img").setValue(taskSnapshot.getDownloadUrl().toString());
                        //update Profile View
                        quickUpdatePA();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Updated " + ((int) progress) + "%...");
                    }
                });

        //Delete old image
        if (!oldImg.isEmpty()) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImg);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.e("firebasestorage", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.e("firebasestorage", "onFailure: did not delete file");
                }
            });
        }
    }
}

//fix rotation issues
private static int exifToDegrees(int exifOrientation) {
    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
    else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
    else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
    return 0;
}

}
