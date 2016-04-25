package com.azoft.expandlayoutmanager.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 14.12.15
 * Time: 11:05
 *
 * @author Artem Zalevskiy
 */
public class CitiesResponse implements Serializable {

    private static final long serialVersionUID = -4851214841497912174L;
    @SerializedName("cities")
    private List<City> mCities;

    public List<City> getCities() {
        return mCities;
    }
}
