package team2.mkesocial.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import team2.mkesocial.R;



public class SettingsActivity extends BaseActivity {

    private Switch location, notifications, privateProfile;
    private TextView invite, rate;
    //publically visible location enabled flag, best coding practices
    public boolean Location_Enabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Init Objects
        location = (Switch) findViewById(R.id.switch_location);
        notifications = (Switch) findViewById(R.id.switch_notifications);
        privateProfile = (Switch) findViewById(R.id.switch_private_profile);
        invite = (TextView) findViewById(R.id.textView_invite);
        rate = (TextView) findViewById(R.id.textView_rate);


        // Set the switch listeners
        location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

            }
        });
        notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
            }
        });
        privateProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
            }
        });

        // Set the text view listeners
        invite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            }
        });
        rate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            }
        });


    }
}
