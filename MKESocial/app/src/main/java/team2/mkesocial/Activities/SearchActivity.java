package team2.mkesocial.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.ListView;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import Firebase.Event;
import Firebase.Tag;
import team2.mkesocial.DateFilterFragment;
import team2.mkesocial.R;
import team2.mkesocial.Adapters.SimpleEventAdapter;

public class SearchActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener,
        ValueEventListener,
        AdapterView.OnItemSelectedListener,
        DateFilterFragment.DateFilterListener {

    private static final String TAG = "SearchActivity";

    private SearchView _searchView;
    private TextView _searchTextField;
    private Spinner _searchFilter;
    private ListView _searchResults;
    private ArrayList<Event> _eventList;
    private SimpleEventAdapter _resultsAdapter;
    private FirebaseDatabase _database;
    private Query _dataQuery;
    private String _queryString;
    private Date _filterStartDate;
    private Date _filterEndDate;
    private Toast _searchActivityToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        _searchView = (SearchView)findViewById(R.id.searchView);
        _searchFilter = (Spinner)findViewById(R.id.searchFilter);
        _searchResults = (ListView)findViewById(R.id.searchResults);
        _searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,int position, long id)
                {
                    Event selectedEvent = (Event)_searchResults.getItemAtPosition(position);
                    inspectEvent(selectedEvent.getEventId());
                }}
            );

        _searchTextField = (TextView)_searchView.findViewById(
                getResources().getIdentifier("android:id/search_src_text", null, null));

        _searchView.setOnQueryTextListener(this);

        _eventList = new ArrayList<>();
        _resultsAdapter = new SimpleEventAdapter(this, _eventList);
        _searchResults.setAdapter(_resultsAdapter);

        _database = FirebaseDatabase.getInstance();

        _searchView.setIconified(false);
        _searchFilter.setOnItemSelectedListener(this);

        if (savedInstanceState != null) {
            DateFilterFragment dff = (DateFilterFragment)getSupportFragmentManager().findFragmentByTag(TAG);
            if (dff != null) {
                dff.setListener(this);
            }
        }
    }

    private void inspectEvent(String eid) {
        if (eid != null) {
            Intent goToEventPage = new Intent(this, EventActivity.class);
            goToEventPage.putExtra("EVENT_ID", eid);
            startActivity(goToEventPage);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.d("Search", "Query string: " + query);

        _resultsAdapter.clear();
        _queryString = query.toLowerCase();

        _dataQuery = _database.getReference(Event.DB_EVENTS_NODE_NAME).orderByChild("title");
        _dataQuery.addValueEventListener(this);
        _searchView.clearFocus();

        _searchActivityToast = Toast.makeText(this, R.string.search_activity, Toast.LENGTH_SHORT);
        _searchActivityToast.show();

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        if (newText.equals(""))
            _resultsAdapter.clear();
        return false;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            try {
                boolean shouldAdd = false;
                Event event = Event.fromSnapshot(snapshot);

                switch (_searchFilter.getSelectedItemPosition())
                {
                    case 0:
                        String title = event.getTitle().toLowerCase();
                        shouldAdd = title.contains(_queryString);
                        break;
                    case 1:
                        String desc = event.getDescription().toLowerCase();
                        shouldAdd = desc.contains(_queryString);
                        break;
                    case 2:
                        Date date = event.getDate().getTime();
                        shouldAdd = date.getTime() >= _filterStartDate.getTime() &&
                                date.getTime() <= _filterEndDate.getTime();
                        break;
                    case 3:
                        for (Tag tag : event.getTags()) {
                            if (tag.getName().toLowerCase().contains(_queryString)) {
                                shouldAdd = true;
                                break;
                            }
                        }
                        break;
                }

                if (shouldAdd) {
                    _resultsAdapter.add(event);
                    if (_searchActivityToast != null) {
                        _searchActivityToast.cancel();
                        _searchActivityToast = null;
                    }
                }

                if (event != null)
                    Log.d(TAG, "Event: " + event.getTitle());
            } catch (Exception e) {
                Log.d(TAG, "Exception:" + e.toString());
                e.printStackTrace();
            }
        }
        _dataQuery.removeEventListener(this);
        _searchResults.requestFocus();

        int numResults = _resultsAdapter.getCount();
        Toast resultToast;
        if (numResults == 0)
            resultToast = Toast.makeText(this, R.string.search_empty_results, Toast.LENGTH_LONG);
        else
            resultToast = Toast.makeText(this,
                    String.format(getString(R.string.search_results_format), numResults, numResults > 1 ? "s" : ""),
                    Toast.LENGTH_SHORT);

        resultToast.show();
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 2) {
            _searchTextField.setFocusable(false);
            _searchTextField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DateFilterFragment dff = DateFilterFragment.create(SearchActivity.this,
                            _filterStartDate, _filterEndDate);
                    dff.show(getSupportFragmentManager(), TAG);
                }
            });
            DateFilterFragment dff = DateFilterFragment.create(this, _filterStartDate, _filterEndDate);
            dff.show(getSupportFragmentManager(), TAG);
        } else {
            if (!_searchTextField.isFocusable()) {
                _searchTextField.setText("");
                _searchTextField.setFocusableInTouchMode(true);
                _searchTextField.setFocusable(true);
                _searchTextField.setOnClickListener(null);
                _queryString = "";
                _filterStartDate = _filterEndDate = null;
            }
            // Trigger a search when the selected filter is changed
            if (_queryString != null && !_queryString.isEmpty())
                onQueryTextSubmit(_queryString);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.wtf(TAG, "No search filters selected. This shouldn't happen!");
    }

    @Override
    public void onDateFilterPositive(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        _filterStartDate = startDate;
        _filterEndDate = endDate;
        _searchTextField.setText(sdf.format(startDate) + " to " + sdf.format(endDate));
        _queryString = _searchTextField.getText().toString();

        // Trigger a search for the specified date range
        onQueryTextSubmit(_queryString);
    }
}
