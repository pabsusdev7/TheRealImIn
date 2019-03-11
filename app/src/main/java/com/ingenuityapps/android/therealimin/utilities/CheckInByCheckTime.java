package com.ingenuityapps.android.therealimin.utilities;

import com.ingenuityapps.android.therealimin.data.CheckIn;

import java.util.Comparator;

public class CheckInByCheckTime implements Comparator<CheckIn> {
    @Override
    public int compare(CheckIn x, CheckIn y) {
        // TODO: Handle null x or y values
        int startComparison = compare(x.getCheckInTime().getSeconds(), y.getCheckInTime().getSeconds());
        return startComparison != 0 ? startComparison
                : compare(x.getCheckOutTime().getSeconds(), y.getCheckOutTime().getSeconds());
    }

    private static int compare(long a, long b) {
        return a < b ? -1
                : a > b ? 1
                : 0;
    }
}
