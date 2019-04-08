package com.ingenuityapps.android.therealimin.data;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pabloalbuja on 6/2/18.
 */

public class Event implements Parcelable {

    private String mEventID;
    private String mDescription;
    private Timestamp mStartTime;
    private Timestamp mEndTime;
    private Location mLocation;
    private Boolean mRequired;

    public Event()
    {}

    public Event(String eventID, String description, Timestamp startTime, Timestamp endTime, Location location)
    {
        setEventID(eventID);
        setDescription(description);
        setStarttime(startTime);
        setEndtime(endTime);
        setLocation(location);
    }

    public Event(String eventID, String description, Timestamp startTime, Timestamp endTime, Boolean required)
    {
        setEventID(eventID);
        setDescription(description);
        setStarttime(startTime);
        setEndtime(endTime);
        setRequired(required);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Event(Parcel in)
    {
        mEventID = in.readString();
        mDescription = in.readString();
        mStartTime = new Timestamp(new Date(in.readLong()));
        mEndTime = new Timestamp(new Date(in.readLong()));
        //mLocation = in.readTypedObject(Location.CREATOR);
        mLocation = in.readParcelable(Location.class.getClassLoader());
        mRequired = in.readInt() > 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public String getEventID() {
        return mEventID;
    }

    public void setEventID(String mEventID) {
        this.mEventID = mEventID;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public Timestamp getStarttime() {
        return mStartTime;
    }

    public void setStarttime(Timestamp mStartTime) {
        this.mStartTime = mStartTime;
    }

    public Timestamp getEndtime() {
        return mEndTime;
    }

    public void setEndtime(Timestamp mEndTime) {
        this.mEndTime = mEndTime;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mEventID);
        dest.writeString(mDescription);
        dest.writeLong(mStartTime.toDate().getTime());
        dest.writeLong(mEndTime.toDate().getTime());
        //dest.writeTypedObject(mLocation, flags);
        dest.writeParcelable(mLocation, flags);
        dest.writeInt(mRequired ? 1 : 0);

    }

    public Boolean getRequired() {
        return mRequired;
    }

    public void setRequired(Boolean mRequired) {
        this.mRequired = mRequired;
    }
}
