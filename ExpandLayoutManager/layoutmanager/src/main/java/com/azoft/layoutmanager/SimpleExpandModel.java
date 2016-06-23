package com.azoft.layoutmanager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class SimpleExpandModel /*implements ExpandModel {

    private int mPendingScrollPosition;

    private AnimationAction mExecutingAnimationAction;
    private AnimationAction mPendingAnimationAction;
    private ValueUpdateListener mValueListener;

    private ValueAnimator mProgressAnimator;

    public void doBeforeFillActionsInPriority(final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (expandLayoutManager.canScrollVertically() && ExpandLayoutManager.INVALID_POSITION != mPendingScrollPosition) {
            expandLayoutManager.mLayoutHelper.mScrollOffset = expandLayoutManager.calculateScrollForSelectingPosition(mPendingScrollPosition, state);
            mPendingScrollPosition = ExpandLayoutManager.INVALID_POSITION;
        }
            *//*
        } else if (null != mPendingCarouselSavedState) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingCarouselSavedState.mCenterItemPosition, state);
            mPendingCarouselSavedState = null;
*//*

        if (null == mExecutingAnimationAction && null != mPendingAnimationAction) {
            expandLayoutManager.mOpenItemPosition = ExpandLayoutManager.INVALID_POSITION;

            mExecutingAnimationAction = mPendingAnimationAction;
            mProgressAnimator = null;

            int scrollPositionDiff = 0;
            if (ExpandLayoutManager.INVALID_POSITION != mExecutingAnimationAction.mExpandPosition) {
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

    @Override
    public int getExpandItem() {
        if (null == mExecutingAnimationAction) {
            return ExpandLayoutManager.INVALID_POSITION;
        }
        return mExecutingAnimationAction.mExpandPosition;
    }

    @Override
    public void setPendingScrollPosition(final int position) {
        mPendingScrollPosition = position;
    }

    @Override
    public int getCollapseItem() {
        if (null == mExecutingAnimationAction) {
            return ExpandLayoutManager.INVALID_POSITION;
        }
        return mExecutingAnimationAction.mCollapsePosition;
    }

    @Override
    public void addAction(final ExpandLayoutManager expandLayoutManager, final AnimationAction animationAction) {
        if (null != mExecutingAnimationAction && GeneralUtils.equals(mExecutingAnimationAction, mPendingAnimationAction)) {
            // the same data executing
            return;
        }
        expandLayoutManager.mOpenItemPosition = ExpandLayoutManager.INVALID_POSITION;
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
            return Math.round(maxHeight - (maxHeight - decoratedChildHeight) * mValueListener.mAnimationProgress);
        }
        return decoratedChildHeight;
    }

    @Override
    public int getMaxSize(final int itemsCount, final int decoratedChildHeight, final int maxHeight) {
        if (0 == itemsCount) {
            return 0;
        }
        int fullSize = decoratedChildHeight * (itemsCount - 1);
        if (null != mExecutingAnimationAction) {
            if (ExpandLayoutManager.INVALID_POSITION != mExecutingAnimationAction.mCollapsePosition) {
                fullSize = fullSize - decoratedChildHeight + getItemSize(mExecutingAnimationAction.mCollapsePosition, decoratedChildHeight, maxHeight);
            }
            if (ExpandLayoutManager.INVALID_POSITION != mExecutingAnimationAction.mExpandPosition) {
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
            if (0 == mExpandLayoutManager.scrollBy(needToScroll, mRecycler, mState)) {
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
}*/ {}