package team2.mkesocial;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import team2.mkesocial.Activities.FeedActivity;

/**
 * Created by Nate on 12/10/2017.
 */

public class NotificationFactory extends IntentService{

    private static final int NOTIFY_ID = 321;

    public NotificationFactory() {
        super("NotificationFactory");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("My Title");
        builder.setContentText("This is the Body");
        Intent notifyIntent = new Intent(this, FeedActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIFY_ID, notificationCompat);
    }
}