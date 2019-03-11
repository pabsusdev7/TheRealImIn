package com.ingenuityapps.android.therealimin.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.Comparator;
import java.util.Date;

public class CheckIn implements Parcelable {

    private String mCheckID;
    private Event mEvent;
    private Timestamp mCheckInTime;
    private Timestamp mCheckOutTime;


    public CheckIn(){}

    public CheckIn(String checkInID, Event event, Timestamp checkInTime, Timestamp checkOutTime){
        mCheckID = checkInID;
        mEvent = event;
        mCheckInTime = checkInTime;
        mCheckOutTime = checkOutTime;
    }


    public String getCheckID() {
        return mCheckID;
    }

    public void setCheckID(String mCheckID) {
        this.mCheckID = mCheckID;
    }

    public Event getEvent() {
        return mEvent;
    }

    public void setEvent(Event mEvent) {
        this.mEvent = mEvent;
    }

    public Timestamp getCheckInTime() {
        return mCheckInTime;
    }

    public void setCheckInTime(Timestamp mCheckInTime) {
        this.mCheckInTime = mCheckInTime;
    }

    public Timestamp getCheckOutTime() {
        return mCheckOutTime;
    }

    public void setCheckOutTime(Timestamp mCheckOutTime) {
        this.mCheckOutTime = mCheckOutTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCheckID);
        dest.writeParcelable(this.mEvent, flags);
        dest.writeLong(mCheckInTime.toDate().getTime());
        dest.writeLong(mCheckOutTime != null ? mCheckOutTime.toDate().getTime() : 0);
    }

    protected CheckIn(Parcel in) {
        this.mCheckID = in.readString();
        this.mEvent = in.readParcelable(Event.class.getClassLoader());
        this.mCheckInTime = new Timestamp(new Date(in.readLong()));
        this.mCheckOutTime = new Timestamp(new Date(in.readLong()));
    }

    public static final Parcelable.Creator<CheckIn> CREATOR = new Parcelable.Creator<CheckIn>() {
        @Override
        public CheckIn createFromParcel(Parcel source) {
            return new CheckIn(source);
        }

        @Override
        public CheckIn[] newArray(int size) {
            return new CheckIn[size];
        }
    };
}
