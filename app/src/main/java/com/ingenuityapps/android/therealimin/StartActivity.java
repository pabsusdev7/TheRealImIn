package com.ingenuityapps.android.therealimin;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.iv_company_logo)
    ImageView companyLogo;
    @BindView(R.id.tv_app_title)
    TextView appTitle;
    @BindView(R.id.tv_app_slogan)
    TextView appSlogan;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mUser!=null){
            Intent intent = new Intent(this, CheckInActivity.class);
            startActivity(intent);
            return;
        }

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        loadLogos();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent organizationActivityIntent = new Intent(StartActivity.this, OrganizationActivity.class);
                startActivity(organizationActivityIntent);
                finish();
            }
        },Constants.START_ACTIVITY_TIME_OUT);
    }

    private void loadLogos() {

        String imageUrl = String.format(getResources().getString(R.string.org_logo_url),Constants.COMPANY_LOGO);

        Picasso.with(this)
                .load(R.drawable.company_logo)
                .into(companyLogo);

        final Runnable r = new Runnable()
        {
            public void run()
            {
                Picasso.with(getApplicationContext())
                        .load(R.mipmap.ic_launcher_foreground)
                        .into(companyLogo);
                appTitle.setVisibility(View.VISIBLE);
                appSlogan.setVisibility(View.VISIBLE);

            }
        };

        companyLogo.postDelayed(r, Constants.START_ACTIVITY_TIME_OUT / 3);


    }
}
