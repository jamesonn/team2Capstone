package team2.mkesocial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //the following lines are simply for me to test navigating from "Hello World!" screen to my assigned pages
    public void map_btn_on_click(View v){
        if(v.getId() == R.id.button1) {
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        }
    }

    public void profile_btn_on_click(View v){
        if(v.getId() == R.id.button2) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
    }

}