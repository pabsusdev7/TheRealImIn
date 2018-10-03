package com.ingenuityapps.android.therealimin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CheckedInActivity extends AppCompatActivity {

    private TextView mChekedInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checked_in);

        mChekedInTextView = (TextView) findViewById(R.id.tv_checked_in);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("event") && intentThatStartedThisActivity.hasExtra("endtime")) {
                mChekedInTextView.setText("You are currently checked in for " + intentThatStartedThisActivity.getStringExtra("event") + ". This event ends at " + intentThatStartedThisActivity.getStringExtra("endtime"));
            }
        }
    }
}
