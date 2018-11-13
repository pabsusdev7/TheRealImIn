package com.ingenuityapps.android.therealimin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AttendanceActivity extends AppCompatActivity implements AttendanceAdapter.AttendanceAdapterOnClickHandler {

    private static final String TAG = AttendanceActivity.class.getSimpleName();

    private FirebaseFirestore db;

    @BindView(R.id.rv_attendance)
    RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;
    private AttendanceAdapter mAttendanceAdapter;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar pb_loading_indicator;
    @BindView(R.id.tv_empty_message_display)
    TextView mEmptyMessageDisplay;

    private String mDeviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        ButterKnife.bind(this);

        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);

        mDeviceID = prefs.getString(Constants.SHARED_PREF_DEVICEID,null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_imin2_round);


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

            db.collection(Constants.FIRESTORE_CHECKIN)
                    .whereEqualTo(Constants.FIRESTORE_CHECKIN_DEVICEID, db.collection(Constants.FIRESTORE_DEVICE).document(mDeviceID))
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
                                    checkIn.setCheckInTime(document.getTimestamp(Constants.FIRESTORE_CHECKIN_CHECKINTIME));
                                    checkIn.setCheckOutTime(document.get(Constants.FIRESTORE_CHECKIN_CHECKOUTTIME) != null ? document.getTimestamp(Constants.FIRESTORE_CHECKIN_CHECKOUTTIME) : null);
                                    Event event = new Event();
                                    event.setEventID(document.getDocumentReference(Constants.FIRESTORE_CHECKIN_EVENTID).getId());

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
                                db.collection(Constants.FIRESTORE_EVENT).document(checkIn.getEvent().getEventID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());

                                        final Event event = new Event(documentSnapshot.getId(), documentSnapshot.get(Constants.FIRESTORE_EVENT_DESCRIPTION).toString(), documentSnapshot.getTimestamp(Constants.FIRESTORE_EVENT_STARTTIME), documentSnapshot.getTimestamp(Constants.FIRESTORE_EVENT_ENDTIME), documentSnapshot.getBoolean(Constants.FIRESTORE_EVENT_REQUIRED));

                                        DocumentReference locationRef = documentSnapshot.getDocumentReference(Constants.FIRESTORE_EVENT_LOCATIONID);

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
                            if(!checkIns.isEmpty()){
                                showAttendanceDataView();
                            }else{
                                showNoResultsMessage();

                            }


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

    private void showNoResultsMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyMessageDisplay.setVisibility(View.VISIBLE);
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
