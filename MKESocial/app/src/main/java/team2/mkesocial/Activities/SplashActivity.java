package team2.mkesocial.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import Firebase.Settings;
import team2.mkesocial.R;

public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 1700;

    @Override
    public void onCreate(Bundle icicle) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null && Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
