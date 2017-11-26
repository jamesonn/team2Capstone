package team2.mkesocial.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import team2.mkesocial.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EventActivity extends Activity {

    public static final int IMAGE_GALLERY_REQUEST = 20;
    private ImageView profile_picture;
    private Bitmap image;
    private boolean change=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //ToDo: only show the edit button if the current profile belongs to the user
        //pseudocode: if user belongs to profile (verify against database). show edit_button. else hide
    }

    public void edit_btn_on_click(View v){
        //click on Edit Button
        if(v.getId() == R.id.edit_button) {
            //Set the xml layout to be the editable one: activity_edit_profile
            setContentView(R.layout.activity_edit_profile);
            wipChange();
        }
    }

    public void edit_pic_on_click(View v) {
        //click on Edit Photo button
        if (v.getId() == R.id.edit_photo) {
            //create intent to open image gallery
            Intent change_pic = new Intent(Intent.ACTION_PICK);
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            Uri data = Uri.parse(pictureDirectory.getPath());
            change_pic.setDataAndType(data, "image/*");
            //start intent to open up gallery where user can select a new picture
            startActivityForResult(change_pic, IMAGE_GALLERY_REQUEST);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if everything processed correctly ...
        if(resultCode == RESULT_OK){
            //if user selected image from gallery
            if(requestCode==IMAGE_GALLERY_REQUEST){
                //address of img on SD card
                Uri imgUri = data.getData();
                //stream to read image data from SD card
                InputStream inputStream;

                //get input stream based on URI of image
                try {
                    inputStream = getContentResolver().openInputStream(imgUri);
                    //if inputStream is good...
                    //get bitmap from string
                    image = BitmapFactory.decodeStream(inputStream);
                    //correcting orientation so it displays correctly
                    String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                    Cursor cur = getContentResolver().query(imgUri, orientationColumn, null, null, null);
                    int orientation = -1;
                    if (cur != null && cur.moveToFirst()) {
                        orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                    }
                    //fix orientation utilizing helper method
                    switch(orientation) {
                        case 90:
                            image = rotateImage(image, 90);
                            break;
                        case 180:
                            image = rotateImage(image, 180);
                            break;
                        case 270:
                            image = rotateImage(image, 270);
                            break;
                        default:
                            break;
                    }
                    setContentView(R.layout.activity_edit_profile);
                    profile_picture = (ImageView) findViewById(R.id.profile_bg);

                    //show image change
                    profile_picture.setImageBitmap(image);
                    change=true;
                    cur.close();
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                    //output message to let user know image is not available
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void save_btn_on_click(View v){
        //Todo: Changes will have to be saved and get stored on firebase so that the changes holds put
        if(v.getId() == R.id.save_button) {
            //Storing changes on firebase... picture, fname, lname, minit, bio, email, age, addr, events



            //Store data/changes that was inputted from user while editing
            //Information below this is from activity_edit_profile
            //save "new" name input
            EditText name_edit = (EditText)findViewById(R.id.first_name);
            String name_change = name_edit.getText().toString();

            //save "new" bio input
            EditText bio_edit = (EditText)findViewById(R.id.about_me_bio);
            String bio_change = bio_edit.getText().toString();

            //save "new" email input
            EditText email_edit = (EditText)findViewById(R.id.email_addr);
            String email_change = email_edit.getText().toString();


            //==========================================================================================
            //Data in-between here can get updated BUT will never be shown publicly for safety reasons
            //middle name changes
            EditText mid_edit = (EditText)findViewById(R.id.middle_init);
            String mid_change = mid_edit.getText().toString();

            //last name changes
            EditText last_edit = (EditText)findViewById(R.id.last_nam);
            String last_change = last_edit.getText().toString();

            //age changes
            EditText age_edit = (EditText)findViewById(R.id.age_year);
            String age_change = age_edit.getText().toString();

            //home address
            EditText home_edit = (EditText)findViewById(R.id.address);
            String home_change = home_edit.getText().toString();


            //===========================================================================================

            //Check the toggle bars
            //Email toggle
            Switch email_toggle = (Switch) findViewById(R.id.tog_email);
            boolean view_email = email_toggle.isChecked();

            //Events toggle
            Switch events_toggle = (Switch) findViewById(R.id.tog_events);
            boolean view_events = events_toggle.isChecked();

            //Information below this is from activity_profile
            setContentView(R.layout.activity_profile);

            //update profile picture

            ImageView pic = (ImageView)findViewById(R.id.profile_bg);

            if(!change) {
                BitmapDrawable drawable = (BitmapDrawable) pic.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                pic.setImageBitmap(bitmap);
            }
            else{
                pic.setImageBitmap(image);
            }

            //update name
            TextView name = (TextView)findViewById(R.id.first_name);
            name.setText(name_change);

            //update bio
            TextView bio = (TextView)findViewById(R.id.about_me_bio);
            bio.setText(bio_change);

            //update email
            TextView email = (TextView)findViewById(R.id.email_addr);
            email.setText(email_change);

            //If toggle is off; must turn off item
            //else must be able see item
            //Email toggle
            TextView email_header = (TextView)findViewById(R.id.contact_info);

            if(!view_email){
                email.setVisibility(View.GONE);
                email_header.setVisibility(View.GONE);
            }
            else{
                email.setVisibility(View.VISIBLE);
                email_header.setVisibility(View.VISIBLE);
            }

            //Event toggle
            //ToDo: Find a good way to count and use a for loop to hide all shared events

            TextView event_header = (TextView)findViewById(R.id.shared_events);

            if(!view_events){
                event_header.setVisibility(View.GONE);

            }
            else{
                event_header.setVisibility(View.VISIBLE);

            }
        }
    }

    //Helper method ... temporarily store changes
    private void wipChange(){
        ////////////////////////////////////////////////////////////////////////////////////////
        //save wip changes
        // setContentView(R.layout.activity_edit_profile);
        ImageView pic = (ImageView)findViewById(R.id.profile_bg);
        BitmapDrawable drawable = (BitmapDrawable) pic.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        pic.setImageBitmap(bitmap);

        //save "new" name input
        EditText name_edit = (EditText)findViewById(R.id.first_name);
        String name_change = name_edit.getText().toString();
        System.out.println(name_change);
        name_edit.setText(name_change);

        //save "new" bio input
        EditText bio_edit = (EditText)findViewById(R.id.about_me_bio);
        String bio_change = bio_edit.getText().toString();
        bio_edit.setText(bio_change);

        //save "new" email input
        EditText email_edit = (EditText)findViewById(R.id.email_addr);
        String email_change = email_edit.getText().toString();
        email_edit.setText(email_change);

        //==========================================================================================
        //Data in-between here can get updated BUT will never be shown publicly for safety reasons
        //middle name changes
        EditText mid_edit = (EditText)findViewById(R.id.middle_init);
        String mid_change = mid_edit.getText().toString();
        mid_edit.setText(mid_change);

        //last name changes
        EditText last_edit = (EditText)findViewById(R.id.last_nam);
        String last_change = last_edit.getText().toString();
        last_edit.setText(last_change);

        //age changes
        EditText age_edit = (EditText)findViewById(R.id.age_year);
        String age_change = age_edit.getText().toString();
        age_edit.setText(age_change);

        //home address
        EditText home_edit = (EditText)findViewById(R.id.address);
        String home_change = home_edit.getText().toString();
        home_edit.setText(home_change);
        //===========================================================================================

        //Check the toggle bars
        //Email toggle
        Switch email_toggle = (Switch) findViewById(R.id.tog_email);
        boolean view_email = email_toggle.isChecked();
        email_toggle.setChecked(view_email);

        //Events toggle
        Switch events_toggle = (Switch) findViewById(R.id.tog_events);
        boolean view_events = events_toggle.isChecked();
        events_toggle.setChecked(view_events);


        ////////////////////////////////////////////////////////////////////////////////////////
    }

    //helper method ...  fix rotation issues
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
