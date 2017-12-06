package team2.mkesocial.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import Firebase.Event;
import Firebase.Settings;
import Firebase.Tag;
import Firebase.User;
import team2.mkesocial.Constants;
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

    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    // Firestore Image DB Ref
    private StorageReference storageReference  = FirebaseStorage.getInstance().getReference();


    private PlaceAutocompleteFragment autocompleteFragment;
    private Place placePicked;

    private Uri filePath;

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
                        Calendar startTime = Calendar.getInstance();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if an image was selected
        if(data != null){
           filePath = data.getData();
            if (resultCode == RESULT_OK) {
                Uri selectedMediaUri = data.getData();
                if (selectedMediaUri.toString().contains("image")) {
                    try {//Update the display values
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                        //For correcting orientation so it displays correctly (on images that where taken sideways/upside-down)
                        ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(filePath));
                        //get current rotation...
                        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        //convert to degrees
                        int rotationInDegrees = exifToDegrees(rotation);
                        Matrix matrix = new Matrix();
                        if (rotation != 0f) {
                            matrix.preRotate(rotationInDegrees);
                        }

                        // Screen height
                        DisplayMetrics display = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(display);
                        int screenWidth = display.widthPixels;
                        int screenHeight = display.heightPixels;

                        Bitmap adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        Bitmap scaledBitmap = resize(adjustedBitmap, bitmap.getWidth(), screenHeight / 3);
                        //resize the imageView displaying image
                        android.view.ViewGroup.LayoutParams layoutParams = eventImage.getLayoutParams();
                        layoutParams.width = eventImage.getWidth();
                        layoutParams.height = scaledBitmap.getHeight();
                        eventImage.setLayoutParams(layoutParams);

                        eventImage.setImageBitmap(scaledBitmap);


                    } catch (Exception e) {
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Incorrect Image format selected", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    //EVENT IMAGE PART
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
    private void uploadFile(String eventId) {
        //checking if file is available
        if (filePath != null) {
            //displaying progress dialog while image is uploading
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Updating");
//            progressDialog.show();

            //getting the storage reference
            StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(filePath));

            //adding the file to reference
            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //dismissing the progress dialog
//                            progressDialog.dismiss();
                            //displaying success toast
                            eventDatabase.child(eventId).child("image").setValue(taskSnapshot.getDownloadUrl().toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
//                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //displaying the upload progress
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                            progressDialog.setMessage("Updated " + ((int) progress) + "%...");
                        }
                    });

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
        }//TODO on location deleted check
        if (placePicked == null || placePicked.getAddress().toString().isEmpty()) {
            autocompleteFragment.setHint("Please add a location before creating event");
            return false;

        } else
            location = placePicked.getAddress().toString()+":"+placePicked.getLatLng();
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

        if(filePath != null)
            uploadFile(eventId);


        //2) Store event info in DB under it's Event ID
        // Get user ID to tie event to
        final String userId = getUid();
        Event newEvent = new Event(title, description, startDate, endDate, startTime, endTime, location,
                userId, suggestedAge, "", cost, tags, eventId);
        // Add event obj to database under its event ID
        eventDatabase.child(eventId).setValue(newEvent.toMap());

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
    //fix rotation issues
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }


}
