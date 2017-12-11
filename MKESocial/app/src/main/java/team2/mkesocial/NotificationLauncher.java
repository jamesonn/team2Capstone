package team2.mkesocial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Nate on 12/10/2017.
 */

public class NotificationLauncher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notify = new Intent(context, NotificationFactory.class);
        context.startService(notify);
    }
}