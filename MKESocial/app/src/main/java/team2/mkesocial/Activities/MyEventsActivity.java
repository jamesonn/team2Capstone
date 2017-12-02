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

    private ArrayList<CalendarDay> _markedDays = new ArrayList<>();
    private ArrayList<CalendarDay> _busyDays = new ArrayList<>();
    private ArrayList<Event> _events = new ArrayList<>();
    private HashMap<CalendarDay, HashSet<Event>> _eventMap = new HashMap<>();
    private HashMap<CalendarDay, HashSet<BusyTime>> _busyMap = new HashMap<>();
    private EventAdapter _eventAdapter;
    private FirebaseDatabase _database;
    private Query _dataRef;
    private Query _busyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        _calendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        _eventList = (ListView)findViewById(R.id.eventList);
        _busyTimeButton = (Button)findViewById(R.id.busyTimeButton);

        _database = FirebaseDatabase.getInstance();
        _dataRef = _database.getReference(Event.DB_USERS_NODE_NAME).child(getUid()).child("attendEid");
        _dataRef.addValueEventListener(this);

        _busyRef = _database.getReference(User.DB_USERS_NODE_NAME).child(getUid()).child("busyTimes");
        _busyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                _busyDays.clear();
                _busyMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BusyTime time = snapshot.getValue(BusyTime.class);
                    GregorianCalendar startDate = new GregorianCalendar();
                    GregorianCalendar endDate = new GregorianCalendar();

                    startDate.setTime(time.getStartTimeAsDate());
                    endDate.setTime(time.getEndTimeAsDate());

                    CalendarDay day = CalendarDay.from(startDate.getTime());
                    _busyDays.add(day);
                    if (_busyMap.containsKey(day)) {
                        _busyMap.get(day).add(time);
                    } else {
                        HashSet<BusyTime> busySet = new HashSet<>();
                        busySet.add(time);
                        _busyMap.put(day, busySet);
                    }

                    while (endDate.get(Calendar.DAY_OF_YEAR) != startDate.get(Calendar.DAY_OF_YEAR)) {
                        startDate.add(Calendar.DAY_OF_YEAR, 1);
                        day = CalendarDay.from(startDate.getTime());
                        _busyDays.add(day);
                        if (_busyMap.containsKey(day)) {
                            _busyMap.get(day).add(time);
                        } else {
                            HashSet<BusyTime> busySet = new HashSet<>();
                            busySet.add(time);
                            _busyMap.put(day, busySet);
                        }
                    }

                    redrawDecorators();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        _calendarView.addDecorators(new WeekendDecorator(),
                                    new EventDecorator(Color.RED, _markedDays));

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

        HashSet<BusyTime> busySet = _busyMap.get(date);
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

        Collections.sort(_events, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {
                return event1.getStartTime().compareTo(event2.getStartTime());
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

            String[] eventIds = dataSnapshot.getValue().toString().split(" ");
            _markedDays.clear();
            _eventMap.clear();
            for (String eid : eventIds){
                Query q = _database.getReference(Event.DB_EVENTS_NODE_NAME).child(eid).orderByKey();
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            Event event = Event.fromSnapshot(dataSnapshot);

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

    // TODO: This could be more efficient if we didn't keep recreating the decorators
    private void redrawDecorators() {
        _calendarView.removeDecorators();

        _calendarView.addDecorator(new WeekendDecorator());
        _calendarView.addDecorator(new EventDecorator(Color.GREEN, _markedDays));
        _calendarView.addDecorator(new EventDecorator(Color.RED, _busyDays));
    }
}
