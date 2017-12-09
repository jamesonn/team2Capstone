package Firebase;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by cfoxj2 on 12/8/2017.
 */

public class MethodOrphanage {

    public static String getFullAddress(String location){
        String fullAddress;//0000 Street Name, City, State Zip, Country:LatLng:(0,0)
        //City, State Zip, Country:LatLng:(0,0)
        //State Zip, Country:LatLng:(0,0)
        fullAddress = location.substring(0, location.indexOf(":"));
        String[] addr = fullAddress.split(",");
        //for loop to append the stuff together and then return it
        String firstPart = " ";
        for(int i=0;i<addr.length;++i){
            firstPart+=addr[i]+"\n";
        }
        return firstPart;
    }

    public static Double getLat(String location){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = location.substring(location.indexOf("(") + 1, location.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[0]);
    }

    public static Double getLng(String location){
        //0000 Street Name, City, State Zip, Country LatLng:(0,0)
        String toSplit = location.substring(location.indexOf("(") + 1, location.lastIndexOf(")"));
        String[] getLatLng = toSplit.split(",");
        return Double.parseDouble(getLatLng[1]);
    }

    /**
     * -1 if date1 is before date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int compareDates(GregorianCalendar date1, GregorianCalendar date2)
    {
        if(date1 == null || date2 == null) return 0;
        //get rid of time
        date1.set(Calendar.HOUR, 0);
        date1.set(Calendar.MINUTE, 0);
        date1.set(Calendar.SECOND, 0);
        date2.set(Calendar.HOUR, 0);
        date2.set(Calendar.MINUTE, 0);
        date2.set(Calendar.SECOND, 0);

        int result = date1.compareTo(date2);
        return result;

    }
    /**
     * -1 if time1 is before time2
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int compareTimes(GregorianCalendar time1, GregorianCalendar time2)
    {
        if(time1 == null || time2 == null) return 0;
        //get rid of time
        time1.set(Calendar.DAY_OF_MONTH, 0);
        time1.set(Calendar.MONTH, 0);
        time1.set(Calendar.YEAR, 0);
        time2.set(Calendar.DAY_OF_MONTH, 0);
        time2.set(Calendar.MONTH, 0);
        time2.set(Calendar.YEAR, 0);

        int result = time1.compareTo(time2);
        return result;

    }

}
