package team2.mkesocial.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import team2.mkesocial.R;

public class NotFoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_found);
    }
    @Override
    public void onBackPressed() {
        //go back to the 'home' page
        Intent feedPage = new Intent(this, FeedActivity.class);
        finish();
        startActivity(feedPage);
    }
}
