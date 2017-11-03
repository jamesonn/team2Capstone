package team2.mkesocial;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {
    private int _color;
    private HashSet<CalendarDay> _dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
        _color = color;
        _dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return _dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, _color));
    }
}
