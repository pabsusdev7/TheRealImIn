package com.ingenuityapps.android.therealimin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.ingenuityapps.android.therealimin.utilities.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private FirebaseFirestore db;
    private String mDeviceID;
    private SharedPreferences mSharedPreferences;
    // Choose authentication providers
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build()
            //,new AuthUI.IdpConfig.GoogleBuilder().build()
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);

        loadDeviceInfo();

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setLogo(R.mipmap.ic_launcher)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(!user.isEmailVerified()){
                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Email sent.");
                                    }
                                }
                            });
                    Toast toast = Toast.makeText(getApplicationContext(), "We sent you an email verification. Please, confirm your email address before proceeding.", Toast.LENGTH_LONG);
                    toast.show();
                    signOut();
                    return;
                }

                if(mSharedPreferences.contains(Constants.SHARED_PREF_ORGDOMAIN) && !user.getEmail().split("@")[1].equals(mSharedPreferences.getString(Constants.SHARED_PREF_ORGDOMAIN,null)))
                {
                    Log.d(TAG,"User's email address domain: "+user.getEmail().split("@")[1]+" - Org Domain: "+mSharedPreferences.getString(Constants.SHARED_PREF_ORGDOMAIN,null));
                    Toast toast = Toast.makeText(getApplicationContext(), "The email address you registered with ("+ user.getEmail() +") does not match the organization's domain.", Toast.LENGTH_LONG);
                    toast.show();
                    signOut();
                    return;
                }

                CollectionReference deviceCollectionRef = db.collection(Constants.FIRESTORE_DEVICE);


                if(mDeviceID!=null) {

                    deviceCollectionRef
                            .whereEqualTo(Constants.FIRESTORE_DEVICE_IMEI, mDeviceID)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        if(!task.getResult().isEmpty())
                                        {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                if(!document.get(Constants.FIRESTORE_DEVICE_ATTENDEEID).equals(user.getUid())){
                                                    Log.w(TAG,"Somebody's got other's phone! Firebase User ID ("+user.getUid()+") does not match Device's registered User ID ("+document.get("attendeeid")+")");
                                                    Toast toast = Toast.makeText(getApplicationContext(), "This device is not registered for your account. Please, try again or use a different account.", Toast.LENGTH_LONG);
                                                    toast.show();
                                                    user.delete();
                                                    signOut();
                                                    return;
                                                }
                                                SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit();
                                                editor.putString("deviceid", document.getId());
                                                editor.apply();

                                            }

                                        }else{
                                            Map<String, Object> data = new HashMap<>();
                                            data.put(Constants.FIRESTORE_DEVICE_ATTENDEEID, user.getUid());
                                            data.put(Constants.FIRESTORE_DEVICE_IMEI, mDeviceID);


                                            db.collection(Constants.FIRESTORE_DEVICE)
                                                    .add(data)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            Toast toast = Toast.makeText(getApplicationContext(), "This device has been registered to your account!", Toast.LENGTH_LONG);
                                                            toast.show();
                                                            SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit();
                                                            editor.putString(Constants.SHARED_PREF_DEVICEID, documentReference.getId());
                                                            editor.apply();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e(TAG,"There was an issue registering this device to account");
                                                        }
                                                    });
                                        }

                                        Intent checkInActivityIntent = new Intent(getApplicationContext(), CheckInActivity.class);
                                        startActivity(checkInActivityIntent);

                                    }else{

                                        Log.d(TAG, "Error getting Device documents: ", task.getException());
                                        if(task.getException().getMessage().contains(Constants.ERR_PERMISSION_DENIED)) {
                                            Toast toast = Toast.makeText(getApplicationContext(), "The account you used does not belong to the selected organization. Please, try again.", Toast.LENGTH_LONG);
                                            toast.show();
                                            signOut();
                                        }
                                    }
                                }
                            });
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "There was an issue identifying your device. Please, try again.", Toast.LENGTH_LONG);
                    toast.show();
                    signOut();
                }

                // ...
            } else {
                if(response==null)
                    restartActivity();
            }
        }
    }

    private void loadDeviceInfo() {

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            mDeviceID = !checkDeviceInfoPermission() ? null : telephonyManager.getDeviceId();
        }
        else {

            mDeviceID = telephonyManager.getDeviceId();

            Log.d(TAG,"Device's ID = " + mDeviceID);
        }


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
                                ActivityCompat.requestPermissions(LoginActivity.this,
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

    private void signOut()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            restartActivity();
                        }
                    }
                });
    }

    private void restartActivity() {

        Intent logInActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(logInActivityIntent);
    }

}
