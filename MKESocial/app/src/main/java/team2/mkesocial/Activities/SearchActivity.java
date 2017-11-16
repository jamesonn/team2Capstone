package team2.mkesocial.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Firebase.Event;
import team2.mkesocial.R;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, ValueEventListener {

    private static final String TAG = "SearchActivity";

    private SearchView _searchView;
    private ListView _searchResults;
    private ArrayAdapter<String> _resultsAdapter;
    private FirebaseDatabase _database;
    private DatabaseReference _ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        _searchView = (SearchView)findViewById(R.id.searchView);
        _searchResults = (ListView)findViewById(R.id.searchResults);

        _searchView.setOnQueryTextListener(this);

        _database = FirebaseDatabase.getInstance();
        _ref =  _database.getReference(Event.DB_EVENTS_NODE_NAME);
        _ref.addValueEventListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.d("Search", "Query string: " + query);

        // Dummy data for demonstration
        String[] events = {"Concert", "Trivia Night", "Movie Marathon", "Charity Dinner"};
        ArrayList<String> eventList = new ArrayList<>();
        eventList.addAll(Arrays.asList(events));

        _resultsAdapter = new ArrayAdapter<>(this, R.layout.list_item_searchresult, eventList);

        _searchResults.setAdapter(_resultsAdapter);

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
                Event event = snapshot.getValue(Event.class);
                Log.d(TAG, "Event: " + event.getTitle());
            } catch (Exception e) {
                Log.d(TAG, "Exception:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }
}
