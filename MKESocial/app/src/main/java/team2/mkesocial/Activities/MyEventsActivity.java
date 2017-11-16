package team2.mkesocial.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.widget.ListView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import Firebase.Event;
import team2.mkesocial.EventAdapter;
import team2.mkesocial.EventDecorator;
import team2.mkesocial.R;
import team2.mkesocial.WeekendDecorator;

public class MyEventsActivity extends AppCompatActivity implements OnDateSelectedListener {

    private MaterialCalendarView _calendarView;
    private ListView _eventList;
    private ArrayList<CalendarDay> _markedDays = new ArrayList<>();
    private ArrayList<Event> _events = new ArrayList<>();
    private EventAdapter _eventAdapter;

    // Dummy events
    private Event _event1 = new Event();
    private Event _event2 = new Event();
    private Event _event3 = new Event();
    private Event _event4 = new Event();

    public MyEventsActivity(){
        // Dummy days for events in November for demonstration purposes
        _markedDays.add(CalendarDay.from(2017,10,6));
        _markedDays.add(CalendarDay.from(2017, 10, 8));
        _markedDays.add(CalendarDay.from(2017, 10, 11));
        _markedDays.add(CalendarDay.from(2017, 10, 14));

        // Dummy data for dummy events
        _event1.setTitle("Concert");
        _event1.setStartTime(new GregorianCalendar(2017, 10, 6, 19, 0));
        _event1.setEndTime(new GregorianCalendar(2017, 10, 6, 21, 0));

        _event2.setTitle("Trivia Night");
        _event2.setStartTime(new GregorianCalendar(2017, 10, 8, 20, 0));
        _event2.setEndTime(new GregorianCalendar(2017, 10, 8, 22, 0));

        _event3.setTitle("Movie Marathon");
        _event3.setStartTime(new GregorianCalendar(2017, 10, 11, 17, 0));
        _event3.setEndTime(new GregorianCalendar(2017, 10, 11, 23, 0));

        _event4.setTitle("Charity Dinner");
        _event4.setStartTime(new GregorianCalendar(2017, 10, 14, 18, 0));
        _event4.setEndTime(new GregorianCalendar(2017, 10, 14, 20, 0));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        _calendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        _eventList = (ListView)findViewById(R.id.eventList);

        _calendarView.addDecorators(new WeekendDecorator(),
                                    new EventDecorator(Color.RED, _markedDays));

        _calendarView.setOnDateChangedListener(this);

        _eventAdapter = new EventAdapter(this, _events);
        _eventList.setAdapter(_eventAdapter);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date,
                               boolean selected) {
        Log.d("My Events", "Selected date:" + date.toString());

        _calendarView.invalidateDecorators();

        _events.clear();

        if (date.equals(CalendarDay.from(2017, 10, 6)))
            _events.add(_event1);
        else if (date.equals(CalendarDay.from(2017, 10, 8)))
            _events.add(_event2);
        else if (date.equals(CalendarDay.from(2017, 10, 11)))
            _events.add(_event3);
        else if (date.equals(CalendarDay.from(2017, 10, 14)))
            _events.add(_event4);

        _eventAdapter.notifyDataSetChanged();
    }
}
