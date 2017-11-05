package team2.mkesocial.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import team2.mkesocial.R;


/**
 * Created by IaOng on 10/23/2017.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Special, starting location marker will be in color blue
        // Add a default marker at UWM and move the camera to center on that coordinate
        LatLng start = new LatLng(43.074982, -87.881344);
        String start_title="UWM-Student Union";
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
