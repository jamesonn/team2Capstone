package team2.mkesocial.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import Firebase.Event;
import Firebase.Settings;
import Firebase.Tag;
import Firebase.User;
import team2.mkesocial.Adapters.SimpleEventAdapter;
import team2.mkesocial.R;

public class FeedActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ValueEventListener {

    private FirebaseDatabase _database;
    private Query _dataQuery;
    private ListView _eventResults;
    private SimpleEventAdapter _resultsAdapter;
    private ArrayList<Event> _eventList;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_feed);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            _eventResults = (ListView)findViewById(R.id.event_results);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        _database = FirebaseDatabase.getInstance();

        _eventList = new ArrayList<>();
        _resultsAdapter = new SimpleEventAdapter(this, _eventList);
        _eventResults.setAdapter(_resultsAdapter);

        _dataQuery = _database.getReference(Event.DB_EVENTS_NODE_NAME).orderByChild("title");
        _dataQuery.addValueEventListener(this);

        _eventResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event)_eventResults.getItemAtPosition(position);
                inspectEvent(selectedEvent.getEventId());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class );
        } else if (id == R.id.nav_create_event) {
            intent = new Intent(this, CreateEventActivity.class );
        } else if (id == R.id.nav_maps) {
            intent = new Intent(this, MapsActivity.class );
        } else if (id == R.id.nav_my_events) {
            intent = new Intent(this, MyEventsActivity.class );
        } else if (id == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class );
        } else if (id == R.id.nav_search) {
            intent = new Intent(this, SearchActivity.class );
        } else if (id == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class );
        } else if (id == R.id.nav_log_out) {
            logout();
            return true;
        }
        startActivity(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        final Intent startOver = new Intent(this, SplashActivity.class);
        if(LoginActivity.getGoogleSignIn() != null) {
            LoginActivity.getGoogleSignIn().signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    startActivity(startOver);
                }
            });
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        _resultsAdapter.clear();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Event event = Event.fromSnapshot(snapshot);
            if (event.getTitle() != null) {
                _resultsAdapter.add(event);
            }
        }
        _resultsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        //init static settings
        Settings.runMethodOnDBSettingsObj(null, false);
        if(Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);
        super.onStart();
    }
          
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_warning)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
