package team2.mkesocial.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import Firebase.Event;
import Firebase.User;
import team2.mkesocial.Constants;
import team2.mkesocial.R;
import java.io.IOException;


import com.bumptech.glide.Glide;
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

import static team2.mkesocial.Activities.BaseActivity.getUid;

/**
 * Created by IaOng on 10/15/2017.
 */

public class ProfileActivity extends Activity {

    //public static final int IMAGE_GALLERY_REQUEST = 20;
    private ImageView profile_picture, npic;
    private Bitmap bitmap, adjustedBitmap;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    //private DatabaseReference userAttendingEventDatabase = FirebaseDatabase.getInstance().getReference(DB_USER_EVENTS_ATTENDING_NODE_NAME);
    //private DatabaseReference userHostEventDatabase = FirebaseDatabase.getInstance().getReference(DB_USER_EVENTS_HOSTING_NODE_NAME);
    //Uri imgUri;
    TextView profile_email, profile_bio, profile_fName, email_head, attend_head, host_head, eventsL;
    EditText pro_email, pro_bio, pro_fName, pro_lName, pro_mInit, pro_age, pro_addr;
    Switch email_toggle, attend_toggle, host_toggle;
    boolean see_email=true, see_attend=true, see_host=true, picChange;
    String email, bio, fName, lName, mInit, age, addr, attendEid, hostEid, rEName, eName;
    String[] EventAid, EventHid, Anames, Hnames;
    LinearLayout events_attend_layout, events_host_layout;
    //List<String> attending = new ArrayList<String>(), hosting = new ArrayList<String>();
    int numEventsAtt, numEventsHost;
    private StorageReference storageReference  = FirebaseStorage.getInstance().getReference();
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quickUpdatePA();
    }

    public void edit_btn_on_click(View v){
        //click on Edit Button
        if(v.getId() == R.id.edit_button) {
            //Set the xml layout to be the editable one: activity_edit_profile
            setContentView(R.layout.activity_edit_profile);
            pro_email=(EditText)findViewById(R.id.email_addr);
            pro_bio =(EditText)findViewById(R.id.about_me_bio);
            pro_fName = (EditText)findViewById(R.id.first_name);
            pro_age = (EditText)findViewById(R.id.age_year);
            pro_addr = (EditText)findViewById(R.id.address);
            pro_lName = (EditText)findViewById(R.id.last_nam);
            pro_mInit = (EditText)findViewById(R.id.middle_init);
            email_toggle = (Switch) findViewById(R.id.tog_email);
            host_toggle = (Switch) findViewById(R.id.tog_host_events);
            attend_toggle = (Switch) findViewById(R.id.tog_events);
            profile_picture = (ImageView) findViewById(R.id.profile_ebg);

            userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                        //ToDo: Figure out how to view other profiles and make changes/updates to this code as necessary
                        if(childSnap.getKey().equals(getUid())) { //if the child node's uid = current uid because we ONLY display current uid info on myProfile page
                            User info = childSnap.getValue(User.class);//now able retrieve all info of the fields of that node
                            //for profile view
                            if(info!=null) {
                                Glide.with(getApplicationContext()).load(info.getImg()).into(profile_picture);
                                pro_email.setText(info.getEmail());
                                pro_bio.setText(info.getBio());
                                pro_fName.setText(info.getName());
                                pro_age.setText(info.getAge());
                                pro_addr.setText(info.getAddress());
                                pro_lName.setText(info.getLname());
                                pro_mInit.setText(info.getInitm());
                                email_toggle.setChecked(Boolean.parseBoolean(info.getEtog()));
                                attend_toggle.setChecked(Boolean.parseBoolean(info.getEattend()));
                                host_toggle.setChecked(Boolean.parseBoolean(info.getEhost()));
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError){}
            });
        }
    }

    public void edit_pic_on_click(View v) {
        //click on Edit Photo button
        if (v.getId() == R.id.edit_photo) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

        }
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //System.out.println("I GOT TO BE INSIDE OF TRY..");
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //correcting orientation so it displays correctly
                ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(filePath));
                //System.out.println("//////////////////////////////////////////////////////////"+getContentResolver().openInputStream(filePath));
                //get current rotation...
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                //convert to degrees
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
                adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                //bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profile_picture = (ImageView)findViewById(R.id.profile_ebg);
                profile_picture.setImageBitmap(adjustedBitmap);
                //picChange=true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
               // picChange=false;
            }
        }
        //picChange=false;
    }

    public void save_btn_on_click(View v){
        if(v.getId() == R.id.save_button) {
            //Get the information entered in each field...handle the cases where a field is NOT updated (aka use old value)
            //first name
            //pro_fName = (EditText)findViewById(R.id.first_name);
            fName = pro_fName.getText().toString();
            if(!fName.isEmpty()){ userDatabase.child(getUid()).child("name").setValue(fName);}//only update DB if field is updated
            else{fName=profile_fName.getText().toString();}
            //bio
            //pro_bio = (EditText)findViewById(R.id.about_me_bio);
            bio = pro_bio.getText().toString();
            if(!bio.isEmpty()){userDatabase.child(getUid()).child("bio").setValue(bio);}//only update DB if field is updated
            else{bio=profile_bio.getText().toString();}
            //email
            //pro_email = (EditText)findViewById(R.id.email_addr);
            email = pro_email.getText().toString();
            if(!email.isEmpty()){userDatabase.child(getUid()).child("email").setValue(email);}//only update DB if field is updated
            else{email=profile_email.getText().toString();}

            //==========================================================================================
            //Data in-between here can get updated BUT will never be shown publicly for safety reasons
            //middle name
            pro_mInit = (EditText)findViewById(R.id.middle_init);
            mInit = pro_mInit.getText().toString();
            if(!mInit.isEmpty()){userDatabase.child(getUid()).child("initm").setValue(mInit);}//only update DB if field is updated
            else{mInit=pro_mInit.getText().toString();}
            //last name
            pro_lName = (EditText)findViewById(R.id.last_nam);
            lName = pro_lName.getText().toString();
            if(!lName.isEmpty()){userDatabase.child(getUid()).child("lname").setValue(lName);}//only update DB if field is updated
            else{lName=pro_lName.getText().toString();}
            //age
            pro_age = (EditText)findViewById(R.id.age_year);
            age = pro_age.getText().toString();
            if(!age.isEmpty()){userDatabase.child(getUid()).child("age").setValue(age);}//only update DB if field is updated
            else{age=pro_age.getText().toString();}
            //home address
            pro_addr = (EditText)findViewById(R.id.address);
            addr = pro_addr.getText().toString();
            if(!addr.isEmpty()){userDatabase.child(getUid()).child("address").setValue(addr);}//only update DB if field is updated
            else{addr=pro_addr.getText().toString();}

            //===========================================================================================
            //Check the toggle bars
            //Email toggle
            email_toggle = (Switch) findViewById(R.id.tog_email);
            see_email = email_toggle.isChecked();
            if(see_email) {userDatabase.child(getUid()).child("etog").setValue("true");}
            else {userDatabase.child(getUid()).child("etog").setValue("false");}
            //Attend Events toggle
            attend_toggle = (Switch) findViewById(R.id.tog_events);
            see_attend = attend_toggle.isChecked();
            if(see_attend) {userDatabase.child(getUid()).child("eattend").setValue("true");}
            else {userDatabase.child(getUid()).child("eattend").setValue("false");}
            //Host Events toggle
            host_toggle = (Switch) findViewById(R.id.tog_host_events);
            see_host = host_toggle.isChecked();
            if(see_host) {userDatabase.child(getUid()).child("ehost").setValue("true");}
            else {userDatabase.child(getUid()).child("ehost").setValue("false");}

            ////////////////////////////////////////Information below this is from activity_profile////////////////////////////////////////////////////////
            setContentView(R.layout.activity_profile);
            npic = (ImageView)findViewById(R.id.profile_bg);
            //if(picChange==true)
            uploadFile();
            npic.setImageBitmap(adjustedBitmap);

            //update name
            profile_fName = (TextView)findViewById(R.id.first_name);
            profile_fName.setText(fName);
            //update bio
            profile_bio = (TextView)findViewById(R.id.about_me_bio);
            profile_bio.setText(bio);
            //update email
            profile_email = (TextView)findViewById(R.id.email_addr);
            profile_email.setText(email);

            //If toggle is off; must turn off item
            //else must be able see item
            //Email toggle
            email_head = (TextView)findViewById(R.id.contact_info);
            if(!see_email){
                profile_email.setVisibility(View.GONE);
                email_head.setVisibility(View.GONE);
            }
            else{
                profile_email.setVisibility(View.VISIBLE);
                email_head.setVisibility(View.VISIBLE);
            }

            //Event toggles
            //Attending...
            attend_head = (TextView)findViewById(R.id.shared_events);
            if(!see_attend){
                attend_head.setVisibility(View.GONE);
                events_attend_layout.setVisibility(View.GONE);
            }
            else{
                attend_head.setVisibility(View.VISIBLE);
                events_attend_layout.setVisibility(View.VISIBLE);
                quickUpdatePA();
            }
            //Hosting...
            host_head = (TextView) findViewById(R.id.shared_host_events);
            if(!see_host){
                host_head.setVisibility(View.GONE);
                events_host_layout.setVisibility(View.GONE);
            }
            else{
                host_head.setVisibility(View.VISIBLE);
                events_host_layout.setVisibility(View.VISIBLE);
                if(!see_attend) quickUpdatePA();
            }
        }
    }

    private void quickUpdatePA(){
        setContentView(R.layout.activity_profile);
        //Default Active Profile shows first name, bio, email, events attending, and events hosting
        profile_email = (TextView)findViewById(R.id.email_addr);
        profile_bio =(TextView)findViewById(R.id.about_me_bio);
        profile_fName = (TextView)findViewById(R.id.first_name);
        events_attend_layout = (LinearLayout) findViewById(R.id.for_att_events);
        events_host_layout = (LinearLayout) findViewById(R.id.for_host_events);
        email_head = (TextView)findViewById(R.id.contact_info);
        attend_head = (TextView)findViewById(R.id.shared_events);
        host_head = (TextView)findViewById(R.id.shared_host_events);
        npic =(ImageView)findViewById(R.id.profile_bg);



        /////////ToDo: Figure out how to view other profiles and make changes/updates to this code as necessary//////////////////////////////
        //get data from database about current user
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    //ToDo: Figure out how to view other profiles and make changes/updates to this code as necessary
                    if(childSnap.getKey().equals(getUid())) { //if the child node's uid = current uid because we ONLY display current uid info on myProfile page
                        User info = childSnap.getValue(User.class);//now able retrieve all info of the fields of that node
                        if(info !=null) {

                            if(info.getImg()!=null && !info.getImg().isEmpty()){

                                Glide.with(getApplicationContext()).load(info.getImg()).into(npic);
                            }
                            if (info.getEmail() != null) profile_email.setText(info.getEmail());
                            profile_bio.setText(info.getBio());
                            profile_fName.setText(info.getName());
                            if (Boolean.parseBoolean(info.getEtog())) {
                                profile_email.setVisibility(View.VISIBLE);
                                email_head.setVisibility(View.VISIBLE);
                            } else if (!Boolean.parseBoolean(info.getEtog())) {
                                profile_email.setVisibility(View.GONE);
                                email_head.setVisibility(View.GONE);
                            }
                            if (Boolean.parseBoolean(info.getEattend())) {
                                attend_head.setVisibility(View.VISIBLE);
                                events_attend_layout.setVisibility(View.VISIBLE);
                            } else if (!Boolean.parseBoolean(info.getEattend())) {
                                attend_head.setVisibility(View.GONE);
                                events_attend_layout.setVisibility(View.GONE);
                            }
                            if (Boolean.parseBoolean(info.getEhost())) {
                                host_head.setVisibility(View.VISIBLE);
                                events_host_layout.setVisibility(View.VISIBLE);
                            } else if (!Boolean.parseBoolean(info.getEhost())) {
                                host_head.setVisibility(View.GONE);
                                events_host_layout.setVisibility(View.GONE);
                            }

                            //For events...
                            //Attending...
                            attendEid = info.getAttendEid();
                            if (attendEid != null) {
                                EventAid = attendEid.split(" ");
                                numEventsAtt = EventAid.length;

                                Anames = new String[numEventsAtt];
                                for (int i = 0; i < numEventsAtt; i++) { //loop to fill Anames array with all titles of events user is attending
                                    eName = EventAid[i];
                                    eventDatabase.child(eName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            int x = 0;
                                            Event e = dataSnapshot.getValue(Event.class);//now able retrieve all info of the fields of that node
                                            if (e != null) {
                                                if (e.getTitle() != null) {
                                                    rEName = e.getTitle();
                                                    eventsL = new TextView(ProfileActivity.this);
                                                    eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                    eventsL.setText(rEName);
                                                    eventsL.setTextSize(20);
                                                    eventsL.setTextColor(0xff424242);
                                                    eventsL.setAllCaps(false);
                                                    eventsL.setClickable(true);
                                                    eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                    eventsL.setId(100 + x);
                                                    events_attend_layout.addView(eventsL);
                                                    ++x;
                                                    //ToDo((TextView) findViewById(i)).setOnClickListener(this);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}
                                    });
                                    Anames[i] = rEName;
                                }
                            }
                            //Hosting...
                            hostEid = info.getHostEid();
                            if (hostEid != null) {
                                EventHid = hostEid.split(" ");
                                numEventsHost = EventHid.length;

                                Hnames = new String[numEventsHost];
                                for (int i = 0; i < numEventsHost; i++) { //loop to fill Anames array with all titles of events user is attending
                                    eName = EventAid[i];
                                    eventDatabase.child(eName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            int x = 0;
                                            Event e = dataSnapshot.getValue(Event.class);//now able retrieve all info of the fields of that node
                                            if (e != null) {
                                                if (e.getTitle() != null) {
                                                    rEName = e.getTitle();
                                                    eventsL = new TextView(ProfileActivity.this);
                                                    eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                    eventsL.setText(rEName);
                                                    eventsL.setTextSize(20);
                                                    eventsL.setTextColor(0xff424242);
                                                    eventsL.setAllCaps(false);
                                                    eventsL.setClickable(true);
                                                    eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                    eventsL.setId(500 + x);
                                                    events_host_layout.addView(eventsL);
                                                    ++x;
                                                    //ToDo((TextView) findViewById(i)).setOnClickListener(this);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                    Hnames[i] = rEName;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //helper method to help upload picture to firebase storage and to update storage + database
    //ToDo Delete old image from firebase when user decides to upload a new picture
    private void uploadFile() {
        //checking if file is available
        if (filePath != null) {
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
                            npic.setImageBitmap(adjustedBitmap);
                            //adding an upload to firebase database
                            //String uploadId = mDatabase.push().getKey();
                            userDatabase.child(getUid()).child("img").setValue(taskSnapshot.getDownloadUrl().toString());
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
        } else {
            //display an error if no file is selected
        }
    }

    //Helper method ...  fix rotation issues
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

}
