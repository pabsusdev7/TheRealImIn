package com.ingenuityapps.android.therealimin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.ingenuityapps.android.therealimin.data.CheckIn;
import com.ingenuityapps.android.therealimin.data.Event;
import com.ingenuityapps.android.therealimin.databinding.ActivityCheckInBinding;
import com.ingenuityapps.android.therealimin.databinding.ActivityEventDetailBinding;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.ingenuityapps.android.therealimin.utilities.JsonUtils;
import com.ingenuityapps.android.therealimin.utilities.LocationUtils;
import com.ingenuityapps.android.therealimin.utilities.NetworkUtils;

import org.json.JSONException;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckInActivity extends AppCompatActivity implements LocationListener {

    private FirebaseFirestore db;

    private ArrayAdapter<StringWithTag> mEventsAdapter;
    private Map<String, Event> mEvents;
    private List<Geofence> mGeofenceList;
    private String mDeviceID;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private DrawerLayout mDrawerLayout;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessage;
    private TextView mCheckedInStatus;
    private View mEventContainer;
    private View mEventInfoContainer;
    private Spinner mEventsSpinner;
    private TextView mEventDescription;
    private TextView mEventLocation;
    private TextView mEventLocationDirections;
    private TextView mEventDate;
    private TextView mEventStartTime;
    private TextView mEventEndTime;
    private Button mCheckIn;

    private static final String TAG = CheckInActivity.class.getSimpleName();


    private ActivityCheckInBinding mCheckInBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCheckInBinding = DataBindingUtil.setContentView(this, R.layout.activity_check_in);

        db = FirebaseFirestore.getInstance();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_imin2_round);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofenceList = new ArrayList<Geofence>();
        mEvents = new HashMap<String, Event>();
        mEventContainer = (View) findViewById(R.id.event_container);
        mEventInfoContainer = (View) findViewById(R.id.event_info_container);
        mCheckedInStatus = (TextView) findViewById(R.id.tv_checkin_status);
        mEventDescription = (TextView) findViewById(R.id.event);
        mEventDate = (TextView) findViewById(R.id.date);
        mEventLocation = (TextView) findViewById(R.id.location_map);
        mEventLocationDirections = (TextView) findViewById(R.id.location_description);
        mEventStartTime = (TextView) findViewById(R.id.start_time);
        mEventEndTime = (TextView) findViewById(R.id.end_time);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorMessage = (TextView) findViewById(R.id.tv_error_message_display);
        mCheckIn = (Button) findViewById(R.id.btn_checkin);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        mEventsSpinner = (Spinner) findViewById(R.id.event_spinner);

        mEventsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                if (pos > 0) {
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy");

                    StringWithTag selectedItem = (StringWithTag) adapterView.getItemAtPosition(pos);
                    final Event event = mEvents.get((String) selectedItem.tag);
                    mEventDescription.setVisibility(View.GONE);
                    //mEventDescription.setText(event.getDescription());
                    mEventDate.setText(dateFormatter.format(event.getStarttime().toDate()));
                    mEventLocation.setText(event.getLocation().getDescription());
                    mEventLocationDirections.setText(R.string.show_directions);
                    mEventLocationDirections.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LocationUtils.openLocationInMap(getApplicationContext(), event.getLocation().getGeolocation());
                        }
                    });
                    mEventStartTime.setText(timeFormatter.format(event.getStarttime().toDate()));
                    mEventEndTime.setText(timeFormatter.format(event.getEndtime().toDate()));

                    mEventInfoContainer.setVisibility(View.VISIBLE);
                } else
                    mEventInfoContainer.setVisibility(View.GONE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Event e = mEvents.get(((StringWithTag) mEventsSpinner.getSelectedItem()).tag);
                if (e != null) {
                    Log.d(TAG,"1");
                    if (mDeviceID != null) {
                        Log.d(TAG,"2");
                        try {
                            getCurrentLocation(null);
                            if (mCurrentLocation != null) {
                                Log.d(TAG,"3");
                                Location eventLocation = new Location(mLocationManager.getBestProvider(createFineCriteria(), true));
                                eventLocation.setLatitude(e.getLocation().getGeolocation().getLatitude());
                                eventLocation.setLongitude(e.getLocation().getGeolocation().getLongitude());
                                Log.d(TAG, "Current Location: " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
                                Log.d(TAG, "Event's Location: " + eventLocation.getLatitude() + "," + eventLocation.getLongitude());



                                if (new Date(Calendar.getInstance().getTimeInMillis() + (Constants.MAX_MINUTES_CHECKIN_BEFOREEVENT * Constants.ONE_MINUTE_IN_MILLIS)).after(e.getStarttime().toDate())
                                        && new Date(Calendar.getInstance().getTimeInMillis()).before(e.getEndtime().toDate())) {

                                    if (mCurrentLocation.distanceTo(eventLocation) <= e.getLocation().getRadius()) {

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("checkintime", Timestamp.now());
                                        data.put("checkouttime", null);
                                        data.put("eventid", e.getDocumentreference());
                                        data.put("deviceid",db.collection("device").document(mDeviceID));


                                        db.collection("checkin")
                                                .add(data)
                                                .continueWith(new Continuation<DocumentReference, DocumentReference>() {
                                                    @Override
                                                    public DocumentReference then(@NonNull Task<DocumentReference> task) throws Exception {


                                                        DocumentReference checkin = task.getResult();

                                                        mGeofenceList.add(new Geofence.Builder()
                                                                // Set the request ID of the geofence. This is a string to identify this
                                                                // geofence.
                                                                .setRequestId(e.getEventID())

                                                                .setCircularRegion(
                                                                        e.getLocation().getGeolocation().getLatitude(),
                                                                        e.getLocation().getGeolocation().getLongitude(),
                                                                        e.getLocation().getRadius()
                                                                )
                                                                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                                                                .build());
                                                        Log.i(TAG, "Creating GeoFence for: "+e.getLocation().getGeolocation().getLatitude()+","+e.getLocation().getGeolocation().getLongitude()+" - with Radius: "+e.getLocation().getRadius());

                                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                            if (!checkLocationPermission())
                                                                return null;
                                                        }
                                                        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(checkin.getId()));
                                                        Log.i(TAG, "Adding GeoFence");

                                                        return checkin;
                                                    }
                                                })
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d(TAG, "CheckIn DocumentSnapshot written with ID: " + documentReference.getId());
                                                        Toast toast = Toast.makeText(getApplicationContext(), "Check In Successful!", Toast.LENGTH_LONG);
                                                        toast.show();


                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding CheckIn document", e);
                                                        Toast toast = Toast.makeText(getApplicationContext(), "Check In UnSuccessful! Please, try again.", Toast.LENGTH_LONG);
                                                        toast.show();
                                                    }
                                                });




                                        showCheckInMessage(e.getDescription());
                                        Intent checkedInActivityIntent = new Intent(getApplicationContext(), CheckInActivity.class);
                                        checkedInActivityIntent.putExtra("checkedin", true);
                                        checkedInActivityIntent.putExtra("checkedin_event", e.getDescription());
                                        startActivity(checkedInActivityIntent);



                                    } else {
                                        Toast toast = Toast.makeText(getApplicationContext(), "You're out of range! Get closer to the event's location at " + e.getLocation().getDescription() + ".", Toast.LENGTH_LONG);
                                        toast.show();
                                    }



                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), "It's not time yet to check in for " + e.getDescription() + ". Check in is available " + Constants.MAX_MINUTES_CHECKIN_BEFOREEVENT + " minutes before the event.", Toast.LENGTH_LONG);
                                    toast.show();
                                }



                            }


                        } catch (Exception ex) {
                            Log.v(TAG, ex.getMessage());
                            ex.printStackTrace();
                        }


                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select an event!", Toast.LENGTH_LONG);
                }

            }
        });


        mEventsSpinner.setEnabled(true);
        mCheckIn.setEnabled(true);

        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        mDeviceID = prefs.getString("deviceid",null);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("checkedin") && intentThatStartedThisActivity.hasExtra("checkedin_event")) {
                Boolean checkedin = intentThatStartedThisActivity.getBooleanExtra("checkedin", false);
                String checkedin_event = intentThatStartedThisActivity.getStringExtra("checkedin_event");
                if (checkedin) {
                    mEventsSpinner.setEnabled(false);
                    mCheckIn.setEnabled(false);
                    mCheckIn.setBackgroundColor(getResources().getColor(R.color.checkedin_status));
                    mCheckedInStatus.setText("You're currently checked into " + checkedin_event + ". You'll be checked out as soon as you leave the location.");
                    mCheckedInStatus.setVisibility(View.VISIBLE);
                }
            }
        }

        loadEvents();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.drawer_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final Context context = getApplicationContext();
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.nav_home:
                intent = new Intent(context, CheckInActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_checkin:
                intent = new Intent(context, CheckInActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_attendance:
                intent = new Intent(context, AttendanceActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Intent logInActivityIntent = new Intent(context, LoginActivity.class);
                                    startActivity(logInActivityIntent);
                                }
                            }
                        });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCheckInMessage(String event) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.checkin_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(R.string.toast_checkin);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }

    private void loadEvents() {

        Source source = Source.CACHE;

        db.collection("event")
                .whereGreaterThan("endtime",Timestamp.now())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<StringWithTag> events = new ArrayList<StringWithTag>();
                            events.add(new StringWithTag("Select One", 0));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                //Event ev = new Event(Integer.parseInt(eventFields[0]), eventFields[1], eventFields[2], new BigDecimal(eventFields[3]), new BigDecimal(eventFields[4]), new BigDecimal(eventFields[5]), formatter.parse(eventFields[6]), formatter.parse(eventFields[7]));
                                final Event event = new Event(document.getId(), document.get("description").toString(), document.getTimestamp("starttime"), document.getTimestamp("endtime"));
                                event.setDocumenteference(document.getReference());

                                DocumentReference locationRef = document.getDocumentReference("locationid");

                                locationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        com.ingenuityapps.android.therealimin.data.Location location = documentSnapshot.toObject(com.ingenuityapps.android.therealimin.data.Location.class);
                                        location.setDocumentreference(documentSnapshot.getReference());
                                        event.setLocation(location);

                                        events.add(new StringWithTag(event.getDescription(), event.getEventID()));
                                        mEvents.put(event.getEventID(), event);
                                        Log.v(TAG, "Event " + event.getDescription() + " is available for check in.");


                                    }
                                });

                            }

                            if (events != null) {
                                showEventsDataView();
                                mEventsAdapter = new ArrayAdapter<StringWithTag>(getBaseContext(), android.R.layout.simple_spinner_item, events);
                                mEventsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mEventsSpinner.setAdapter(mEventsAdapter);
                                mEventContainer.setVisibility(View.VISIBLE);
                            } else {
                                showErrorMessage();
                            }
                        } else {
                            showErrorMessage();
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    private void showEventsDataView() {
        mEventContainer.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mEventContainer.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private class HasDeviceCheckedIntoEvent extends AsyncTask<Integer, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Integer... integers) {

            if(integers.length == 2)
            {
                HashMap<String,String> params = new HashMap<String,String>();
                params.put("event",integers[0].toString());
                params.put("device",integers[1].toString());
                URL checkinURL = NetworkUtils.buildUrl("checkins", params);

                try
                {
                    String response = NetworkUtils.getResponseFromHttpUrl(checkinURL);
                    if(response!=null)
                    {
                        String []checkin = response.split("\\|");
                        Log.v(TAG,"Device " + integers[1] + " has checked in for event " + integers[0] + " at " + checkin[1] + " (CheckIn ID: " + checkin[0]);
                        return true;
                    }
                    else
                    {
                        Log.v(TAG,"Device " + integers[1] + " has not checked in for event " + integers[0]);
                        return false;
                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    return false;
                }
            }

            return false;

        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        Log.i(TAG, "Current GeoFence List:");
        for(Geofence geofence: mGeofenceList)
        {
            Log.i(TAG, geofence.toString());
        }
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(String checkInID) {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra("checkInID", checkInID);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onLocationChanged(Location location) {
        getCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void getCurrentLocation(Location location) {


        if(checkLocationPermission()) {
            mCurrentLocation = location != null ? location : mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(createFineCriteria(), true));
            Log.v(TAG, "Current Location: " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
        }

    }

    public static Criteria createFineCriteria() {

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;

    }

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CheckInActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        Constants.PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(createFineCriteria(),true),400,1, this);
            return true;
        }
    }

    public boolean checkDeviceInfoPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_device_permission)
                        .setMessage(R.string.text_device_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CheckInActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        Constants.PERMISSIONS_REQUEST_PHONE_STATE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        Constants.PERMISSIONS_REQUEST_PHONE_STATE);
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
            case Constants.PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(createFineCriteria(),true),400,1, this);


                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }


        }
    }

    private static class StringWithTag {
        public String string;
        public Object tag;

        public StringWithTag(String string, Object tag) {
            this.string = string;
            this.tag = tag;
        }

        @Override
        public String toString() {
            return string;
        }
    }


}
