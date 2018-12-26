package com.ingenuityapps.android.therealimin.utilities;

/**
 * Created by pabloalbuja on 5/24/18.
 */

public final class Constants {

    public static final Integer GEOFENCE_EXPIRATION_IN_MILLISECONDS = 1000000;
    public static final int PERMISSIONS_REQUEST_LOCATION = 101;
    public static final int PERMISSIONS_REQUEST_PHONE_STATE= 102;
    public static final Integer MAX_MINUTES_CHECKIN_BEFOREEVENT = 15;
    public static final long ONE_MINUTE_IN_MILLIS=60000;
    public static final String SHARED_PREFS = "shared_preferences";
    public static final String SHARED_PREF_DEVICEID = "deviceid";
    public static final String SHARED_PREF_CHECKEDIN = "checkedin";
    public static final String SHARED_PREF_CHECKINID = "checkinid";
    public static final String SHARED_PREF_EVENTID = "eventid";
    public static final String SHARED_PREF_EVENTENDTIME = "eventendtime";
    public static final String SHARED_PREF_ORGID = "orgid";

    //FIRESTORE
    public static final String FIRESTORE_CHECKIN = "checkin";
    public static final String FIRESTORE_CHECKIN_CHECKINTIME = "checkintime";
    public static final String FIRESTORE_CHECKIN_CHECKOUTTIME = "checkouttime";
    public static final String FIRESTORE_CHECKIN_DEVICEID = "deviceid";
    public static final String FIRESTORE_CHECKIN_EVENTID = "eventid";
    public static final String FIRESTORE_DEVICE = "device";
    public static final String FIRESTORE_DEVICE_ATTENDEEID = "attendeeid";
    public static final String FIRESTORE_DEVICE_IMEI = "imei";
    public static final String FIRESTORE_EVENT = "event";
    public static final String FIRESTORE_EVENT_DESCRIPTION = "description";
    public static final String FIRESTORE_EVENT_STARTTIME = "starttime";
    public static final String FIRESTORE_EVENT_ENDTIME = "endtime";
    public static final String FIRESTORE_EVENT_REQUIRED = "required";
    public static final String FIRESTORE_EVENT_LOCATIONID = "locationid";
    public static final String FIRESTORE_EVENT_ACTIVE = "active";
    public static final String FIRESTORE_EVENT_ORGID = "orgid";
    public static final String FIRESTORE_EVENTCATEGORY = "eventcategory";
    public static final String FIRESTORE_EVENTCATEGORY_DESCRIPTION = "description";
    public static final String FIRESTORE_LOCATION= "location";
    public static final String FIRESTORE_LOCATION_DESCRIPTION= "description";
    public static final String FIRESTORE_LOCATION_GEOLOCATION= "geolocation";
    public static final String FIRESTORE_LOCATION_RADIUS= "radius";
    public static final String FIRESTORE_ORGANIZATION= "organization";
    public static final String FIRESTORE_ORGANIZATION_DESCRIPTION= "description";
    public static final String FIRESTORE_ORGANIZATION_DOMAIN= "domain";
    public static final String FIRESTORE_ORGANIZATION_ACTIVE= "active";

    //Errors
    public static final String ERR_PERMISSION_DENIED = "PERMISSION_DENIED";
}
