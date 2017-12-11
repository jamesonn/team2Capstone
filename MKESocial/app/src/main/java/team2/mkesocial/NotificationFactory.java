package team2.mkesocial;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import Firebase.Event;
import team2.mkesocial.Activities.FeedActivity;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;

/**
 * Created by Nate on 12/10/2017.
 */

public class NotificationFactory extends IntentService implements ValueEventListener {

    private static final int NOTIFY_ID = 321;
    private String MY_USERID;

    public NotificationFactory() {
        super("NotificationFactory");
    }

    private void buildAttendeeNumberNotification(String message){

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if(intent.getParcelableExtra("ATTENDEE_NUMS") != null){
            MY_USERID = intent.getParcelableExtra("USER_ID");
            FirebaseDatabase df = FirebaseDatabase.getInstance();
            df.getReference(Event.DB_EVENTS_NODE_NAME).orderByChild("title");
            Query _dataQuery = df.getReference(Event.DB_EVENTS_NODE_NAME).orderByChild("title");
            _dataQuery.addValueEventListener(this);
        }
        //Notification.Builder builder = new Notification.Builder(this);
        //builder.setContentTitle("My Title");
        //builder.setContentText("This is the Body");
        Intent notifyIntent = new Intent(this, FeedActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //builder.setContentIntent(pendingIntent);
        //Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        //managerCompat.notify(NOTIFY_ID, notificationCompat);
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        StringBuilder sb = new StringBuilder();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Event event = Event.fromSnapshot(snapshot);
            if (event.getHostUid().equals(MY_USERID)){
                String unparsedAttendees = event.getAttendees();
                String[] numberOfAttendees = unparsedAttendees.split("'");
                int intNumOfAttendees = numberOfAttendees.length;
                sb.append(event.getTitle() + "has" + intNumOfAttendees + "\\n");
            }
        }
        buildAttendeeNumberNotification(sb.toString());
    }
}