package com.ingenuityapps.android.therealimin.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.math.BigDecimal;

public class Location implements Parcelable {


    private String mLocationID;
    private String mDescription;
    private GeoPoint mGeoLocation;
    private Float mLocationRadius;
    private DocumentReference mDocumentReference;

    public Location(){}

    public Location(String locationID, String description, GeoPoint geoLocation, Float locationRadius)
    {
        mLocationID = locationID;
        mDescription = description;
        mGeoLocation = geoLocation;
        mLocationRadius = locationRadius;
    }

    public String getLocationID() {
        return mLocationID;
    }

    public void setLocationID(String mLocationID) {
        this.mLocationID = mLocationID;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public Float getRadius() {
        return mLocationRadius;
    }

    public void setRadius(Float mLocationRadius) {
        this.mLocationRadius = mLocationRadius;
    }

    public GeoPoint getGeolocation() {
        return mGeoLocation;
    }

    public void setGeolocation(GeoPoint mGeoLocation) {
        this.mGeoLocation = mGeoLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLocationID);
        dest.writeString(this.mDescription);
        //dest.writeParcelable((Parcelable) this.mGeoLocation, flags);
        dest.writeDouble(this.mGeoLocation.getLatitude());
        dest.writeDouble(this.mGeoLocation.getLongitude());
        dest.writeValue(this.mLocationRadius);
        dest.writeParcelable((Parcelable) this.mDocumentReference, flags);
    }

    protected Location(Parcel in) {
        this.mLocationID = in.readString();
        this.mDescription = in.readString();
        //this.mGeoLocation = in.readParcelable(GeoPoint.class.getClassLoader());
        Double lat = in.readDouble();
        Double lng = in.readDouble();
        this.mGeoLocation = new GeoPoint(lat, lng);
        this.mLocationRadius = (Float) in.readValue(Float.class.getClassLoader());
        this.mDocumentReference = in.readParcelable(DocumentReference.class.getClassLoader());
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public DocumentReference getDocumentreference() {
        return mDocumentReference;
    }

    public void setDocumentreference(DocumentReference mDocumentReference) {
        this.mDocumentReference = mDocumentReference;
    }
}
