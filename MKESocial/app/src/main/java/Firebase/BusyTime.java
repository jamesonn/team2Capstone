package Firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class BusyTime {
    private long startTime;
    private long endTime;

    public BusyTime() {

    }

    public BusyTime(Date startTime, Date endTime) {
        this.startTime = startTime.getTime();
        this.endTime = endTime.getTime();
    }

    public long getStartTime() { return startTime; }

    @Exclude
    public Date getStartTimeAsDate() {
        return new Date(startTime);
    }

    @Exclude
    public void setStartTime(Date startTime) {
        this.startTime = startTime.getTime();
    }

    public long getEndTime() { return endTime; }

    @Exclude
    public Date getEndTimeAsDate() {
        return new Date(endTime);
    }

    @Exclude
    public void setEndTime(Date endTime) {
        this.endTime = endTime.getTime();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("startTime", startTime);
        result.put("endTime", endTime);

        return result;
    }

    @Exclude @Override
    public boolean equals(Object obj) {
        BusyTime other = (BusyTime)obj;

        if (other == null || getClass() != other.getClass())
            return false;

        return this.startTime == other.startTime && this.endTime == other.endTime;
    }
}
