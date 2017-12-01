package team2.mkesocial.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Firebase.User;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;
import static Firebase.Databasable.DB_USER_SETTINGS_NODE_NAME;
import static team2.mkesocial.Activities.BaseActivity.getUid;



/**
 * Created by IaOng on 10/23/2017.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    private DatabaseReference userSettingsDB = FirebaseDatabase.getInstance().getReference(DB_USER_SETTINGS_NODE_NAME);

    LatLng start, curent;
    String start_title, current_title = "Current Position", home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = User.fromSnapshot(dataSnapshot);
                if (user != null) {
                    if (user.getAddress().equals("")) {//If User didn't specify their Home Address
                        start = new LatLng(43.074982, -87.881344);
                        start_title = "UWM-Student Union";
                        home = "2200 E Kenwood Blvd, Milwaukee, WI 53211, USA";
                    } else {//Home Address
                        start = new LatLng(user.getLat(), user.getLng());
                        start_title = "Home";
                        home = user.getFullAddress();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * Default starting marker = UWM-Student Union
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //HUE_ORANGE -- Event
        //HUE_BLUE  -- Home Address (or the Default UWM-Union)
        //HUE_AZURE -- Current Location

        MarkerOptions startM, currentM, event;
        startM = new MarkerOptions().position(start).title(start_title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(startM);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 11));


        // All other (aka the "events) marker will be in red
        //ToDo: For loop to pull and add all event markers user has agreed to attend
        LatLng testing = new LatLng(43.074459, -87.880597);
        String test_loc = "Zipcar-Kenwood United Methodist Church";
        mMap.addMarker(new MarkerOptions().position(testing).title(test_loc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker m) {
                if (m.getTitle().equals("Home") || m.getTitle().equals("UWM-Student Union")) {
                    // get a reference to the already created main layout
                    RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_m);

                    // inflate the layout of the popup window
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.pop_up, null);

                    TextView text = (TextView) popupView.findViewById(R.id.pop);
                    text.setText(home);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.MATCH_PARENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // show the popup window
                    popupWindow.showAtLocation(mapLayout, Gravity.TOP, 0, 0);

                    // dismiss the popup window when touched
                    popupView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return true;
                        }
                    });
                }
            }
        });
    }
}