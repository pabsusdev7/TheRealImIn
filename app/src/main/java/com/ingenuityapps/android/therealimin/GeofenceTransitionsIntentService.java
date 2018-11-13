package com.ingenuityapps.android.therealimin;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ingenuityapps.android.therealimin.utilities.Constants;

import java.util.List;

/**
 * Created by pabloalbuja on 5/24/18.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    private FirebaseFirestore db;


    private GeofencingClient mGeofencingClient;


    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    // ...
    protected void onHandleIntent(final Intent intent) {

        db = FirebaseFirestore.getInstance();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        mGeofencingClient  = LocationServices.getGeofencingClient(this);
        if (geofencingEvent.hasError()) {
            /*String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());*/
            Log.e(TAG, "Error with geofencing event: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            Log.v(TAG, "GeoFence Transition: " + geofenceTransition + ", Triggering Location:" + geofencingEvent.getTriggeringLocation().toString()+", Triggering Geofence: "+geofencingEvent.getTriggeringGeofences().get(0).toString());
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            if(intent!=null)
            {
                if(intent.hasExtra(Constants.SHARED_PREF_CHECKINID))
                {
                    final String checkInID = intent.getStringExtra(Constants.SHARED_PREF_CHECKINID);
                    try {

                        DocumentReference checkInRef = db.collection(Constants.FIRESTORE_CHECKIN).document(checkInID);

                        checkInRef
                                .update(Constants.FIRESTORE_CHECKIN_CHECKOUTTIME,Timestamp.now())
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
                        mGeofencingClient.removeGeofences(PendingIntent.getService(this, 0, intent, PendingIntent.
                                FLAG_UPDATE_CURRENT));

                        Log.i(TAG, "Removing GeoFence for CheckIn: "+checkInID);
                        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit();
                        editor.putBoolean(Constants.SHARED_PREF_CHECKEDIN, false);
                        editor.apply();

                        Intent checkInIntent = new Intent(this, CheckInActivity.class);
                        startActivity(checkInIntent);


                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }

        } else {
            // Log the error.
        }
    }


}
