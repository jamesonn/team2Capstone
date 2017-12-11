package team2.mkesocial;

import android.app.Application;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import team2.mkesocial.Activities.FeedActivity;
import team2.mkesocial.Activities.LoginActivity;

/**
 * Created by cfoxj2 on 12/10/2017.
 */

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Custom Exception setup
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(true) //default: true
                .trackActivities(false) //default: false
                .minTimeBetweenCrashesMs(3000) //default: 3000
                .errorDrawable(R.drawable.error_page) //set to custom one
                .restartActivity(LoginActivity.class) //default: null (your app's launch activity)
                .errorActivity(FeedActivity.class) //default: null (default error activity)
                .eventListener(null) //default: null
                .apply();
    }
}

