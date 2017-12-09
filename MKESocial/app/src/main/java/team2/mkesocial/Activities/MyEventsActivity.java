package team2.mkesocial.Activities;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Firebase.BusyTime;
import Firebase.Event;
import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.Adapters.EventAdapter;
import team2.mkesocial.Fragments.BusyTimeFragment;
import team2.mkesocial.EventDecorator;
import team2.mkesocial.R;
import team2.mkesocial.WeekendDecorator;

public class MyEventsActivity extends BaseActivity
        implements OnDateSelectedListener, ValueEventListener, BusyTimeFragment.BusyTimeListener {

    private final String TAG = "My Events";

    private MaterialCalendarView _calendarView;
    private ListView _eventList;
    private Button _busyTimeButton;

    private ArrayList<Event> _events = new ArrayList<>();
    private HashMap<CalendarDay, HashSet<Event>> _attendDays = new HashMap<>();
    private HashMap<CalendarDay, HashSet<Event>> _maybeDays = new HashMap<>();
    private HashMap<CalendarDay, HashSet<Event>> _hostDays = new HashMap<>();
    private HashMap<CalendarDay, HashSet<BusyTime>> _busyDays = new HashMap<>();
    private EventAdapter _eventAdapter;
    private FirebaseDatabase _database;
    private Query _dataRef;
    private Query _busyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        _calendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        _eventList = (ListView)findViewById(R.id.eventList);
        _busyTimeButton = (Button)findViewById(R.id.busyTimeButton);

        _database = FirebaseDatabase.getInstance();
        _dataRef = _database.getReference(Event.DB_USERS_NODE_NAME).child(getUid()).child("attendEid");
        _dataRef.addValueEventListener(this);

        _dataRef = _database.getReference(Event.DB_USERS_NODE_NAME).child(getUid()).child("hostEid");
        _dataRef.addValueEventListener(this);

        _dataRef = _database.getReference(Event.DB_USERS_NODE_NAME).child(getUid()).child("maybeEid");
        _dataRef.addValueEventListener(this);

        _busyRef = _database.getReference(User.DB_USERS_NODE_NAME).child(getUid()).child("busyTimes");
        _busyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                _busyDays.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BusyTime time = snapshot.getValue(BusyTime.class);
                    GregorianCalendar startDate = new GregorianCalendar();
                    GregorianCalendar endDate = new GregorianCalendar();

                    startDate.setTime(time.getStartTimeAsDate());
                    endDate.setTime(time.getEndTimeAsDate());

                    CalendarDay day = CalendarDay.from(startDate.getTime());
                    addToDayMap(day, _busyDays, time);

                    while (endDate.get(Calendar.DAY_OF_YEAR) != startDate.get(Calendar.DAY_OF_YEAR)) {
                        startDate.add(Calendar.DAY_OF_YEAR, 1);
                        day = CalendarDay.from(startDate.getTime());
                        addToDayMap(day, _busyDays, time);
                    }
                }
                redrawDecorators();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        _calendarView.setOnDateChangedListener(this);

        _eventAdapter = new EventAdapter(this, _events);
        _eventList.setAdapter(_eventAdapter);

        _eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event)_eventList.getItemAtPosition(position);
                inspectEvent(selectedEvent.getEventId());
            }
        });

        _busyTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusyTimeFragment.create(MyEventsActivity.this, getUid()).show(getSupportFragmentManager(), TAG);
            }
        });

        redrawDecorators();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date,
                               boolean selected) {
        Log.d(TAG, "Selected date:" + date.toString());

        _calendarView.invalidateDecorators();

        _events.clear();

        HashSet<Event> eventSet = _attendDays.get(date);
        if (eventSet != null)
            _events.addAll(eventSet);

        eventSet = _maybeDays.get(date);
        if (eventSet != null)
            _events.addAll(eventSet);

        eventSet = _hostDays.get(date);
        if (eventSet != null) {
            for (Event e : eventSet) {
                if (!_events.contains(e))
                    _events.add(e);
            }
        }

        HashSet<BusyTime> busySet = _busyDays.get(date);
        if (busySet != null) {
            for (BusyTime time : busySet) {
                Event busyEvent = new Event();
                GregorianCalendar start = new GregorianCalendar();
                GregorianCalendar end = new GregorianCalendar();
                start.setTime(time.getStartTimeAsDate());
                end.setTime(time.getEndTimeAsDate());

                // Adjust times for overlapping days
                if (date.getDate().compareTo(cloneDateNoTime(start).getTime()) > 0) {
                    start.set(Calendar.HOUR, start.getActualMinimum(Calendar.HOUR));
                    start.set(Calendar.HOUR_OF_DAY, start.getActualMinimum(Calendar.HOUR_OF_DAY));
                    start.set(Calendar.MINUTE, start.getActualMinimum(Calendar.MINUTE));
                }
                if (date.getDate().compareTo(cloneDateNoTime(end).getTime()) < 0) {
                    end.set(Calendar.HOUR, end.getActualMaximum(Calendar.HOUR));
                    end.set(Calendar.HOUR_OF_DAY, end.getActualMaximum(Calendar.HOUR_OF_DAY));
                    end.set(Calendar.MINUTE, end.getActualMaximum(Calendar.MINUTE));
                }

                busyEvent.setTitle("Busy");
                busyEvent.setStartTime(start);
                busyEvent.setEndTime(end);

                _events.add(busyEvent);
            }
        }

        // Sort events by start time
        Collections.sort(_events, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                event1.getStartTime().compareTo(event2.getStartTime());
            }
        });

        _eventAdapter.notifyDataSetChanged();
    }

    // Clone calendar object with no time data set
    private static Calendar cloneDateNoTime(Calendar date) {
        Calendar newDate = (Calendar)date.clone();
        newDate.clear(Calendar.HOUR);
        newDate.clear(Calendar.HOUR_OF_DAY);
        newDate.clear(Calendar.MINUTE);
        newDate.clear(Calendar.SECOND);
        newDate.clear(Calendar.MILLISECOND);
        newDate.clear(Calendar.DST_OFFSET);
        newDate.clear(Calendar.ZONE_OFFSET);
        newDate.clear(Calendar.AM_PM);
        return newDate;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        try {
            Log.d(TAG, dataSnapshot.toString());

            final HashMap<CalendarDay, HashSet<Event>> map;

            switch (dataSnapshot.getKey()) {
                case "attendEid":
                    map = _attendDays;
                    break;
                case "maybeEid":
                    map = _maybeDays;
                    break;
                case "hostEid":
                    map = _hostDays;
                    break;
                default:
                    Log.wtf(TAG, "This shouldn't happen");
                    return;
            }

            String[] events = dataSnapshot.getValue().toString().split("`");
            map.clear();

            for (int i = 0; i < events.length; i++) {
                // Skip odd indices because even ones have the event id that we want
                if (i % 2 == 1)
                    continue;

                String eid = events[i];
                Query q = _database.getReference(Event.DB_EVENTS_NODE_NAME).child(eid).orderByKey();
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            Event event = Event.fromSnapshot(dataSnapshot);

                            if (event != null) {
                                Log.d(TAG, event.getTitle());

                                CalendarDay day = CalendarDay.from(event.getStartDate().getTime());
                                addToDayMap(day, map, event);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Exception:" + e.toString());
                            e.printStackTrace();
                        }

                        redrawDecorators();
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

    @Override
    public void OnBusyTimePositive(List<BusyTime> busyTimes) {
        Log.d(TAG, busyTimes.toString());
    }

    private <T> void addToDayMap(CalendarDay day, HashMap<CalendarDay, HashSet<T>> map, T obj) {
        if (map.containsKey(day)) {
            map.get(day).add(obj);
        } else {
            HashSet<T> set = new HashSet<>();
            set.add(obj);
            map.put(day, set);
        }
    }

    private void redrawDecorators() {
        EventDecorator attendDecorator = new EventDecorator(Color.GREEN, _attendDays.keySet());
        EventDecorator hostDecorator = new EventDecorator(Color.MAGENTA, _hostDays.keySet());
        EventDecorator maybeDecorator = new EventDecorator(Color.BLUE, _maybeDays.keySet());
        EventDecorator busyDecorator = new EventDecorator(Color.RED, _busyDays.keySet());

        _calendarView.removeDecorators();
        _calendarView.addDecorators(new WeekendDecorator(), busyDecorator, maybeDecorator,
                hostDecorator, attendDecorator);
    }
}
