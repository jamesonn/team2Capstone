package team2.mkesocial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import Firebase.Event;

public class SimpleEventAdapter extends BaseAdapter {
    private Context _context;
    private LayoutInflater _inflater;
    private ArrayList<Event> _dataSource;

    public SimpleEventAdapter(Context context, ArrayList<Event> items) {
        _context = context;
        _dataSource = items;
        _inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add (Event event) {
        _dataSource.add(event);
    }

    public void clear() { _dataSource.clear();}

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
        TextView eventItem = (TextView)_inflater.inflate(R.layout.list_item_searchresult, parent, false);
        
        Event e = (Event)getItem(position);
        eventItem.setText(e.getTitle());

        return eventItem;
    }
}
