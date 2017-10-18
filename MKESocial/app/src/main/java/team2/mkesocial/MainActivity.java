package team2.mkesocial;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


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

    public void create_event_btn_on_click(View v)
    {
        if(v.getId() == R.id.create_event_button) {
            startActivity(new Intent(MainActivity.this, CreateEventActivity.class));
        }
    }
    public void settings_btn_on_click(View v)
    {
        if(v.getId() == R.id.setting_button) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
    }
    public void about_btn_on_click(View v)
    {
        if(v.getId() == R.id.about_image_button) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }
    }

    public void searchBtnOnClick (View view) {
        startActivity(new Intent(this, SearchActivity.class));
    }

}