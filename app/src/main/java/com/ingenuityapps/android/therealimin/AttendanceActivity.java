package com.ingenuityapps.android.therealimin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ingenuityapps.android.therealimin.data.AttendanceAdapter;
import com.ingenuityapps.android.therealimin.data.CheckIn;
import com.ingenuityapps.android.therealimin.data.Event;
import com.ingenuityapps.android.therealimin.data.Location;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.ingenuityapps.android.therealimin.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class AttendanceActivity extends AppCompatActivity implements AttendanceAdapter.AttendanceAdapterOnClickHandler {

    private static final String TAG = AttendanceActivity.class.getSimpleName();

    private FirebaseFirestore db;

    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;
    private AttendanceAdapter mAttendanceAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private String mDeviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);

        mDeviceID = prefs.getString("deviceid",null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_imin2_round);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_attendance);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());

        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        mAttendanceAdapter = new AttendanceAdapter(this);

        mRecyclerView.setAdapter(mAttendanceAdapter);

        loadAttendanceData();

    }

    private void loadAttendanceData() {

        showAttendanceDataView();

        if(mDeviceID!=null) {

            db.collection("checkin")
                    .whereEqualTo("deviceid", db.collection("device").document(mDeviceID))
                    .get()
                    .continueWith(new Continuation<QuerySnapshot, List<CheckIn>>() {

                        @Override
                        public List<CheckIn> then(@NonNull Task<QuerySnapshot> task) throws Exception {

                            final List<CheckIn> checkIns = new ArrayList<>();

                            if (task.isSuccessful()) {


                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());

                                    final CheckIn checkIn = new CheckIn();

                                    checkIn.setCheckID(document.getId());
                                    checkIn.setCheckInTime(document.getTimestamp("checkintime"));
                                    checkIn.setCheckOutTime(document.get("checkouttime") != null ? document.getTimestamp("checkouttime") : null);
                                    Event event = new Event();
                                    event.setDocumenteference(document.getDocumentReference("eventid"));

                                    checkIn.setEvent(event);


                                    checkIns.add(checkIn);

                                }


                            } else {
                                showErrorMessage();
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }

                            return checkIns;
                        }
                    })
                    .continueWith(new Continuation<List<CheckIn>, List<CheckIn>>() {
                        @Override
                        public List<CheckIn> then(@NonNull Task<List<CheckIn>> task) throws Exception {

                            final List<CheckIn> checkIns = task.getResult();

                            for (final CheckIn checkIn : checkIns) {
                                checkIn.getEvent().getDocumentreference().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());

                                        final Event event = new Event(documentSnapshot.getId(), documentSnapshot.get("description").toString(), documentSnapshot.getTimestamp("starttime"), documentSnapshot.getTimestamp("endtime"));

                                        DocumentReference locationRef = documentSnapshot.getDocumentReference("locationid");

                                        locationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());

                                                Location location = documentSnapshot.toObject(Location.class);

                                                event.setLocation(location);


                                            }
                                        });

                                        checkIn.setEvent(event);

                                        //Making sure we reach the end of the list before setting the attendance recyclerview adapter
                                        if (checkIns.lastIndexOf(checkIn) == checkIns.size() - 1) {
                                            Log.d(TAG, "Setting up Attendance Adapter - CheckIn(0): " + checkIns.get(0).getCheckInTime().toDate() + " - Event: " + checkIns.get(0).getEvent().getDescription() + " - " + (checkIns.get(0).getEvent().getStarttime() != null ? checkIns.get(0).getEvent().getStarttime() : ""));
                                            mAttendanceAdapter.setmAttendanceData(checkIns);
                                        }
                                    }
                                });

                            }

                            return checkIns;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<List<CheckIn>>() {
                @Override
                public void onSuccess(List<CheckIn> checkIns) {
                    showAttendanceDataView();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showErrorMessage();
                }
            });
        }




    }

    @Override
    public void onClick(CheckIn attendanceForEvent) {
        Context context = this;
        Class destinationClass = AttendanceDetailActivity.class;

        Intent intent = new Intent(context, destinationClass);
        intent.putExtra(Intent.EXTRA_TEXT, attendanceForEvent);
        startActivity(intent);

    }

    private void showAttendanceDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class FetchAttendanceTask extends AsyncTask<String, Void, String[]>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }


        @Override
        protected String[] doInBackground(String... strings) {

            if(strings.length==0)
            {
                return null;
            }

            String deviceID = strings[0];
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("device",deviceID);
            URL attendanceRequestUrl = NetworkUtils.buildUrl("checkins",params, "attendance");

            try
            {
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(attendanceRequestUrl);
                Log.v(TAG, "jsonResponse: " + jsonResponse);

                String[] attendanceData = jsonResponse.split(";");

                return attendanceData;

                /*SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");

                for (String checkin : jsonResponse.split(";")) {

                    String[] checkinFields = checkin.split("\\|");
                    if (checkinFields.length > 0 && checkinFields.length == 5) {


                    }

                }*/

            }catch (Exception ex)
            {
                ex.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] attendanceData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if(attendanceData!=null)
            {
                showAttendanceDataView();
                //mAttendanceAdapter.setmAttendanceData(attendanceData);
            }
            else
            {
                showErrorMessage();
            }
        }
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

        switch (item.getItemId())
        {
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
}
