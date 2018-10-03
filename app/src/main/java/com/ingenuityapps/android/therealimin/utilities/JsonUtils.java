package com.ingenuityapps.android.therealimin.utilities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pabloalbuja on 5/21/18.
 */

public class JsonUtils {


    public static String createCheckInOutJson(String deviceID, String eventID, String dt, String type) throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("checkintime",dt);
        json.put("checktype", type);
        json.put("eventid", eventID);
        json.put("deviceid",deviceID);

        return json.toString();
    }
}
