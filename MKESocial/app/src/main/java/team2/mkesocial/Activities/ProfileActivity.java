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

import android.util.Log;
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

public class ProfileActivity extends Activity {


private ImageView profile_picture, npic;
private Bitmap adjustedBitmap;
private static final int PICK_IMAGE_REQUEST = 234;
private Uri filePath, oldFilePath;
private String oldImg;

private TextView profile_email, profile_bio, profile_fName, eventsL;
private EditText pro_email, pro_bio, pro_fName, pro_lName, pro_mInit, pro_age;
private Switch email_toggle, attend_toggle, host_toggle;
boolean see_email=true, see_attend=true, see_host=true, addrC=false, picChange=false;
private LinearLayout events_attend_layout, events_host_layout;
private String[] aID, hID, attendEv, hostEv;

private PlaceAutocompleteFragment autocompleteFragment;
private Place placePicked;


private StorageReference storageReference  = FirebaseStorage.getInstance().getReference();
private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
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
        pro_lName = (EditText)findViewById(R.id.last_nam);
        pro_mInit = (EditText)findViewById(R.id.middle_init);
        email_toggle = (Switch) findViewById(R.id.tog_email);
        host_toggle = (Switch) findViewById(R.id.tog_host_events);
        attend_toggle = (Switch) findViewById(R.id.tog_events);
        profile_picture = (ImageView) findViewById(R.id.profile_ebg);
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.addr_autocomplete_fragment);


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
        //Initialize fields that exist only for Profile Edit View
        pro_mInit = (EditText)findViewById(R.id.middle_init);
        pro_lName = (EditText)findViewById(R.id.last_nam);
        pro_age = (EditText)findViewById(R.id.age_year);
        email_toggle = (Switch) findViewById(R.id.tog_email);
        attend_toggle = (Switch) findViewById(R.id.tog_events);
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
        if(addrC){userDatabase.child(getUid()).child("address").setValue(placePicked.getAddress().toString()+" "+placePicked.getLatLng().toString());}
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
    events_host_layout = (LinearLayout) findViewById(R.id.for_host_events);
    npic = (ImageView) findViewById(R.id.profile_bg);


    //Idea: Retrieve information from DB to know what items should/shouldn't be displayed
    //Other Users who view this profile will only what Owner of the Profile chooses to show (in regards to email and events)
    userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = User.fromSnapshot(dataSnapshot);

            Glide.with(getApplicationContext()).load(user.getImg()).into(npic);

            //Set text fields
            profile_email.setText(user.getEmail());
            profile_bio.setText(user.getBio());
            profile_fName.setText(user.getName());

            //Populate the events (Owner of Profile always sees them!)
            //ToDo:
            String attendNames = user.parseEventAttendNames(user.getAttendEid());
            attendEv = attendNames.split(" ");
            String hostNames = user.parseEventHostNames(user.getHostEid());

            System.out.println("attendNAMES ==========================="+attendNames);
            System.out.println("HOST NAMES ==========================="+hostNames);
            hostEv = hostNames.split(" ");
            populateAttend();
            populateHost();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    });
}


private void populateAttend(){

    if(attendEv!=null) {
        for (int i = 0; i < attendEv.length - 1; ++i) {
            TextView eventsL = new TextView(this);
            eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            eventsL.setText(attendEv[i]);
            eventsL.setTextSize(20);
            eventsL.setTextColor(0xff424242);
            eventsL.setAllCaps(false);
            eventsL.setClickable(true);
            eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            eventsL.setVisibility(View.VISIBLE);
            eventsL.setId(i);
            events_attend_layout.addView(eventsL);
        }
    }
}

private void populateHost(){
    if(hostEv!=null) {
        for (int i = 0; i < hostEv.length - 1; ++i) {
            System.out.println("I GOT INSIDE THE HOST LOOP!!");
            System.out.println("====================TITLE" + hostEv[i]);

            TextView eventsL = new TextView(this);
            eventsL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            eventsL.setText(hostEv[i]);
            eventsL.setTextSize(20);
            eventsL.setTextColor(0xff424242);
            eventsL.setAllCaps(false);
            eventsL.setClickable(true);
            eventsL.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            eventsL.setVisibility(View.VISIBLE);
            eventsL.setId(5000 + i);
            events_host_layout.addView(eventsL);
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
