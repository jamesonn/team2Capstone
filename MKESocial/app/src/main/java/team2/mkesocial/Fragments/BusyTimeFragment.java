package team2.mkesocial.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Firebase.BusyTime;
import Firebase.Databasable;
import team2.mkesocial.Adapters.BusyTimeAdapter;
import team2.mkesocial.MaxHeightListView;
import team2.mkesocial.R;


public class BusyTimeFragment extends AppCompatDialogFragment
        implements DateTimeSelectFragment.DateTimeSelectListener, ValueEventListener {
    private final String TAG = BusyTimeFragment.class.getSimpleName();

    private MaxHeightListView _busyTimeList;
    private Button _addTimeButton;

    private BusyTimeAdapter _busyAdapter;
    private ArrayList<BusyTime> _busyTimes = new ArrayList<>();
    private BusyTime _selectedBusyTime;

    private BusyTimeListener _listener;
    private String _uid;

    private FirebaseDatabase _database;
    private DatabaseReference _dataRef;

    public interface BusyTimeListener
    {
        void OnBusyTimePositive(List<BusyTime> busyTimes);
    }

    public static BusyTimeFragment create(BusyTimeListener listener, String uid) {
        final BusyTimeFragment fragment = new BusyTimeFragment();
        fragment.setListener(listener);
        fragment.setUserId(uid);
        
        return fragment;
    }

    public void setListener(BusyTimeListener listener)
    {
        _listener = listener;
    }

    private void setUserId(String uid) {
        _uid = uid;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.fragment_busy_time_message);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.fragment_busy_times, null);
        builder.setView(view);

        _busyTimeList = (MaxHeightListView)view.findViewById(R.id.busyTimeList);
        _addTimeButton = (Button)view.findViewById(R.id.addTimeButton);

        _busyAdapter = new BusyTimeAdapter(getContext(), _busyTimes);
        _busyTimeList.setAdapter(_busyAdapter);

        _database = FirebaseDatabase.getInstance();
        _dataRef = _database.getReference(Databasable.DB_USERS_NODE_NAME).child(_uid).child("busyTimes");

        _busyTimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _selectedBusyTime = (BusyTime)_busyTimeList.getItemAtPosition(position);
                DateTimeSelectFragment fragment = DateTimeSelectFragment.create(BusyTimeFragment.this,
                        _selectedBusyTime.getStartTimeAsDate(), _selectedBusyTime.getEndTimeAsDate(), false);
                fragment.show(getFragmentManager(), TAG);
            }
        });

        _addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeSelectFragment fragment = DateTimeSelectFragment.create(BusyTimeFragment.this,
                        null, null, true);
                fragment.show(getFragmentManager(), TAG);
            }
        });

        builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _dataRef.setValue(_busyTimes);
                _listener.OnBusyTimePositive(_busyTimes);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        _dataRef.addValueEventListener(this);

        return builder.create();
    }

    @Override
    public void onDateTimeSelectPositive(Date startTime, Date endTime) {
        if (_selectedBusyTime != null) {
            _selectedBusyTime.setStartTime(startTime);
            _selectedBusyTime.setEndTime(endTime);
            _selectedBusyTime = null;
        } else {
            BusyTime time = new BusyTime(startTime, endTime);
            if (_busyTimes.contains(time)) {
                Toast.makeText(getContext(), getString(R.string.fragment_busy_time_dupe_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                _busyTimes.add(time);
            }
        }
        _busyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDateTimeSelectDelete(Date startTime, Date endTime) {
        if (_selectedBusyTime != null) {
            _busyTimes.remove(_selectedBusyTime);
            _selectedBusyTime = null;
            _busyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            BusyTime time = snapshot.getValue(BusyTime.class);
            _busyTimes.add(time);
        }
        _busyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
