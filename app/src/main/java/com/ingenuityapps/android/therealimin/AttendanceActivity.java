package com.ingenuityapps.android.therealimin;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.GravityCompat;
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

import com.ingenuityapps.android.therealimin.data.AttendanceAdapter;
import com.ingenuityapps.android.therealimin.data.Event;
import com.ingenuityapps.android.therealimin.utilities.NetworkUtils;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity implements AttendanceAdapter.AttendanceAdapterOnClickHandler {

    private static final String TAG = AttendanceActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;
    private AttendanceAdapter mAttendanceAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

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

        new FetchAttendanceTask().execute("1");

    }

    @Override
    public void onClick(String attendanceForEvent) {
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
                mAttendanceAdapter.setmAttendanceData(attendanceData);
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

        Context context = getApplicationContext();
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
        }

        return super.onOptionsItemSelected(item);
    }
}
