package team2.mkesocial.Activities;

/**
 * Created by cfoxj2 on 11/1/2017.
 */

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

//TODO
//import com.google.firebase.auth.FirebaseAuth;


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

    public String getUid() {
        //TODO
       //return FirebaseAuth.getInstance().getCurrentUser().getUid();
        //jrfox@uwm.edu UID
        return "Xf8K7LZcJvZiMPqAnqFCWM1wbDl2";
    }


}