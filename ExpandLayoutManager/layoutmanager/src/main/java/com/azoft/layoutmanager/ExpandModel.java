package com.azoft.layoutmanager;

import android.support.annotation.NonNull;

public interface ExpandModel {

    void addAction(@NonNull final ExpandLayoutManager expandLayoutManager, @NonNull final AnimationAction animationAction);

    int getCollapseItem();

    int getExpandItem();

    void setPendingScrollPosition(final int position);

    int getMaxSize(final int itemsCount, final int decoratedChildHeight, int heightNoPadding);
}