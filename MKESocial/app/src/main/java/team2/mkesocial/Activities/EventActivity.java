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

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import team2.mkesocial.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EventActivity extends Activity {

    public static final int IMAGE_GALLERY_REQUEST = 20;
    private ImageView profile_picture;
    private Bitmap image;
    private boolean change=false;
    private FirebaseDatabase _database;
    private Query _dataQuery;
    private String _eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        _database = FirebaseDatabase.getInstance();

        _eventTitle = getIntent().getStringExtra("EVENT_TITLE");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
