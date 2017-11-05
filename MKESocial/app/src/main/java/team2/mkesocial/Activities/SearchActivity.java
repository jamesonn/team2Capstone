package team2.mkesocial.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import team2.mkesocial.R;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchView _searchView;
    private ListView _searchResults;
    private ArrayAdapter<String> _resultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        _searchView = (SearchView)findViewById(R.id.searchView);
        _searchResults = (ListView)findViewById(R.id.searchResults);

        _searchView.setOnQueryTextListener(this);
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
}
