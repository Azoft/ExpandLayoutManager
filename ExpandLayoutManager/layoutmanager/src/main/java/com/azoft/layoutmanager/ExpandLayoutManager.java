package com.azoft.layoutmanager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("ClassWithTooManyMethods")
public class ExpandLayoutManager extends RecyclerView.LayoutManager {

    private static final int INVALID_POSITION = -1;

    private final LayoutHelper mLayoutHelper = new LayoutHelper();

    private final PendingActions mPendingActions = new PendingActions();

    private int mOpenItemPosition = INVALID_POSITION;
    private int mItemsCount;

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
        final AnimationAction animationAction = new AnimationAction();
        if (getOpenOrOpeningItemPosition() == adapterPosition) {
            animationAction.mCollapsePosition = adapterPosition;
        } else {
            if (isItemExpandOrExpanding()) {
                animationAction.mCollapsePosition = getOpenOrOpeningItemPosition();
            }
            animationAction.mExpandPosition = adapterPosition;
        }
        mPendingActions.addAction(this, animationAction);
    }

    public void expandItem(final int adapterPosition) {
        if (0 > adapterPosition) {
            throw new IllegalArgumentException("adapter position can't be less then 0");
        }
        final AnimationAction animationAction = new AnimationAction();
        if (isItemExpandOrExpanding()) {
            if (getOpenOrOpeningItemPosition() == adapterPosition) {
                // nothing to do
                return;
            } else {
                animationAction.mCollapsePosition = getOpenOrOpeningItemPosition();
            }
        }
        animationAction.mExpandPosition = adapterPosition;
        mPendingActions.addAction(this, animationAction);
    }

    public void collapseItem(final int adapterPosition) {
        if (0 > adapterPosition) {
            throw new IllegalArgumentException("adapter position can't be less then 0");
        }
        if (getOpenOrOpeningItemPosition() != adapterPosition || adapterPosition == mPendingActions.getCollapseItem()) {
            // nothing to do
            return;
        }
        final AnimationAction animationAction = new AnimationAction();
        animationAction.mCollapsePosition = adapterPosition;
        mPendingActions.addAction(this, animationAction);
    }

    public int getOpenOrOpeningItemPosition() {
        return INVALID_POSITION == mOpenItemPosition ? mPendingActions.getExpandItem() : mOpenItemPosition;
    }

    public boolean isItemExpandOrExpanding() {
        return INVALID_POSITION != getOpenOrOpeningItemPosition();
    }

    public boolean isItemCollapsing() {
        return INVALID_POSITION != mPendingActions.getCollapseItem();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void scrollToPosition(final int position) {
        mPendingActions.mPendingScrollPosition = position;
        requestLayout();
    }

    @Override
    public boolean canScrollVertically() {
        return 0 != getChildCount() && !isItemExpandOrExpanding() && !isItemCollapsing();
    }

    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        return scrollBy(dy, recycler, state, true);
    }

    @CallSuper
    protected int scrollBy(final int diff, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state, final boolean fromUser) {
        if (0 == getChildCount()) {
            return 0;
        }
        final int resultScroll;

        final int maxOffset = getMaxScrollOffset();
        if (0 > mLayoutHelper.mScrollOffset + diff) {
            resultScroll = -mLayoutHelper.mScrollOffset; //to make it 0
        } else if (mLayoutHelper.mScrollOffset + diff > maxOffset) {
            resultScroll = maxOffset - mLayoutHelper.mScrollOffset; //to make it maxOffset
        } else {
            resultScroll = diff;
        }
/*
        } else {
            if (0 > mLayoutHelper.mScrollOffset + diff) {
                resultScroll = -mLayoutHelper.mScrollOffset; //to make it 0
            } else {
                for (int i = 0; i < mItemsCount; ++i) {
                    maxScroll += mPendingActions.getItemSize(i, mLayoutHelper.mDecoratedChildHeight, getHeightNoPadding());
                    if ()
                }
            }
        }
*/
        if (0 != resultScroll) {
            mLayoutHelper.mScrollOffset += resultScroll;
            fillData(recycler, state, false);
        }
        return resultScroll;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        mLayoutHelper.mDecoratedChildHeight = null;

        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    @CallSuper
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        mItemsCount = state.getItemCount();
        if (0 == mItemsCount) {
            removeAndRecycleAllViews(recycler);
            selectOpenItemPosition(INVALID_POSITION);
            return;
        }

        final boolean childMeasuringNeeded = mLayoutHelper.onActionsInOnLayout(this, recycler, state);

        mPendingActions.doBeforeFillActionsInPriority(this, recycler, state);

        fillData(recycler, state, childMeasuringNeeded);

        mPendingActions.doAfterFillActionsInPriority(this, recycler, state);
    }

    private void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state, final boolean childMeasuringNeeded) {
        generateLayoutOrder(state);
        removeAndRecycleUnusedViews(mLayoutHelper, recycler);

        fillDataVertical(recycler, getWidthNoPadding(), childMeasuringNeeded);

        recycler.clear();
    }

    private void fillDataVertical(final RecyclerView.Recycler recycler, final int width, final boolean childMeasuringNeeded) {
        final int start = getPaddingStart();
        final int end = start + width;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.size(); i < count; ++i) {
            final LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder.get(i);

            fillChildItem(start, layoutOrder.mTop, end, layoutOrder.mBottom, layoutOrder, recycler, childMeasuringNeeded);
        }
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private void fillChildItem(final int start, final int top, final int end, final int bottom, @NonNull final LayoutOrder layoutOrder, @NonNull final RecyclerView.Recycler recycler, final boolean childMeasuringNeeded) {
        final View view = bindChild(layoutOrder.mItemAdapterPosition, recycler, childMeasuringNeeded);
        Log.e("!!!!!!!!", "layout item: " + layoutOrder.mItemAdapterPosition + "; position: " + top + ", " + end);
        view.layout(start, top, end, bottom);
    }

    private void generateLayoutOrder(@NonNull final RecyclerView.State state) {
        mItemsCount = state.getItemCount();

        mLayoutHelper.clearItems();

        int firstRender = 0;
        int tmpScroll = -mLayoutHelper.mScrollOffset;
        for (int i = 0; i < mItemsCount; ++i) {
            firstRender = i;
            final int itemHeight = mPendingActions.getItemSize(i, mLayoutHelper.mDecoratedChildHeight, getHeightNoPadding());
            if (0 < tmpScroll + itemHeight) {
                break;
            }
            tmpScroll += itemHeight;
        }

        if (INVALID_POSITION == mOpenItemPosition) {
            int top = tmpScroll;

            for (int i = firstRender; i < mItemsCount - 1; ++i) {
                final int itemHeight = mPendingActions.getItemSize(i, mLayoutHelper.mDecoratedChildHeight, getHeightNoPadding());
                final int bottom = top + itemHeight;

                mLayoutHelper.createItem(i, top, bottom);

                top = bottom;
                if (top > getHeightNoPadding()) {
                    break;
                }
            }
        } else {
            final int top = 0;
            final int bottom = getHeightNoPadding();

            mLayoutHelper.createItem(firstRender, top, bottom);
        }
    }

    private void removeAndRecycleUnusedViews(final LayoutHelper layoutHelper, final RecyclerView.Recycler recycler) {
        final List<View> viewsToRemove = new ArrayList<>();
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (!(lp instanceof RecyclerView.LayoutParams)) {
                viewsToRemove.add(child);
                continue;
            }
            final RecyclerView.LayoutParams recyclerViewLp = (RecyclerView.LayoutParams) lp;
            final int adapterPosition = recyclerViewLp.getViewAdapterPosition();
            if (recyclerViewLp.isItemRemoved() || !layoutHelper.hasAdapterPosition(adapterPosition)) {
                viewsToRemove.add(child);
            }
        }

        for (final View view : viewsToRemove) {
            removeAndRecycleView(view, recycler);
        }
    }

    private int calculateScrollForSelectingPosition(final int itemPosition, final RecyclerView.State state) {
        final int fixedItemPosition = itemPosition < state.getItemCount() ? itemPosition : state.getItemCount() - 1;
        return fixedItemPosition * mLayoutHelper.mDecoratedChildHeight;
    }

    private void selectOpenItemPosition(final int position) {
        mOpenItemPosition = position;
    }

    protected int getMaxScrollOffset() {
        // getScrollItemSize() * (mItemsCount - 1) - getHeightNoPadding())
        return mPendingActions.getMaxSize(mItemsCount, mLayoutHelper.mDecoratedChildHeight, getHeightNoPadding());
    }

    protected int getScrollItemSize() {
        return mLayoutHelper.mDecoratedChildHeight;
    }

    protected int getWidthNoPadding() {
        return getWidth();
    }

    protected int getHeightNoPadding() {
        return getHeight();
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
                    measureChildWithMargins(child, 0, 0);
                }
                return child;
            }
        }
        final View view = recycler.getViewForPosition(position);
        recycler.bindViewToPosition(view, position);
        if (view instanceof AnimationView) {
            ((AnimationView) view).setLayoutManager(this);
            return view;
        } else {
            throw new IllegalArgumentException("This layout support only AnimationView childs!");
        }
    }

    private static class LayoutHelper {

        private Integer mDecoratedChildHeight;
        private int mScrollOffset;

        private final List<LayoutOrder> mLayoutOrder;
        private final List<WeakReference<LayoutOrder>> mReusedItems;

        LayoutHelper() {
            mLayoutOrder = new ArrayList<>();
            mReusedItems = new ArrayList<>();
        }

        public void clearItems() {
            for (final LayoutOrder layoutOrder : mLayoutOrder) {
                //noinspection ObjectAllocationInLoop
                mReusedItems.add(new WeakReference<>(layoutOrder));
            }
            mLayoutOrder.clear();
        }

        public void createItem(final int adapterPosition, final int top, final int bottom) {
            final LayoutOrder layoutOrder = createLayoutOrder();

            layoutOrder.mItemAdapterPosition = adapterPosition;
            layoutOrder.mTop = top;
            layoutOrder.mBottom = bottom;

            mLayoutOrder.add(layoutOrder);
        }

        private LayoutOrder createLayoutOrder() {
            final Iterator<WeakReference<LayoutOrder>> iterator = mReusedItems.iterator();
            while (iterator.hasNext()) {
                final WeakReference<LayoutOrder> layoutOrderWeakReference = iterator.next();
                final LayoutOrder layoutOrder = layoutOrderWeakReference.get();
                iterator.remove();
                if (null != layoutOrder) {
                    return layoutOrder;
                }
            }
            return new LayoutOrder();
        }

        public boolean hasAdapterPosition(final int adapterPosition) {
            for (final LayoutOrder layoutOrder : mLayoutOrder) {
                if (layoutOrder.mItemAdapterPosition == adapterPosition) {
                    return true;
                }
            }
            return false;
        }

        public boolean onActionsInOnLayout(final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
            if (null == mDecoratedChildHeight) {
                final AnimationView view = (AnimationView) recycler.getViewForPosition(0);
                view.setLayoutManager(expandLayoutManager);
                expandLayoutManager.addView(view);
                expandLayoutManager.measureChildWithMargins(view, 0, 0);

                mDecoratedChildHeight = expandLayoutManager.getDecoratedMeasuredHeight(view);
                expandLayoutManager.removeAndRecycleView(view, recycler);

                return true;
            }
            return false;
        }
    }

    private static class PendingActions {

        private int mPendingScrollPosition;

        private AnimationAction mExecutingAnimationAction;
        private AnimationAction mPendingAnimationAction;
        private ValueUpdateListener mValueListener;

        private ValueAnimator mProgressAnimator;

        public void doBeforeFillActionsInPriority(final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
            if (expandLayoutManager.canScrollVertically() && INVALID_POSITION != mPendingScrollPosition) {
                expandLayoutManager.mLayoutHelper.mScrollOffset = expandLayoutManager.calculateScrollForSelectingPosition(mPendingScrollPosition, state);
                mPendingScrollPosition = INVALID_POSITION;
            }
            /*
        } else if (null != mPendingCarouselSavedState) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingCarouselSavedState.mCenterItemPosition, state);
            mPendingCarouselSavedState = null;
*/

            if (null == mExecutingAnimationAction && null != mPendingAnimationAction) {
                expandLayoutManager.mOpenItemPosition = INVALID_POSITION;

                mExecutingAnimationAction = mPendingAnimationAction;
                mProgressAnimator = null;

                int scrollPositionDiff = 0;
                if (INVALID_POSITION != mExecutingAnimationAction.mExpandPosition) {
                    final int toScrollPosition = expandLayoutManager.calculateScrollForSelectingPosition(mExecutingAnimationAction.mExpandPosition, state);
                    final int currentScrollPosition = expandLayoutManager.mLayoutHelper.mScrollOffset;
                    scrollPositionDiff = toScrollPosition - currentScrollPosition;
                }

                mValueListener = new ValueUpdateListener(scrollPositionDiff, mExecutingAnimationAction, expandLayoutManager, recycler, state);

                mPendingAnimationAction = null;
            }
        }

        private ValueAnimator createAnimation() {
            return ValueAnimator.ofFloat(0, 1).setDuration(10000);
        }

        public void doAfterFillActionsInPriority(final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
            if (null != mExecutingAnimationAction && null == mProgressAnimator) {
                mProgressAnimator = createAnimation();
                mProgressAnimator.addUpdateListener(mValueListener);
                mProgressAnimator.addListener(mValueListener);
                mProgressAnimator.start();
            }
        }

        public int getExpandItem() {
            if (null == mExecutingAnimationAction) {
                return INVALID_POSITION;
            }
            return mExecutingAnimationAction.mExpandPosition;
        }

        public int getCollapseItem() {
            if (null == mExecutingAnimationAction) {
                return INVALID_POSITION;
            }
            return mExecutingAnimationAction.mCollapsePosition;
        }

        public void addAction(final ExpandLayoutManager expandLayoutManager, final AnimationAction animationAction) {
            if (null != mExecutingAnimationAction && GeneralUtils.equals(mExecutingAnimationAction, mPendingAnimationAction)) {
                // the same data executing
                return;
            }
            expandLayoutManager.mOpenItemPosition = INVALID_POSITION;
            mPendingAnimationAction = animationAction;
            expandLayoutManager.requestLayout();
        }

        public int getItemSize(final int adapterPosition, final int decoratedChildHeight, final int maxHeight) {
            if (null == mExecutingAnimationAction) {
                return decoratedChildHeight;
            }
            if (mExecutingAnimationAction.mExpandPosition == adapterPosition) {
                return Math.round(decoratedChildHeight + (maxHeight - decoratedChildHeight) * mValueListener.mAnimationProgress);
            }
            if (mExecutingAnimationAction.mCollapsePosition == adapterPosition) {
                final int itemSize = Math.round(maxHeight - (maxHeight - decoratedChildHeight) * mValueListener.mAnimationProgress);
                Log.e("!!!!!!!!", "item size: " + itemSize + "; decoratedChildHeight" + decoratedChildHeight);
                return itemSize;
            }
            return decoratedChildHeight;
        }

        public int getMaxSize(final int itemsCount, final int decoratedChildHeight, final int maxHeight) {
            if (0 == itemsCount) {
                return 0;
            }
            int fullSize = decoratedChildHeight * (itemsCount - 1);
            if (null != mExecutingAnimationAction) {
                if (INVALID_POSITION != mExecutingAnimationAction.mCollapsePosition) {
                    fullSize = fullSize - decoratedChildHeight + getItemSize(mExecutingAnimationAction.mCollapsePosition, decoratedChildHeight, maxHeight);
                }
                if (INVALID_POSITION != mExecutingAnimationAction.mExpandPosition) {
                    fullSize = fullSize - decoratedChildHeight + getItemSize(mExecutingAnimationAction.mExpandPosition, decoratedChildHeight, maxHeight);
                }
            }
            if (fullSize < maxHeight) {
                return 0;
            }
            return fullSize - maxHeight;
        }

        private static class ValueUpdateListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

            private final ExpandLayoutManager mExpandLayoutManager;
            private final RecyclerView.Recycler mRecycler;
            private final RecyclerView.State mState;

            private final AnimationAction mExecutingAnimationAction;
            private final int mScrollOffset;

            private int mScrolledOffset;

            private float mAnimationProgress;

            ValueUpdateListener(final int scrollOffset, final AnimationAction executingAnimationAction, final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                mScrollOffset = scrollOffset;
                mExecutingAnimationAction = executingAnimationAction;
                mExpandLayoutManager = expandLayoutManager;
                mRecycler = recycler;
                mState = state;
            }

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mAnimationProgress = (float) animation.getAnimatedValue();

                final int needToScroll = Math.round(mScrollOffset * mAnimationProgress - mScrolledOffset);
                mScrolledOffset += needToScroll;
                if (0 == mExpandLayoutManager.scrollBy(needToScroll, mRecycler, mState, false)) {
                    mExpandLayoutManager.fillData(mRecycler, mState, false);
                }

                for (int i = 0; i < mExpandLayoutManager.getChildCount(); ++i) {
                    final View child = mExpandLayoutManager.getChildAt(i);
                    final ViewGroup.LayoutParams lp = child.getLayoutParams();
                    if (lp instanceof RecyclerView.LayoutParams) {
                        final int adapterPosition = ((RecyclerView.LayoutParams) lp).getViewAdapterPosition();
                        if (adapterPosition == mExecutingAnimationAction.mExpandPosition) {
                            ((AnimationView) child).doExpandAnimation(mAnimationProgress);
                        } else if (adapterPosition == mExecutingAnimationAction.mCollapsePosition) {
                            ((AnimationView) child).doCollapseAnimation(mAnimationProgress);
                        }
                    }
                }
            }

            @Override
            public void onAnimationStart(final Animator animation) {

            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                mExpandLayoutManager.mOpenItemPosition = mExecutingAnimationAction.mExpandPosition;
                mExpandLayoutManager.mPendingActions.mExecutingAnimationAction = null;
                mExpandLayoutManager.requestLayout();
            }

            @Override
            public void onAnimationCancel(final Animator animation) {

            }

            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        }
    }

    private static final class AnimationAction {

        private int mExpandPosition = INVALID_POSITION;
        private int mCollapsePosition = INVALID_POSITION;

        @SuppressWarnings("NonFinalFieldReferenceInEquals")
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AnimationAction)) {
                return false;
            }

            final AnimationAction animationAction = (AnimationAction) o;

            return mExpandPosition == animationAction.mExpandPosition && mCollapsePosition == animationAction.mCollapsePosition;
        }

        @SuppressWarnings("NonFinalFieldReferencedInHashCode")
        @Override
        public int hashCode() {
            int result = mExpandPosition;
            result = 31 * result + mCollapsePosition;
            return result;
        }
    }

    private static class LayoutOrder {

        /**
         * Item adapter position
         */
        private int mItemAdapterPosition;
        private int mTop;
        private int mBottom;
    }
}