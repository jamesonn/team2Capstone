package team2.mkesocial.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
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

import java.util.ArrayList;

import Firebase.Event;
import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;
import static Firebase.Databasable.DB_USER_SETTINGS_NODE_NAME;

import team2.mkesocial.Adapters.SimpleEventAdapter;

import static team2.mkesocial.Activities.BaseActivity.getUid;


/**
 * Created by IaOng on 10/23/2017.
 */

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    private DatabaseReference userSettingsDB = FirebaseDatabase.getInstance().getReference(DB_USER_SETTINGS_NODE_NAME);

    private LatLng start, current;
    private String start_title, current_title, home, aEvents, hEvents, mEvents;
    private Marker eventM;
    private MarkerOptions startM;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Settings.setDarkTheme())
            setTheme(R.style.MKEDarkTheme);
        super.onCreate(savedInstanceState);
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    if (childSnap.getKey().equals(getUid())) { //if the child node's uid = current uid because we ONLY display current uid info on myProfile page
                        User info = childSnap.getValue(User.class);//now able retrieve all info of the fields of that node

                        if (info != null) {
                            if (info.getAddress().equals("")) {//If User didn't specify their Home Address
                                start = new LatLng(43.074982, -87.881344);
                                start_title = "UWM-Student Union";
                                home = "2200 E Kenwood Blvd, Milwaukee, WI 53211, USA";
                            } else {//Home Address
                                start = new LatLng(info.getLat(), info.getLng());
                                start_title = "Home";
                                home = info.getFullAddress().substring(0, info.getFullAddress().lastIndexOf("\n"));
                            }
                            startM = new MarkerOptions().position(start).title(start_title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                            aEvents = info.parseEventAttendIDs();//HUE_GREEN
                            hEvents = info.parseEventHostIDs();//HUE_VIOLET
                            mEvents = info.parseEventMaybeIDs();//HUE_ORANGE

                            String[] aEv = aEvents.split("`");
                            String[] hEv = hEvents.split("`");
                            String[] mEv = mEvents.split("`");

                            String[] aID = info.parseEventAttendIDs().split("`");
                            String[] hID = info.parseEventHostIDs().split("`");
                            String[] mID = info.parseEventMaybeIDs().split("`");

                            eventDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                        Event event = Event.fromSnapshot(childSnap);
                                        for (int i = 0; i < aEv.length; ++i) {
                                            if (event.getEventId().equals(aEv[i])) {  //
                                                eventM = mMap.addMarker(new MarkerOptions().position(new LatLng(event.getLat(), event.getLng())).title(event.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                                eventM.setTag(aID[i]);
                                            }
                                        }
                                        for (int i = 0; i < hEv.length; ++i) {
                                            if (event.getEventId().equals(hEv[i])) {  //
                                                eventM = mMap.addMarker(new MarkerOptions().position(new LatLng(event.getLat(), event.getLng())).title(event.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                                                eventM.setTag(hID[i]);
                                            }
                                        }

                                        for (int i = 0; i < mEv.length; ++i) {
                                            if (event.getEventId().equals(mEv[i])) {  //
                                                eventM = mMap.addMarker(new MarkerOptions().position(new LatLng(event.getLat(), event.getLng())).title(event.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                                eventM.setTag(mID[i]);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


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
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        mMap.addMarker(startM);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 11));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mMap.setMyLocationEnabled(true);
        }


        /***********************************
         blue = home / current / default
         orange = maybe
         green = attending
         purple = hosting(edited)
         ***********************************/

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker m) {
                if (m.getTitle().equals("Home")||m.getTitle().equals("UWM-Student Union")){
                    // get a reference to the already created main layout
                    RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_m);

                    // inflate the layout of the popup window
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.pop_up, null);
                    TextView text = (TextView)popupView.findViewById(R.id.pop);
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
                else{//click on Event Markers :)

                    String key = m.getTag().toString();

                    eventDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(key)) {
                               inspectEvent(key);
                            }
                            else{
                                userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = User.fromSnapshot(dataSnapshot);
                                        //Remove event from user's DB profile
                                        if (user.getMaybeEid().contains(m.getTag() + "`" + m.getTitle() + "`")) {
                                            userDatabase.child(getUid()).child("maybeEid").setValue(user.getMaybeEid().replace(m.getTag().toString() + "`" + m.getTitle()+"`", ""));
                                        }
                                        if (user.getAttendEid().contains(m.getTag() + "`" + m.getTitle() + "`")) {
                                            userDatabase.child(getUid()).child("attendEid").setValue(user.getAttendEid().replace(m.getTag().toString() + "`" + m.getTitle()+"`", ""));
                                        }
                                        if (user.getHostEid().contains(m.getTag() + "`" + m.getTitle() + "`")) {
                                            userDatabase.child(getUid()).child("hostEid").setValue(user.getHostEid().replace(m.getTag().toString() + "`" + m.getTitle()+"`", ""));
                                        }

                                        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_m);

                                        // inflate the layout of the popup window
                                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                        View popupView = inflater.inflate(R.layout.pop_up, null);
                                        TextView text = (TextView)popupView.findViewById(R.id.pop);
                                        String msg = "The host has cancelled the event!\nIt will now be removed.";
                                        text.setText(msg);

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
                                        m.remove();//remove marker from map!
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }
        });

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

}
