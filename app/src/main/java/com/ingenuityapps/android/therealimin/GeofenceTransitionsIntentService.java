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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.ingenuityapps.android.therealimin.utilities.NotificationUtils;

import java.util.List;

/**
 * Created by pabloalbuja on 5/24/18.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();


    private GeofencingClient mGeofencingClient;


    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    // ...
    protected void onHandleIntent(final Intent intent) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        mGeofencingClient  = LocationServices.getGeofencingClient(this);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error with geofencing event: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


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
                        final Timestamp checkOutTime = Timestamp.now();


                        checkInRef
                                .update(Constants.FIRESTORE_CHECKIN_CHECKOUTTIME,checkOutTime)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast toast = Toast.makeText(getApplicationContext(), "Check Out Successful!", Toast.LENGTH_LONG);
                                            toast.show();

                                            mGeofencingClient.removeGeofences(PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.
                                                    FLAG_UPDATE_CURRENT));

                                            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean(Constants.SHARED_PREF_CHECKEDIN, false);
                                            editor.apply();

                                            NotificationUtils.remindUserAutoCheckOut(getApplicationContext(), sharedPreferences.getString(Constants.SHARED_PREF_EVENTDESCRIPTION,null), checkOutTime);

                                            Intent checkInIntent = new Intent(getApplicationContext(), CheckInActivity.class);
                                            startActivity(checkInIntent);


                                        }else{
                                            Toast toast = Toast.makeText(getApplicationContext(), "Check Out UnSuccessful! Please, try again.", Toast.LENGTH_LONG);
                                            toast.show();
                                        }

                                    }
                                });







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
