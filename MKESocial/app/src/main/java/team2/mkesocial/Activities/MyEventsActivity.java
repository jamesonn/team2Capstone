package team2.mkesocial.Activities;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import Firebase.Event;
import team2.mkesocial.EventAdapter;
import team2.mkesocial.EventDecorator;
import team2.mkesocial.R;
import team2.mkesocial.WeekendDecorator;

public class MyEventsActivity extends BaseActivity implements OnDateSelectedListener, ValueEventListener {

    private final String TAG = "My Events";

    private MaterialCalendarView _calendarView;
    private ListView _eventList;
    private ArrayList<CalendarDay> _markedDays = new ArrayList<>();
    private ArrayList<Event> _events = new ArrayList<>();
    private HashMap<CalendarDay, HashSet<Event>> _eventMap = new HashMap<>();
    private EventAdapter _eventAdapter;
    private FirebaseDatabase _database;
    private Query _dataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        _calendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        _eventList = (ListView)findViewById(R.id.eventList);

        _database = FirebaseDatabase.getInstance();
        _dataRef = _database.getReference(Event.DB_USERS_NODE_NAME).child(getUid()).child("attendEid");
        _dataRef.addValueEventListener(this);

        _calendarView.addDecorators(new WeekendDecorator(),
                                    new EventDecorator(Color.RED, _markedDays));

        _calendarView.setOnDateChangedListener(this);

        _eventAdapter = new EventAdapter(this, _events);
        _eventList.setAdapter(_eventAdapter);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date,
                               boolean selected) {
        Log.d(TAG, "Selected date:" + date.toString());

        _calendarView.invalidateDecorators();

        _events.clear();

        HashSet eventSet = _eventMap.get(date);
        if (eventSet != null)
            _events.addAll(eventSet);

        Collections.sort(_events, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return event2.getStartTime().compareTo(event1.getStartTime());
            }
        });

        _eventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        try {
            Log.d(TAG, dataSnapshot.toString());

            String[] eventIds = dataSnapshot.getValue().toString().split(" ");
            _markedDays.clear();
            _calendarView.removeDecorators();
            _calendarView.addDecorator(new WeekendDecorator());
            _eventMap.clear();
            for (String eid : eventIds){
                Query q = _database.getReference(Event.DB_EVENTS_NODE_NAME).child(eid).orderByKey();
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            Event event = dataSnapshot.getValue(Event.class);

                            if (event != null) {
                                Log.d(TAG, event.getTitle());

                                CalendarDay day = CalendarDay.from(event.getDate().getTime());
                                _markedDays.add(day);
                                if (_eventMap.containsKey(day)) {
                                    _eventMap.get(day).add(event);
                                } else {
                                    HashSet<Event> eventSet = new HashSet<>();
                                    eventSet.add(event);
                                    _eventMap.put(day, eventSet);
                                }

                                _calendarView.addDecorator(new EventDecorator(Color.RED, _markedDays));
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Exception:" + e.toString());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception:" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
