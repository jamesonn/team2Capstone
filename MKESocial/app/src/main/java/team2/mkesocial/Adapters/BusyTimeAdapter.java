package team2.mkesocial.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Firebase.BusyTime;
import team2.mkesocial.R;


public class BusyTimeAdapter extends BaseAdapter {
    private Context _context;
    private LayoutInflater _inflater;
    private ArrayList<BusyTime> _dataSource;

    public BusyTimeAdapter(Context context, ArrayList<BusyTime> items) {
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
        // Get view for busy time item
        View busyTimeItem = _inflater.inflate(R.layout.list_item_busy_time, parent, false);

        TextView startTime = (TextView)busyTimeItem.findViewById(R.id.startTimeText);
        TextView endTime = (TextView)busyTimeItem.findViewById(R.id.endTimeText);

        BusyTime bt = (BusyTime)getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
        startTime.setText(dateFormat.format(bt.getStartTime()));
        endTime.setText(dateFormat.format(bt.getEndTime()));

        return busyTimeItem;
    }
}
