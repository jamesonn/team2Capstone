package team2.mkesocial.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

import Firebase.User;
import team2.mkesocial.GeocodingLocation;
import team2.mkesocial.R;

import static Firebase.Databasable.DB_EVENTS_NODE_NAME;
import static Firebase.Databasable.DB_USERS_NODE_NAME;
import static team2.mkesocial.Activities.BaseActivity.getUid;


/**
 * Created by IaOng on 10/23/2017.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference(DB_USERS_NODE_NAME);
    private DatabaseReference eventDatabase = FirebaseDatabase.getInstance().getReference(DB_EVENTS_NODE_NAME);
    LatLng start;
    String start_title, address, locationAddress;;
    GeocodingLocation locAddr;
    String [] arrLatLng;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    if(childSnap.getKey().equals(getUid())) { //if the child node's uid = current uid because we ONLY display current uid info on myProfile page
                        User info = childSnap.getValue(User.class);//now able retrieve all info of the fields of that node

                        if(info!=null) {
                            //if(info.getAddress().equals("")) {
                                start = new LatLng(43.074982, -87.881344);
                                start_title = "UWM-Student Union";
                            //}
                           /* else {
                                //start = getLocationFromAddress(getApplicationContext(), info.getAddress());
                                start_title = "Home";
                                address = info.getAddress();
                                locAddr = new GeocodingLocation();

                                try {
                                    GeocodingLocation.getAddressFromLocation(address, getApplicationContext(), new GeocoderHandler());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                start = new LatLng(lat, lng);
                            }*/
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
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
        setUpMap();
    }

    @SuppressLint("HandlerLeak")
    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    arrLatLng = locationAddress.split("\n");
                    break;
                default:
                    locationAddress = null;
            }
            lat = Double.parseDouble(arrLatLng[3]);
            lng = Double.parseDouble(arrLatLng[4]);
            System.out.println("CONTENTS:"+arrLatLng[0]+" "+arrLatLng[1]+" "+ arrLatLng[2]+" "+arrLatLng[3]+" "+arrLatLng[4]+" ");
        }
    }

    public void setUpMap(){
        // Special, starting location marker will be in color blue
        // Add a default marker at UWM and move the camera to center on that coordinate
        //ToDo: Implement changing this starter marker to a home address specified by user
        mMap.addMarker(new MarkerOptions().position(start).title(start_title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 12));

        // All other (aka the "events) marker will be in red
        //ToDo: For loop to pull and add all event markers user has agreed to attend
        LatLng testing = new LatLng(43.074459, -87.880597);
        String test_loc = "Zipcar-Kenwood United Methodist Church";
        mMap.addMarker(new MarkerOptions().position(testing).title(test_loc));
    }
}
