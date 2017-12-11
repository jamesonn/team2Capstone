package team2.mkesocial.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Firebase.Event;
import Firebase.MethodOrphanage;
import Firebase.Settings;
import Firebase.User;
import team2.mkesocial.DirectionsJSONParser;
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

    private LatLng start, current, origin;
    private String start_title, current_title, home, aEvents, hEvents, mEvents, dist, dur, msg;
    private double latitude, longitude;
    private Marker eventM;
    private MarkerOptions startM;
    private Polyline polylineFinal;
    private Location location = null;
    private PolylineOptions lineOptions = null;
    private boolean hasRoute = false;
   // private String[] aEv, hEv, mEv, aID, hID, mID;

    private LocationManager locationManager;



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapsInitializer.initialize(getApplicationContext());
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
                                start = new LatLng(MethodOrphanage.getLat(info.getAddress()), MethodOrphanage.getLng(info.getAddress()));
                                start_title = "Home";
                                home = info.getFullAddress().substring(0, info.getFullAddress().lastIndexOf("\n"));
                            }
                            startM = new MarkerOptions().position(start).title(start_title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                            aEvents = info.parseEventAttendIDs();//HUE_GREEN
                            hEvents = info.parseEventHostIDs();//HUE_VIOLET
                            mEvents = info.parseEventMaybeIDs();//HUE_ORANGE

                            /*
                            aEv = aEvents.split("`");
                            hEv = hEvents.split("`");
                            mEv = mEvents.split("`");

                            aID = info.parseEventAttendIDs().split("`");
                            hID = info.parseEventHostIDs().split("`");
                            mID = info.parseEventMaybeIDs().split("`");
                            */

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
                                                eventM = mMap.addMarker(new MarkerOptions().position(new LatLng(MethodOrphanage.getLat(event.getLocation()),
                                                        MethodOrphanage.getLng(event.getLocation()))).title(event.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                                eventM.setTag(aID[i]);
                                            }
                                        }
                                        for (int i = 0; i < hEv.length; ++i) {
                                            if (event.getEventId().equals(hEv[i])) {  //
                                                eventM = mMap.addMarker(new MarkerOptions().position(new LatLng(MethodOrphanage.getLat(event.getLocation()),
                                                        MethodOrphanage.getLng(event.getLocation()))).title(event.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                                                eventM.setTag(hID[i]);
                                            }
                                        }

                                        for (int i = 0; i < mEv.length; ++i) {
                                            if (event.getEventId().equals(mEv[i])) {  //
                                                eventM = mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(MethodOrphanage.getLat(event.getLocation()),
                                                                MethodOrphanage.getLng(event.getLocation()))).title(event.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
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
        mapFragment.getMapAsync(MapsActivity.this::onMapReady);


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
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                m.showInfoWindow();
                if(hasRoute){
                    polylineFinal.remove();//remove OLD path, so can create new path
                    hasRoute=false; //let map know there's no route again; good to draw
                }

                /*************************/
                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(origin, m.getPosition());
                DownloadTask downloadTask = new DownloadTask();
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
                /*************************/
                // inflate the layout of the popup window
                RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_m);

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.pop_up, null);
                TextView text = (TextView) popupView.findViewById(R.id.pop);
                /*if(dist!=null && dur!=null) {
                    String msg = "Distance: " + dist + "\nDuration: " + dur;
                    text.setText(msg);
                }*/

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
                return true;
            }
        });


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
                } else {//click on Event Markers :)

                    String key = m.getTag().toString();

                    eventDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(key)) {
                                inspectEvent(key);
                            } else {
                                userDatabase.child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = User.fromSnapshot(dataSnapshot);
                                        //Remove event from user's DB profile
                                        if (user.getMaybeEid().contains(m.getTag() + "`" + m.getTitle() + "`")) {
                                            userDatabase.child(getUid()).child("maybeEid").setValue(user.getMaybeEid().replace(m.getTag().toString() + "`" + m.getTitle() + "`", ""));
                                        }
                                        if (user.getAttendEid().contains(m.getTag() + "`" + m.getTitle() + "`")) {
                                            userDatabase.child(getUid()).child("attendEid").setValue(user.getAttendEid().replace(m.getTag().toString() + "`" + m.getTitle() + "`", ""));
                                        }
                                        if (user.getHostEid().contains(m.getTag() + "`" + m.getTitle() + "`")) {
                                            userDatabase.child(getUid()).child("hostEid").setValue(user.getHostEid().replace(m.getTag().toString() + "`" + m.getTitle() + "`", ""));
                                        }

                                        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.activity_m);

                                        // inflate the layout of the popup window
                                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                        View popupView = inflater.inflate(R.layout.pop_up, null);
                                        TextView text = (TextView) popupView.findViewById(R.id.pop);
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
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){sb.append(line);}

            data = sb.toString();
            br.close();

        }catch(Exception e){Log.d("Exception while downloading url", e.toString());}
        finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{data = downloadUrl(url[0]);}// Fetching the data from web service
            catch(Exception e){Log.d("Background Task",e.toString());}
            return data;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){e.printStackTrace();}
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j <path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){ // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);

            }
            dist = distance;
            dur = duration;
            msg = "Distance: " + dist + "\nDuration: " + dur;
            //tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);

            // Drawing polyline in the Google Map for the i-th route
            polylineFinal = mMap.addPolyline (lineOptions);
            //mMap.addPolyline(lineOptions);
            hasRoute=true;
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        origin = new LatLng(latitude, longitude);


                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, startM.getPosition());
                        //String url = getDirectionsUrl(origin, new LatLng(43.074982, -87.881344));
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);


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


