package com.azoft.expandlayoutmanager.data;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class City implements Serializable {

    private static final long serialVersionUID = -3017239982553398201L;
    @SerializedName("id")
    public long mId;
    @SerializedName("name")
    public String mName;
    @SerializedName("location")
    public Location mLocation;
    @SerializedName("image_url")
    public String mImageUrl;
    @SerializedName("description")
    public String mDescription;

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Location getLocation() {
        return mLocation;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getDescription() {
        return mDescription;
    }
}
