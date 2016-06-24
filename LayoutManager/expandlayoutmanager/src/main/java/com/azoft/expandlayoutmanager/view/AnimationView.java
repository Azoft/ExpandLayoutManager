package com.azoft.expandlayoutmanager.view;

import android.view.ViewGroup;

import com.azoft.expandlayoutmanager.ExpandLayoutManager;

public interface AnimationView {

    /**
     * Reset view to the default state. Make all animated views GONE in this method.
     *
     * @return true if visibility of any view was change or remeasuring needed
     */
    boolean resetState();

    ViewGroup.LayoutParams getLayoutParams();

    /**
     * Helper method for setting app any custom data and ability to call layoutManager methods from the view
     *
     * @param expandLayoutManager current expand layout manager
     */
    void setExpandLayoutManager(ExpandLayoutManager expandLayoutManager);

    /**
     * Perform expand animation with animationProgress completion.
     * animationProgress is a float from 0 to 1.
     * 0 means that the expand animation is only started
     * 1 means that the expand animation is finished
     *
     * @return true if visibility of any view was change or remeasuring needed
     */
    boolean doExpandAnimation(float animationProgress);

    /**
     * Perform collapse animation with animationProgress completion.
     * animationProgress is a float from 0 to 1.
     * 0 means that the collapse animation is only started
     * 1 means that the collapse animation is finished
     *
     * @return true if visibility of any view was change or remeasuring needed
     */
    boolean doCollapseAnimation(float animationProgress);
}