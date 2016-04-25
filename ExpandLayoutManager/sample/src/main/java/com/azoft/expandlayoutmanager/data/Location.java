package com.azoft.expandlayoutmanager.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Date: 14.12.15
 * Time: 11:21
 *
 * @author Artem Zalevskiy
 */
public class Location implements Serializable{

    private static final long serialVersionUID = 6735965448501650589L;
    @SerializedName("latitude")
    private double mLatitude;
    @SerializedName("longitude")
    private double mLongitude;

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(final double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(final double longitude) {
        mLongitude = longitude;
    }
}
