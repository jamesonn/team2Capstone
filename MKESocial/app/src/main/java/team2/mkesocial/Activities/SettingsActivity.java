package team2.mkesocial.Activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import Firebase.Databasable;
import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.R;



public class SettingsActivity extends BaseActivity {

    private Switch location, notifications, privateProfile, darkTheme;
    private TextView invite, rate;

    //publically visible location enabled flag, best coding practices
    //private static boolean location_enabled = true;
    //public boolean getLocationEnabled(){return location_enabled;}
    //public void setLocationEnabled(boolean locationEn){location_enabled = locationEn;}
    private boolean onPageLoad = true;

    //DB user setting ref
    final private static DatabaseReference settingsDBReference = FirebaseDatabase.getInstance()
            .getReference(Databasable.DB_USERS_NODE_NAME).child(getUid());

    private static final String TAG = SettingsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Init Objects
        //location = (Switch) findViewById(R.id.switch_location);
        notifications = (Switch) findViewById(R.id.switch_notifications);
        privateProfile = (Switch) findViewById(R.id.switch_private_profile);
        darkTheme = (Switch) findViewById(R.id.switch_theme);
        invite = (TextView) findViewById(R.id.textView_invite);
        rate = (TextView) findViewById(R.id.textView_rate);

        // Grab user settings to display
        //Location

        //Notifications
        final BiConsumer<Settings, Switch> readSettingNotifications= (settingsObj, notifications)
                -> notifications.setChecked(Boolean.parseBoolean(settingsObj.getNotificationEnabled()));
        final Consumer<Settings> readNotifSet = settings ->
                readSettingNotifications.accept(settings, notifications);
        try {
            Settings.runMethodOnDBSettingsObj(readNotifSet, false);
        }
        catch(Exception e)
        {
            Toast.makeText(SettingsActivity.this, "Error Fetching DB info",
                    Toast.LENGTH_SHORT).show();
        }
        //Private Profile
        final BiConsumer<Settings, Switch> readSettingPrivateProfile = (settingsObj, notifications)
                -> privateProfile.setChecked(Boolean.parseBoolean(settingsObj.getPrivateProfile()));
        final Consumer<Settings> readPriv = settings ->
                readSettingPrivateProfile.accept(settings, notifications);
        try {
            Settings.runMethodOnDBSettingsObj(readPriv, false);
        }
        catch(Exception e)
        {
            Toast.makeText(SettingsActivity.this, "Error Fetching DB info",
                    Toast.LENGTH_SHORT).show();
        }

        //Theme
        final BiConsumer<Settings, Switch> readSettingTheme = (settingsObj, notifications)
                -> darkTheme.setChecked(Boolean.parseBoolean(settingsObj.getTheme()));
        final Consumer<Settings> readTheme = settings ->
                readSettingTheme.accept(settings, notifications);
        try {
            Settings.runMethodOnDBSettingsObj(readTheme, false);
        }
        catch(Exception e)
        {
            Toast.makeText(SettingsActivity.this, "Error Fetching DB info",
                    Toast.LENGTH_SHORT).show();
        }

        // Set the switch listeners
        /*location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set the location enabled field
                setLocationEnabled(isChecked);

            }
        });*/


        // Notification Settings enable listener
        notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                final String checkedState = String.valueOf(isChecked);
                //lambda to update
                final BiConsumer<Settings, String> updateSettingNotifications= (settingsObj, notification_state)
                        -> settingsObj.setNotificationEnabled(notification_state);

                //wrap it in a consumer lambda to give to Settings Class
                final Consumer<Settings> updateSet = settings ->
                        updateSettingNotifications.accept(settings, checkedState);

                try {
                    Settings.runMethodOnDBSettingsObj(updateSet, true);
                }
                catch(Exception e)
                {
                    Toast.makeText(SettingsActivity.this, "Error Saving to DB",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });


        privateProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final String checkedState = String.valueOf(isChecked);
                //lambda to update
                final BiConsumer<Settings, String> updatePrivateProfileNotifications= (settingsObj, notification_state)
                        -> settingsObj.setPrivateProfile(notification_state);

                //wrap it in a consumer lambda to give to Settings Class
                final Consumer<Settings> updateSet = settings ->
                        updatePrivateProfileNotifications.accept(settings, checkedState);

                try {
                    Settings.runMethodOnDBSettingsObj(updateSet, true);
                }
                catch(Exception e)
                {
                    Toast.makeText(SettingsActivity.this, "Error Saving to DB",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        darkTheme.setOnClickListener(new CompoundButton.OnClickListener(){
            public void onClick(View v) {
                final String checkedState = String.valueOf(darkTheme.isChecked());
                //lambda to update
                final BiConsumer<Settings, String> updatePrivateProfileNotifications= (settingsObj, theme_state)
                        -> settingsObj.setTheme(theme_state);

                //wrap it in a consumer lambda to give to Settings Class
                final Consumer<Settings> updateSet = settings ->
                        updatePrivateProfileNotifications.accept(settings, checkedState);

                try {
                    Settings.runMethodOnDBSettingsObj(updateSet, true);
                }
                catch(Exception e)
                {
                    Toast.makeText(SettingsActivity.this, "Error Saving to DB",
                            Toast.LENGTH_SHORT).show();
                }
                getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
                finish();
                startActivity(getIntent());

               /** if(getIntent().getExtras() == null
                        || !getIntent().getExtras().getBoolean("pageRefreshed"))
                    refreshSettings();
                else
                    getIntent().putExtra("pageRefreshed", false);
*/
            }

        });

        // Set the text view listeners
        invite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String strShareMessage = "\nLet me recommend you this application\n\n";
                    strShareMessage = strShareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
                    i.putExtra(Intent.EXTRA_TEXT, strShareMessage);
                    startActivity(Intent.createChooser(i, "Share via"));
                } catch(Exception e) {
                    Toast.makeText(SettingsActivity.this, "Error Sharing",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        rate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });


    }
    public void refreshSettings()
    {
        //refresh page
        Intent refreshPage = new Intent(this, SettingsActivity.class);
        refreshPage.putExtra("pageRefreshed", true);
        finish();
        startActivity(refreshPage);
    }

    @Override
    public void onBackPressed() {
        Intent gotoFeed = new Intent(this, FeedActivity.class);
        finish();
        startActivity(gotoFeed);

    }



}
