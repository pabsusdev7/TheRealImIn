package com.ingenuityapps.android.therealimin.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.GeoPoint;
import com.ingenuityapps.android.therealimin.CheckInActivity;

import java.math.BigDecimal;


/**
 * Created by pabloalbuja on 6/6/18.
 */

public class LocationUtils {

    private static final String TAG = LocationUtils.class.getSimpleName();

    public static void openLocationInMap(Context context, GeoPoint location, String label) {
        //Uri geoLocation = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude());geo:0,0?q=-33.8666,151.1957(Google+Sydney)

        Uri geoLocation = Uri.parse("geo:0,0?q="+ location.getLatitude() +","+ location.getLongitude() +"("+ label +")");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }
    }
}
