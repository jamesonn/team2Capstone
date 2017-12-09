package team2.mkesocial.Activities;

/**
 * Created by cfoxj2 on 11/1/2017.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

//TODO
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.R;


public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static String getUid() {
       return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    protected void inspectEvent(String eid) {
        if (eid != null) {
            Intent goToEventPage = new Intent(this, EventActivity.class);
            goToEventPage.putExtra("EVENT_ID", eid);
            startActivity(goToEventPage);
        }
        else {
            //ToDo: Handle case where event no longer exist
            //aka need to remove the event from the user
        }
    }

    protected void inspectUser(String uid) {
        if (uid != null) {
            Intent goToProfilePage = new Intent(this, ProfileActivity.class);
            goToProfilePage.putExtra("USER_ID", uid);
            startActivity(goToProfilePage);
        }
        else{
            //ToDO: Handle case where user no longer exist
            //aka remove user from the event
        }
    }


}