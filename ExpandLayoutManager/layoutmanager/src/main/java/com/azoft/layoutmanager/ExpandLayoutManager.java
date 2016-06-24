package com.azoft.layoutmanager;

import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.azoft.layoutmanager.model.ExpandModel;
import com.azoft.layoutmanager.model.SimpleExpandModel;
import com.azoft.layoutmanager.view.AnimationView;

import java.util.ArrayList;
import java.util.List;

public class ExpandLayoutManager extends RecyclerView.LayoutManager {

    public static final int INVALID_POSITION = -1;

    private static final int DEFAULT_ANIMATION_DURATION = 1000;

    private final ExpandModel mExpandModel;
    private final LayoutHelper mLayoutHelper;

    private final List<View> mTmpRemoveList = new ArrayList<>();

    public ExpandLayoutManager() {
        this(DEFAULT_ANIMATION_DURATION);
    }

    public ExpandLayoutManager(final int animationDuration) {
        this(new SimpleExpandModel(animationDuration));
    }

    public ExpandLayoutManager(@NonNull final ExpandModel expandModel) {
        mExpandModel = expandModel;
        mLayoutHelper = new LayoutHelper(mExpandModel);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void actionItem(@NonNull final AnimationView animationView) {
        final ViewGroup.LayoutParams layoutParams = animationView.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            actionItem(((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition());
        } else {
            throw new IllegalArgumentException("View is not attached to LayoutManager");
        }
    }

    public void expandItem(@NonNull final AnimationView animationView) {
        final ViewGroup.LayoutParams layoutParams = animationView.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            expandItem(((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition());
        } else {
            throw new IllegalArgumentException("View is not attached to LayoutManager");
        }
    }

    public void collapseItem(@NonNull final AnimationView animationView) {
        final ViewGroup.LayoutParams layoutParams = animationView.getLayoutParams();
        if (layoutParams instanceof RecyclerView.LayoutParams) {
            collapseItem(((RecyclerView.LayoutParams) layoutParams).getViewAdapterPosition());
        } else {
            throw new IllegalArgumentException("View is not attached to LayoutManager");
        }
    }

    public void actionItem(final int adapterPosition) {
        if (0 > adapterPosition) {
            throw new IllegalArgumentException("adapter position can't be less then 0");
        }
        int collapsePosition = INVALID_POSITION;
        int expandPosition = INVALID_POSITION;
        if (getOpenOrOpeningItemPosition() == adapterPosition) {
            collapsePosition = adapterPosition;
        } else {
            if (isItemExpandOrExpanding()) {
                collapsePosition = getOpenOrOpeningItemPosition();
            }
            expandPosition = adapterPosition;
        }
        mExpandModel.addAction(this, AnimationAction.createdAction(expandPosition, collapsePosition));
    }

    public void expandItem(final int adapterPosition) {
        if (0 > adapterPosition) {
            throw new IllegalArgumentException("adapter position can't be less then 0");
        }
        int collapsePosition = INVALID_POSITION;
        if (isItemExpandOrExpanding()) {
            if (getOpenOrOpeningItemPosition() == adapterPosition) {
                // nothing to do
                return;
            } else {
                collapsePosition = getOpenOrOpeningItemPosition();
            }
        }
        mExpandModel.addAction(this, AnimationAction.createdAction(adapterPosition, collapsePosition));
    }

    public void collapseItem(final int adapterPosition) {
        if (0 > adapterPosition) {
            throw new IllegalArgumentException("adapter position can't be less then 0");
        }
        if (getOpenOrOpeningItemPosition() != adapterPosition) {
            // nothing to do
            return;
        }
        mExpandModel.addAction(this, AnimationAction.createCollapseAction(adapterPosition));
    }

    public int getOpenOrOpeningItemPosition() {
        return mExpandModel.getExpandOrExpandingItem();
    }

    public boolean isItemExpandOrExpanding() {
        return INVALID_POSITION != getOpenOrOpeningItemPosition();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void scrollToPosition(final int position) {
        mExpandModel.setPendingScrollPosition(position);
        requestLayout();
    }

    @Override
    public boolean canScrollVertically() {
        return 0 != getChildCount() && !isItemExpandOrExpanding();
    }

    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        return scrollBy(dy, recycler, state);
    }

    @CallSuper
    public int scrollBy(final int diff, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == getChildCount()) {
            return 0;
        }

        final int resultScroll = mExpandModel.scrollBy(diff, state.getItemCount(), getHeight());
        if (0 != resultScroll) {
            fillData(recycler, state, false);
        }
        return resultScroll;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        mExpandModel.onMeasure(recycler, state);

        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    @CallSuper
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        final boolean childMeasuringNeeded = mExpandModel.checkRemeasureNeeded(this, recycler, state);

        mExpandModel.doBeforeFillActionsInPriority(this, recycler, state);

        fillData(recycler, state, childMeasuringNeeded);

        mExpandModel.doAfterFillActionsInPriority();
    }

    public void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state, final boolean childMeasuringNeeded) {
        mLayoutHelper.generateLayoutOrder(state, getHeight());

        removeAndRecycleUnusedViews(mLayoutHelper, recycler);

        fillDataVertical(recycler, childMeasuringNeeded);

        recycler.clear();
    }

    private void fillDataVertical(final RecyclerView.Recycler recycler, final boolean measuringNeeded) {
        final int start = getPaddingStart();
        final int end = start + getWidth() - getPaddingEnd() - getPaddingStart();

        for (final LayoutHelper.LayoutOrder layoutOrder : mLayoutHelper.getLayoutOrder()) {
            final View view = bindChild(layoutOrder.getItemAdapterPosition(), recycler, measuringNeeded);
            view.layout(start, layoutOrder.getTop(), end, layoutOrder.getBottom());
        }
    }

    private void removeAndRecycleUnusedViews(final LayoutHelper layoutHelper, final RecyclerView.Recycler recycler) {
        mTmpRemoveList.clear();
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                mTmpRemoveList.add(child);
                continue;
            }
            final RecyclerView.LayoutParams recyclerViewLp = (RecyclerView.LayoutParams) lp;
            final int adapterPosition = recyclerViewLp.getViewAdapterPosition();
            if (recyclerViewLp.isItemRemoved() || !layoutHelper.hasAdapterPosition(adapterPosition)) {
                mTmpRemoveList.add(child);
            }
        }

        for (final View view : mTmpRemoveList) {
            removeAndRecycleView(view, recycler);
        }
        mTmpRemoveList.clear();
    }

    private View bindChild(final int position, @NonNull final RecyclerView.Recycler recycler, final boolean childMeasuringNeeded) {
        final View view = findViewForPosition(recycler, position);

        if (null == view.getParent()) {
            addView(view);
            measureChildWithMargins(view, 0, 0);
        } else {
            detachView(view);
            attachView(view);
            if (childMeasuringNeeded) {
                measureChildWithMargins(view, 0, 0);
            }
        }

        return view;
    }

    private View findViewForPosition(final RecyclerView.Recycler recycler, final int position) {
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                continue;
            }
            final RecyclerView.LayoutParams recyclerLp = (RecyclerView.LayoutParams) lp;
            final int adapterPosition = recyclerLp.getViewAdapterPosition();
            if (adapterPosition == position) {
                if (recyclerLp.isItemChanged()) {
                    recycler.bindViewToPosition(child, position);
                }
                if (mExpandModel.updateChildStat((AnimationView) child, position) || recyclerLp.isItemChanged()) {
                    measureChildWithMargins(child, 0, 0);
                }
                return child;
            }
        }
        final View view = recycler.getViewForPosition(position);
        recycler.bindViewToPosition(view, position);
        if (view instanceof AnimationView) {
            ((AnimationView) view).setExpandLayoutManager(this);
            mExpandModel.updateChildStat((AnimationView) view, position);
            return view;
        } else {
            throw new IllegalArgumentException("This layout support only AnimationView childs!");
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return mExpandModel.onSaveInstanceState(super.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        super.onRestoreInstanceState(mExpandModel.onRestoreInstanceState(state));
    }
}