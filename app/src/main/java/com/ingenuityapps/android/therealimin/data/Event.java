package com.ingenuityapps.android.therealimin.data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pabloalbuja on 6/2/18.
 */

public class Event{

    private Integer mEventID;
    private String mDescription;
    private String mLocation;
    private BigDecimal mLocationLong;
    private BigDecimal mLocationLat;
    private BigDecimal mLocationRadius;
    private Date mStartTime;
    private Date mEndTime;

    public Event()
    {}

    public Event(Integer eventID, String description, String location, BigDecimal locationLong, BigDecimal locationLat, BigDecimal locationRadius, Date startTime, Date endTime)
    {
        setmEventID(eventID);
        setmDescription(description);
        setmLocation(location);
        setmLocationLong(locationLong);
        setmLocationLat(locationLat);
        setmLocationRadius(locationRadius);
        setmStartTime(startTime);
        setmEndTime(endTime);
    }

    public Integer getmEventID() {
        return mEventID;
    }

    public void setmEventID(Integer mEventID) {
        this.mEventID = mEventID;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmLocation() {
        return mLocation;
    }

    public void setmLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public Date getmStartTime() {
        return mStartTime;
    }

    public void setmStartTime(Date mStartTime) {
        this.mStartTime = mStartTime;
    }

    public Date getmEndTime() {
        return mEndTime;
    }

    public void setmEndTime(Date mEndTime) {
        this.mEndTime = mEndTime;
    }

    public BigDecimal getmLocationLong() {
        return mLocationLong;
    }

    public void setmLocationLong(BigDecimal mLocationLong) {
        this.mLocationLong = mLocationLong;
    }

    public BigDecimal getmLocationLat() {
        return mLocationLat;
    }

    public void setmLocationLat(BigDecimal mLocationLat) {
        this.mLocationLat = mLocationLat;
    }

    public BigDecimal getmLocationRadius() {
        return mLocationRadius;
    }

    public void setmLocationRadius(BigDecimal mLocationRadius) {
        this.mLocationRadius = mLocationRadius;
    }
}
