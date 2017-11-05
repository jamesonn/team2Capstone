package team2.mkesocial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import team2.mkesocial.R;
import Firebase.Event;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

public class EventAdapter extends BaseAdapter {
    private Context _context;
    private LayoutInflater _inflater;
    private ArrayList<Event> _dataSource;

    public EventAdapter(Context context, ArrayList<Event> items) {
        _context = context;
        _dataSource = items;
        _inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return _dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for event item
        View eventItem = _inflater.inflate(R.layout.list_item_event, parent, false);

        TextView startTime = (TextView)eventItem.findViewById(R.id.startTime);
        TextView endTime = (TextView)eventItem.findViewById(R.id.endTime);
        TextView eventTitle = (TextView)eventItem.findViewById(R.id.eventTitle);

        Event e = (Event)getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        startTime.setText(dateFormat.format(e.get_startTime().getTime()));
        endTime.setText(dateFormat.format(e.get_endTime().getTime()));
        eventTitle.setText(e.get_title());

        return eventItem;
    }

}
