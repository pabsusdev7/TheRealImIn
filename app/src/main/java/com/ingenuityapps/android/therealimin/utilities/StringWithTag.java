package com.ingenuityapps.android.therealimin.utilities;

public class StringWithTag {

    public String string;
    public Object tag;

    public StringWithTag(String string, Object tag) {
        this.string = string;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return string;
    }
}
