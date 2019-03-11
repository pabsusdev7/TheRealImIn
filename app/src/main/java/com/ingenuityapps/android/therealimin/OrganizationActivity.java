package com.ingenuityapps.android.therealimin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.ingenuityapps.android.therealimin.data.Event;
import com.ingenuityapps.android.therealimin.data.Organization;
import com.ingenuityapps.android.therealimin.utilities.Constants;
import com.ingenuityapps.android.therealimin.utilities.StringWithTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrganizationActivity extends AppCompatActivity {

    private static final String TAG = OrganizationActivity.class.getSimpleName();

    private FirebaseFirestore db;
    private ArrayAdapter<StringWithTag> mOrganizationsAdapter;
    private Map<String, Organization> mOrganizations;
    private SharedPreferences mSharedPreferences;
    private String mDeviceID;

    @BindView(R.id.org_spinner)
    Spinner mOrgSpinner;
    @BindView(R.id.btn_next)
    Button mNextButon;
    @BindView(R.id.tv_empty_message_display)
    TextView mEmptyMessage;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessage;
    @BindView(R.id.org_container)
    View mOrgInfoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        String orgId = mSharedPreferences.getString(Constants.SHARED_PREF_ORGID,null);

        loadDeviceInfo();

        if(mSharedPreferences.contains(Constants.SHARED_PREF_ORGID) && orgId!=null){
            goToLogIn();
            return;
        }


        setContentView(R.layout.activity_organization);
        ButterKnife.bind(this);

        mOrganizations = new HashMap<String, Organization>();
        db = FirebaseFirestore.getInstance();


        mNextButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Organization organization = mOrganizations.get(((StringWithTag) mOrgSpinner.getSelectedItem()).tag);
                if (organization != null) {

                    SharedPreferences.Editor editor = mSharedPreferences.edit();


                    editor.putString(Constants.SHARED_PREF_ORGID,organization.getOrganizationID());
                    editor.putString(Constants.SHARED_PREF_ORGDOMAIN,organization.getDomain());
                    editor.apply();
                    goToLogIn();

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please, select an organization", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });

        loadOrganizations();
    }

    private void goToLogIn() {

        Intent logInActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(logInActivityIntent);
    }


    private void loadOrganizations() {

        db.collection(Constants.FIRESTORE_ORGANIZATION)
                .whereEqualTo(Constants.FIRESTORE_ORGANIZATION_ACTIVE,true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<StringWithTag> orgs = new ArrayList<>();
                            orgs.add(new StringWithTag("Select Your Organization", 0));
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Organization organization = new Organization(document.getId(), document.getString(Constants.FIRESTORE_ORGANIZATION_DESCRIPTION), document.getString(Constants.FIRESTORE_ORGANIZATION_DOMAIN), document.getBoolean(Constants.FIRESTORE_ORGANIZATION_ACTIVE));
                                //Organization organization = document.toObject(Organization.class);


                                orgs.add(new StringWithTag(organization.getDescription(), organization.getOrganizationID()));
                                mOrganizations.put(organization.getOrganizationID(), organization);



                            }

                            if (orgs != null) {
                                mOrganizationsAdapter = new ArrayAdapter<StringWithTag>(getBaseContext(), android.R.layout.simple_spinner_item, orgs);
                                mOrganizationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mOrgSpinner.setAdapter(mOrganizationsAdapter);
                                if(!mOrgSpinner.getAdapter().isEmpty())
                                    showOrgInfoDataView();
                                else{
                                    showNoResultsMessage();
                                }
                            } else {
                                showErrorMessage();
                            }
                        } else {
                            showErrorMessage();
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    private void loadDeviceInfo() {

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            mDeviceID = !checkDeviceInfoPermission() ? null : telephonyManager.getDeviceId();
        }
        else {

            mDeviceID = telephonyManager.getDeviceId();

        }


        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.SHARED_PREF_DEVICEID, mDeviceID);
        editor.apply();


    }

    private boolean checkDeviceInfoPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_device_permission)
                        .setMessage(R.string.text_device_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(OrganizationActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        Constants.PERMISSIONS_REQUEST_PHONE_STATE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        Constants.PERMISSIONS_REQUEST_PHONE_STATE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    loadDeviceInfo();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }

    private void showNoResultsMessage() {
        mOrgInfoContainer.setVisibility(View.INVISIBLE);
        mEmptyMessage.setVisibility(View.VISIBLE);
    }

    private void showOrgInfoDataView() {
        mOrgInfoContainer.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage() {
        mOrgInfoContainer.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }
}
