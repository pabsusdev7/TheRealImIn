package com.ingenuityapps.android.therealimin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.firestore.GeoPoint;
import com.ingenuityapps.android.therealimin.data.CheckIn;
import com.ingenuityapps.android.therealimin.databinding.ActivityAttendanceDetailBinding;
import com.ingenuityapps.android.therealimin.utilities.LocationUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class AttendanceDetailActivity extends AppCompatActivity {

    private static final String TAG = AttendanceDetailActivity.class.getSimpleName();
    private ActivityAttendanceDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_round);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_attendance_detail);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {

                final CheckIn intentData = intentThatStartedThisActivity.getParcelableExtra(Intent.EXTRA_TEXT);
                try {

                    SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat checkInTimeFormatter = new SimpleDateFormat("hh:mm:ss a");
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy");
                    mDetailBinding.primaryInfo.event.setText(intentData.getEvent().getDescription());
                    mDetailBinding.primaryInfo.date.setText(dateFormatter.format(formatter.parse(intentData.getEvent().getStarttime().toDate().toString())));
                    mDetailBinding.primaryInfo.locationMap.setText(intentData.getEvent().getLocation().getDescription());
                    mDetailBinding.primaryInfo.locationDescription.setText(getResources().getString(R.string.show_directions));
                    mDetailBinding.primaryInfo.locationDescription.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LocationUtils.openLocationInMap(getApplicationContext(),intentData.getEvent().getLocation().getGeolocation(), intentData.getEvent().getLocation().getDescription());
                        }
                    });
                    mDetailBinding.primaryInfo.startTime.setText(timeFormatter.format(formatter.parse(intentData.getEvent().getStarttime().toDate().toString())));
                    mDetailBinding.primaryInfo.endTime.setText(timeFormatter.format(formatter.parse(intentData.getEvent().getEndtime().toDate().toString())));
                    mDetailBinding.primaryInfo.tvRequired.setVisibility(intentData.getEvent().getRequired()?View.VISIBLE:View.INVISIBLE);

                    mDetailBinding.extraDetails.checkin.setText(checkInTimeFormatter.format(formatter.parse(intentData.getCheckInTime().toDate().toString())));
                    mDetailBinding.extraDetails.checkout.setText((intentData.getCheckOutTime().toDate().getTime() > 0) ? checkInTimeFormatter.format(formatter.parse(intentData.getCheckOutTime().toDate().toString())) : getResources().getString(R.string.no_data));

                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
