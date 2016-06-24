package com.azoft.expandlayoutmanager.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.azoft.expandlayoutmanager.AnimationAction;
import com.azoft.expandlayoutmanager.ExpandLayoutManager;
import com.azoft.expandlayoutmanager.view.AnimationView;

public interface ExpandModel {

    void addAction(@NonNull final ExpandLayoutManager expandLayoutManager, @NonNull final AnimationAction animationAction);

    int getExpandItemPosition();

    int getExpandOrExpandingItem();

    void setPendingScrollPosition(final int position);

    int getMaxSize(final int itemsCount, int height);

    int scrollBy(int diff, int itemsCount, int height);

    void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state);

    boolean checkRemeasureNeeded(ExpandLayoutManager expandLayoutManager, RecyclerView.Recycler recycler, final RecyclerView.State state);

    void doBeforeFillActionsInPriority(ExpandLayoutManager expandLayoutManager, RecyclerView.Recycler recycler, RecyclerView.State state);

    void doAfterFillActionsInPriority();

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    void generateFirstRenderData(int height, final int[] firstRenderPair);

    int getItemSize(int adapterPosition, int height);

    Parcelable onSaveInstanceState(final Parcelable parcelable);

    Parcelable onRestoreInstanceState(final Parcelable state);

    boolean updateChildStat(AnimationView view, int adapterPosition);
}