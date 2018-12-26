package com.ingenuityapps.android.therealimin.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Organization implements Parcelable {

    private String mOrganizationID;
    private String mDescription;
    private String mDomain;
    private Boolean mActive;

    public Organization() {
    }

    public Organization(String mOrganizationID, String mDescription, String mDomain, Boolean mActive) {
        this.mOrganizationID = mOrganizationID;
        this.mDescription = mDescription;
        this.mDomain = mDomain;
        this.mActive = mActive;
    }


    public String getOrganizationID() {
        return mOrganizationID;
    }

    public void setOrganizationID(String mOrganizationID) {
        this.mOrganizationID = mOrganizationID;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String mDomain) {
        this.mDomain = mDomain;
    }

    public Boolean getActive() {
        return mActive;
    }

    public void setActive(Boolean mActive) {
        this.mActive = mActive;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mOrganizationID);
        dest.writeString(this.mDescription);
        dest.writeString(this.mDomain);
        dest.writeValue(this.mActive);
    }

    protected Organization(Parcel in) {
        this.mOrganizationID = in.readString();
        this.mDescription = in.readString();
        this.mDomain = in.readString();
        this.mActive = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<Organization> CREATOR = new Parcelable.Creator<Organization>() {
        @Override
        public Organization createFromParcel(Parcel source) {
            return new Organization(source);
        }

        @Override
        public Organization[] newArray(int size) {
            return new Organization[size];
        }
    };
}
