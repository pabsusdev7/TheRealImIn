package com.ingenuityapps.android.therealimin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.ingenuityapps.android.therealimin.data.Event;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.ingenuityapps.android.therealimin.utilities.LocationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckInActivity extends AppCompatActivity implements LocationListener {

    private FirebaseFirestore db;

    private ArrayAdapter<StringWithTag> mEventsAdapter;
    private Map<String, Event> mEvents;
    private List<Geofence> mGeofenceList;
    private String mDeviceID;

    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private CountDownTimer mTimer;

    @BindView(R.id.tv_empty_message_display)
    TextView mEmptyMessage;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessage;
    @BindView(R.id.tv_checkin_status)
    TextView mCheckedInStatus;
    @BindView(R.id.event_container)
    View mEventContainer;
    @BindView(R.id.event_info_container)
    View mEventInfoContainer;
    @BindView(R.id.timer_container)
    View mEventTimerContainer;
    @BindView(R.id.tv_timer)
    TextView mEventTimer;
    @BindView(R.id.event_spinner)
    Spinner mEventsSpinner;
    @BindView(R.id.event)
    TextView mEventDescription;
    @BindView(R.id.tv_event_label)
    TextView mEventLabel;
    @BindView(R.id.location_map)
    TextView mEventLocation;
    @BindView(R.id.location_description)
    TextView mEventLocationDirections;
    @BindView(R.id.date)
    TextView mEventDate;
    @BindView(R.id.start_time)
    TextView mEventStartTime;
    @BindView(R.id.end_time)
    TextView mEventEndTime;
    @BindView(R.id.tv_required)
    TextView mEventRequired;
    @BindView(R.id.btn_checkin)
    Button mCheckIn;

    private static final String TAG = CheckInActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        ButterKnife.bind(this);

        db = FirebaseFirestore.getInstance();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_imin2_round);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofenceList = new ArrayList<Geofence>();
        mEvents = new HashMap<String, Event>();

        getCurrentLocation(null);

        final SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        mDeviceID = prefs.getString(Constants.SHARED_PREF_DEVICEID,null);




        if (prefs.contains(Constants.SHARED_PREF_CHECKEDIN) && prefs.contains(Constants.SHARED_PREF_EVENTID) && prefs.contains(Constants.SHARED_PREF_CHECKINID) && prefs.contains(Constants.SHARED_PREF_EVENTENDTIME)) {
            Boolean checkedin = prefs.getBoolean(Constants.SHARED_PREF_CHECKEDIN,false);
            String event_id = prefs.getString(Constants.SHARED_PREF_EVENTID, null);
            final String checkin_id = prefs.getString(Constants.SHARED_PREF_CHECKINID, null);
            Long event_endtime = prefs.getLong(Constants.SHARED_PREF_EVENTENDTIME,0);
            if (checkedin && event_id!=null && checkin_id!=null) {

                Log.d(TAG,"User is currently checked in!");

                final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                final Calendar calendar = Calendar.getInstance();
                final SharedPreferences.Editor editor = prefs.edit();

                if(mTimer==null) {
                    mTimer = new CountDownTimer((event_endtime - Timestamp.now().getSeconds()) * 1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            Log.d(TAG, "Seconds remaining: " + millisUntilFinished / 1000);

                            calendar.setTimeInMillis(millisUntilFinished);
                            calendar.set(Calendar.HOUR_OF_DAY, (int) (millisUntilFinished / 3600000));

                            mEventTimer.setText(timeFormatter.format(calendar.getTime()));

                            if (!prefs.getBoolean(Constants.SHARED_PREF_CHECKEDIN, false))
                                cancel();
                        }

                        public void onFinish() {
                            Log.d(TAG, "Done!");

                            mEventTimer.setText(getResources().getString(R.string.timer_done));

                            DocumentReference checkInRef = db.collection(Constants.FIRESTORE_CHECKIN).document(checkin_id);

                            checkInRef
                                    .update(Constants.FIRESTORE_CHECKIN_CHECKOUTTIME, Timestamp.now())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast toast = Toast.makeText(getApplicationContext(), "Check Out Successful!", Toast.LENGTH_LONG);
                                            toast.show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast toast = Toast.makeText(getApplicationContext(), "Check Out UnSuccessful! Please, try again.", Toast.LENGTH_LONG);
                                            toast.show();
                                        }
                                    });

                            editor.putBoolean(Constants.SHARED_PREF_CHECKEDIN, false);
                            editor.apply();

                            Intent checkInIntent = new Intent(getApplicationContext(), CheckInActivity.class);
                            startActivity(checkInIntent);
                        }
                    };

                    mTimer.start();
                }

                db.collection(Constants.FIRESTORE_EVENT).document(event_id)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                        final Event event = new Event(document.getId(), document.get(Constants.FIRESTORE_EVENT_DESCRIPTION).toString(), document.getTimestamp(Constants.FIRESTORE_EVENT_STARTTIME), document.getTimestamp(Constants.FIRESTORE_EVENT_ENDTIME), document.getBoolean(Constants.FIRESTORE_EVENT_REQUIRED));

                                        document.getDocumentReference(Constants.FIRESTORE_EVENT_LOCATIONID).get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        com.ingenuityapps.android.therealimin.data.Location location = documentSnapshot.toObject(com.ingenuityapps.android.therealimin.data.Location.class);
                                                        event.setLocation(location);


                                                        setUpEventInfoContainer(event, true);
                                                        mEventInfoContainer.setVisibility(View.VISIBLE);
                                                        //Hide/Show CheckedIn Controls
                                                        mCheckedInStatus.setText(String.format(getResources().getString(R.string.checkedin_message),event.getDescription(),event.getLocation().getDescription()));
                                                        mCheckedInStatus.setVisibility(View.VISIBLE);
                                                        mEventTimerContainer.setVisibility(View.VISIBLE);
                                                        mEventsSpinner.setVisibility(View.GONE);
                                                        mCheckIn.setVisibility(View.GONE);
                                                        mEventLabel.setVisibility(View.GONE);


                                                    }
                                                });
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "Get failed with ", task.getException());
                                }
                            }
                        });






            }
            else
                loadEvents();
        }
        else
            loadEvents();

        mEventsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                if (pos > 0) {

                    StringWithTag selectedItem = (StringWithTag) adapterView.getItemAtPosition(pos);


                    Event event = mEvents.get((String)selectedItem.tag);

                    setUpEventInfoContainer(event, false);

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
                    if (mDeviceID != null) {
                        try {
                            getCurrentLocation(null);
                            if (mCurrentLocation != null) {
                                Location eventLocation = new Location(mLocationManager.getBestProvider(createFineCriteria(), true));
                                eventLocation.setLatitude(e.getLocation().getGeolocation().getLatitude());
                                eventLocation.setLongitude(e.getLocation().getGeolocation().getLongitude());
                                Log.d(TAG, "Current Location: " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
                                Log.d(TAG, "Event's Location: " + eventLocation.getLatitude() + "," + eventLocation.getLongitude());



                                if (new Date(Calendar.getInstance().getTimeInMillis() + (Constants.MAX_MINUTES_CHECKIN_BEFOREEVENT * Constants.ONE_MINUTE_IN_MILLIS)).after(e.getStarttime().toDate())
                                        && new Date(Calendar.getInstance().getTimeInMillis()).before(e.getEndtime().toDate())) {

                                    if (mCurrentLocation.distanceTo(eventLocation) <= e.getLocation().getRadius()) {

                                        Map<String, Object> data = new HashMap<>();
                                        data.put(Constants.FIRESTORE_CHECKIN_CHECKINTIME, Timestamp.now());
                                        data.put(Constants.FIRESTORE_CHECKIN_CHECKOUTTIME, null);
                                        data.put(Constants.FIRESTORE_CHECKIN_EVENTID, db.collection(Constants.FIRESTORE_EVENT).document(e.getEventID()));
                                        data.put(Constants.FIRESTORE_CHECKIN_DEVICEID,db.collection(Constants.FIRESTORE_DEVICE).document(mDeviceID));


                                        db.collection(Constants.FIRESTORE_CHECKIN)
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
                                                    public void onSuccess(final DocumentReference documentReference) {
                                                        Log.d(TAG, "CheckIn DocumentSnapshot written with ID: " + documentReference.getId());

                                                        final SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
                                                        final SharedPreferences.Editor editor = prefs.edit();


                                                        editor.putBoolean(Constants.SHARED_PREF_CHECKEDIN, true);
                                                        editor.putString(Constants.SHARED_PREF_EVENTID,e.getEventID());
                                                        editor.putString(Constants.SHARED_PREF_CHECKINID, documentReference.getId());
                                                        editor.putLong(Constants.SHARED_PREF_EVENTENDTIME, e.getEndtime().getSeconds());
                                                        editor.apply();

                                                        Intent checkedInActivityIntent = new Intent(getApplicationContext(), CheckInActivity.class);
                                                        startActivity(checkedInActivityIntent);


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







    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mTimer!=null)mTimer.cancel();
    }

    private void setUpEventInfoContainer(final Event event, boolean showTitle) {


        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy");

        mEventDescription.setVisibility(showTitle? View.VISIBLE: View.GONE);
        mEventDescription.setText(event.getDescription());
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
        mEventRequired.setVisibility(event.getRequired()?View.VISIBLE:View.INVISIBLE);
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

        db.collection(Constants.FIRESTORE_EVENT)
                .whereGreaterThan(Constants.FIRESTORE_EVENT_ENDTIME,Timestamp.now())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<StringWithTag> events = new ArrayList<StringWithTag>();
                            events.add(new StringWithTag("Select One", 0));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                final Event event = new Event(document.getId(), document.get(Constants.FIRESTORE_EVENT_DESCRIPTION).toString(), document.getTimestamp(Constants.FIRESTORE_EVENT_STARTTIME), document.getTimestamp(Constants.FIRESTORE_EVENT_ENDTIME), document.getBoolean(Constants.FIRESTORE_EVENT_REQUIRED));

                                DocumentReference locationRef = document.getDocumentReference(Constants.FIRESTORE_EVENT_LOCATIONID);

                                locationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        com.ingenuityapps.android.therealimin.data.Location location = documentSnapshot.toObject(com.ingenuityapps.android.therealimin.data.Location.class);
                                        event.setLocation(location);

                                        events.add(new StringWithTag(event.getDescription(), event.getEventID()));
                                        mEvents.put(event.getEventID(), event);
                                        Log.v(TAG, "Event " + event.getDescription() + " is available for check in.");


                                    }
                                });

                            }

                            if (events != null) {
                                mEventsAdapter = new ArrayAdapter<StringWithTag>(getBaseContext(), android.R.layout.simple_spinner_item, events);
                                mEventsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mEventsSpinner.setAdapter(mEventsAdapter);
                                if(!mEventsSpinner.getAdapter().isEmpty())
                                    showEventsDataView();
                                else{
                                    showNoResultsMessage();
                                }
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

    private void showNoResultsMessage() {
        mEventContainer.setVisibility(View.INVISIBLE);
        mEmptyMessage.setVisibility(View.VISIBLE);
    }

    private void showEventsDataView() {
        mEventContainer.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mEventContainer.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
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
        intent.putExtra(Constants.SHARED_PREF_CHECKINID, checkInID);
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
