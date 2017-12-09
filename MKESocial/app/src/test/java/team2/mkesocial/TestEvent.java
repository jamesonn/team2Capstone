package team2.mkesocial;

import android.content.Context;
import android.test.mock.MockContext;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import Firebase.Event;
import Firebase.Tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by cfoxj2 on 12/8/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class TestEvent {
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
    }
    @Test
    public void parseTags_space_test() throws Exception {

        List<Tag> baseList = new ArrayList<Tag>();
        baseList.add(new Tag("a"));
        baseList.add(new Tag("b"));
        List<Tag> testList = new ArrayList<Tag>();

        testList = Event.parseTags("a, b");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("  a,   b");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("   a   , b   ");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());
    }
    @Test
    public void parseTags_delimiter_test() throws Exception {

        List<Tag> baseList = new ArrayList<Tag>();
        baseList.add(new Tag("a"));
        baseList.add(new Tag("b"));
        List<Tag> testList = new ArrayList<Tag>();

        testList = Event.parseTags("a.b");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("a-b");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("a/b");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("a,b");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());
    }
    @Test
    public void parseTags_case_test() throws Exception {

        List<Tag> baseList = new ArrayList<Tag>();
        baseList.add(new Tag("aaa"));
        baseList.add(new Tag("bbb"));
        List<Tag> testList = new ArrayList<Tag>();

        testList = Event.parseTags("Aaa, Bbb");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("aAa, bBb");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

        testList = Event.parseTags("aaA, bbB");

        assertEquals(testList.get(0).getName(), baseList.get(0).getName());
        assertEquals(testList.get(1).getName(), baseList.get(1).getName());

    }
    @Test
    public void isInteger_test() throws Exception {
        assertTrue(Event.isInteger("-1", 10));
        assertTrue(Event.isInteger("-9", 10));
        assertTrue(Event.isInteger("1", 10));
        assertTrue(Event.isInteger("9", 10));
        assertTrue(Event.isInteger("0", 10));
        assertTrue(Event.isInteger("-0", 10));

        assertFalse(Event.isInteger("--", 10));
        assertFalse(Event.isInteger("", 10));
        assertFalse(Event.isInteger("ten", 10));
        assertFalse(Event.isInteger("1O1", 10));
        assertFalse(Event.isInteger("-", 10));
    }
    @Test
    public void set_same_dates_test()
    {
        GregorianCalendar startDate = Event.parseDate("07/22/2019");
        GregorianCalendar endDate = Event.parseDate("07/22/2019");

        Event event = new Event();
        assertTrue(event.setStartDate(Event.parseDate("07/22/2019")));
        //see if conversions work out
        assertEquals(startDate.getTimeInMillis(), event.getStartDate().getTimeInMillis());

        //can set both dates if they are the same
        assertTrue(event.setEndDate(endDate));
        assertTrue(event.setStartDate(startDate));
        assertTrue(event.setEndDate(endDate));
        assertTrue(event.setStartDate(startDate));


    }
    @Test
    public void set_different_dates_test()
    {
        String baseDate = "07/22/2019";
        GregorianCalendar startDate = Event.parseDate(baseDate);
        GregorianCalendar endDate = Event.parseDate(baseDate);
        Event event = new Event();
        //can set same day event
        assertTrue(event.setStartDate(startDate));
        assertTrue(event.setEndDate(endDate));

        //can't set if start date is after end
        startDate = Event.parseDate("08/22/2019");
        assertFalse(event.setStartDate(startDate));
        assertEquals(event.getFormattedStartDate(), baseDate);
        startDate = Event.parseDate("07/23/2019");
        assertFalse(event.setStartDate(startDate));
        assertEquals(event.getFormattedStartDate(), baseDate);
        startDate = Event.parseDate("07/22/2020");
        assertFalse(event.setStartDate(startDate));
        assertEquals(event.getFormattedStartDate(), baseDate);

        //can't set end date if it's before start
        endDate = Event.parseDate("06/22/2019");
        assertFalse(event.setEndDate(endDate));
        assertEquals(event.getFormattedEndDate(), baseDate);
        endDate = Event.parseDate("07/21/2019");
        assertFalse(event.setEndDate(endDate));
        assertEquals(event.getFormattedEndDate(), baseDate);
        endDate = Event.parseDate("07/22/2018");
        assertFalse(event.setEndDate(endDate));
        assertEquals(event.getFormattedEndDate(), baseDate);


    }



}
