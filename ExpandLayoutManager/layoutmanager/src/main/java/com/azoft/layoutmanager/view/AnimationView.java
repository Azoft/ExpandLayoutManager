package com.azoft.layoutmanager.view;

import android.view.ViewGroup;

import com.azoft.layoutmanager.ExpandLayoutManager;

public interface AnimationView {

    boolean resetState();

    ViewGroup.LayoutParams getLayoutParams();

    void setExpandLayoutManager(ExpandLayoutManager expandLayoutManager);

    boolean doExpandAnimation(float animationProgress);

    boolean doCollapseAnimation(float animationProgress);
}