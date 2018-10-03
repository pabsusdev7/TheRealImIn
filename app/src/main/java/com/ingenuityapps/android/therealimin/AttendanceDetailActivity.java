package com.ingenuityapps.android.therealimin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ingenuityapps.android.therealimin.databinding.ActivityAttendanceDetailBinding;
import com.ingenuityapps.android.therealimin.utilities.LocationUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class AttendanceDetailActivity extends AppCompatActivity {

    private ActivityAttendanceDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher_imin2_round);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_attendance_detail);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {

                String intentData = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
                try {
                    String[] intentDataList = intentData.split(";");

                    for(String record:intentDataList)
                    {
                        final String[] recordFields = record.split("\\|");
                        if(recordFields.length==9)
                        {
                            SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
                            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                            SimpleDateFormat checkInTimeFormatter = new SimpleDateFormat("hh:mm:ss a");
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy");
                            mDetailBinding.primaryInfo.event.setText(recordFields[1]);
                            mDetailBinding.primaryInfo.date.setText(dateFormatter.format(formatter.parse(recordFields[2])));
                            mDetailBinding.primaryInfo.locationMap.setText(recordFields[4]);
                            mDetailBinding.primaryInfo.locationDescription.setText(getResources().getString(R.string.show_directions));
                            mDetailBinding.primaryInfo.locationDescription.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LocationUtils.openLocationInMap(getApplicationContext(),new BigDecimal(recordFields[5]), new BigDecimal(recordFields[6]));
                                }
                            });
                            mDetailBinding.primaryInfo.startTime.setText(timeFormatter.format(formatter.parse(recordFields[2])));
                            mDetailBinding.primaryInfo.endTime.setText(timeFormatter.format(formatter.parse(recordFields[3])));

                            mDetailBinding.extraDetails.checkin.setText(checkInTimeFormatter.format(formatter.parse(recordFields[7])));
                            mDetailBinding.extraDetails.checkout.setText(!recordFields[8].equals("0") ? checkInTimeFormatter.format(formatter.parse(recordFields[8])) : getResources().getString(R.string.no_data));
                        }
                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            }
        }


    }
}
