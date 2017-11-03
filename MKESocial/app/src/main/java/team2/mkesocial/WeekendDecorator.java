package team2.mkesocial;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

public class WeekendDecorator implements DayViewDecorator {
    private final Calendar _calendar = Calendar.getInstance();
    private final Drawable _highlightDrawable;
    private static final int _color = Color.parseColor("#228BC34A");

    public WeekendDecorator() {
        _highlightDrawable = new ColorDrawable(_color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(_calendar);
        int weekDay = _calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(_highlightDrawable);
    }
}
