package com.ingenuityapps.android.therealimin;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.ingenuityapps.android.therealimin.utilities.JsonUtils;
import com.ingenuityapps.android.therealimin.utilities.NetworkUtils;

import java.net.URL;
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
    protected void onHandleIntent(Intent intent) {

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
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            Log.v(TAG, "GeoFence Transition: " + geofenceTransition);
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            if(intent!=null)
            {
                if(intent.hasExtra("eventID") && intent.hasExtra("deviceID"))
                {
                    Integer event = intent.getIntExtra("eventID", 0);
                    Integer device = intent.getIntExtra("deviceID",0);
                    try {
                        String checkOutJson = JsonUtils.createCheckInOutJson(device.toString(), event.toString(), null, Constants.CHECK_OUT);
                        new PostCheckOutTask().execute(checkOutJson);
                        mGeofencingClient.removeGeofences(PendingIntent.getService(this, 0, intent, PendingIntent.
                                FLAG_UPDATE_CURRENT));

                        Intent checkInIntent = new Intent(this, CheckInActivity.class);
                        startActivity(checkInIntent);
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }

            // Get the transition details as a String.
            /*String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );*/

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            //Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            /*Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));*/
        }
    }

    public class PostCheckOutTask extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... jsonParams) {

            URL checkinURL = NetworkUtils.buildUrl("checkins");

            try {
                Boolean jsonResponse = NetworkUtils.getPOSTResponseFromHttpUrl(checkinURL, jsonParams[0]);
                return jsonResponse;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Boolean responseCode) {

            if (responseCode) {
                Toast toast = Toast.makeText(getApplicationContext(), "Check Out Successful!", Toast.LENGTH_LONG);
                toast.show();

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Check Out UnSuccessful! Please, try again.", Toast.LENGTH_LONG);
                toast.show();

            }


        }
    }
}
