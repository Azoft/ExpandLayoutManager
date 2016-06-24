package com.azoft.expandlayoutmanager.model;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.azoft.expandlayoutmanager.AnimationAction;
import com.azoft.expandlayoutmanager.ExpandLayoutManager;
import com.azoft.expandlayoutmanager.utils.GeneralUtils;
import com.azoft.expandlayoutmanager.view.AnimationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleExpandModel implements ExpandModel {

    private final int mAnimationDuration;

    private int mScrollOffset;
    private Integer mDecoratedChildHeight;
    private int mExpandItemPosition = ExpandLayoutManager.INVALID_POSITION;

    private int mPendingScrollPosition = ExpandLayoutManager.INVALID_POSITION;
    private AnimationAction mPendingAnimationAction;
    private ExpandSavedState mPendingSavedState;

    private ExecutingAnimationData mExecutingAnimationData;
    private final List<Integer> mTmpList = new ArrayList<>();

    public SimpleExpandModel(final int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    @Override
    public boolean checkRemeasureNeeded(final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            expandLayoutManager.removeAndRecycleAllViews(recycler);
            return false;
        }

        if (null == mDecoratedChildHeight) {
            final AnimationView view = (AnimationView) recycler.getViewForPosition(0);
            view.resetState();
            expandLayoutManager.addView((View) view);
            expandLayoutManager.measureChildWithMargins((View) view, 0, 0);

            mDecoratedChildHeight = expandLayoutManager.getDecoratedMeasuredHeight((View) view);
            expandLayoutManager.removeAndRecycleView((View) view, recycler);

            return true;
        }
        return false;
    }

    @Override
    public void doBeforeFillActionsInPriority(final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (expandLayoutManager.canScrollVertically() && ExpandLayoutManager.INVALID_POSITION != mPendingScrollPosition) {
            mScrollOffset = calculateScrollForSelectingPosition(mPendingScrollPosition, state, expandLayoutManager.getHeight());
            mPendingScrollPosition = ExpandLayoutManager.INVALID_POSITION;
        } else if (null != mPendingSavedState) {
            if (ExpandLayoutManager.INVALID_POSITION == mPendingSavedState.mExpandItemPosition) {
                mScrollOffset = mPendingSavedState.mScrollOffset;
            } else {
                mScrollOffset = calculateScrollForSelectingPosition(mPendingSavedState.mExpandItemPosition, state, expandLayoutManager.getHeight());
                mExpandItemPosition = mPendingSavedState.mExpandItemPosition;
            }
            mPendingSavedState = null;
        }

        if (null == mExecutingAnimationData && null != mPendingAnimationAction) {
            mExpandItemPosition = ExpandLayoutManager.INVALID_POSITION;

            mExecutingAnimationData = new ExecutingAnimationData(new ValueUpdateListener(this, expandLayoutManager, recycler, state), createAnimation());
            mExecutingAnimationData.addAnimationAction(mPendingAnimationAction);

            mPendingAnimationAction = null;
        }
    }

    private int calculateScrollForSelectingPosition(final int scrollToItemPosition, final RecyclerView.State state, final int heightNoPadding) {
        final int fixedItemPosition;
        if (null == state) {
            fixedItemPosition = scrollToItemPosition;
        } else {
            fixedItemPosition = scrollToItemPosition < state.getItemCount() ? scrollToItemPosition : state.getItemCount() - 1;
        }

        int tmpScroll = fixedItemPosition * mDecoratedChildHeight;

        if (null != mExecutingAnimationData) {
            mTmpList.clear();
            mTmpList.addAll(mExecutingAnimationData.mAnimationProgressAction.getCollapseItems());
            if (ExpandLayoutManager.INVALID_POSITION != mExecutingAnimationData.mAnimationProgressAction.getExpandItem()) {
                mTmpList.add(mExecutingAnimationData.mAnimationProgressAction.getExpandItem());
            }
            Collections.sort(mTmpList);
            for (final int itemPosition : mTmpList) {
                if (itemPosition >= fixedItemPosition) {
                    break;
                }
                final int movingDiff = getItemSize(itemPosition, heightNoPadding) - mDecoratedChildHeight;
                tmpScroll += movingDiff;
            }
        }

        return tmpScroll;
    }

    private ValueAnimator createAnimation() {
        return ValueAnimator.ofFloat(0, 1).setDuration(mAnimationDuration);
    }

    @Override
    public void doAfterFillActionsInPriority() {
        if (null != mExecutingAnimationData) {
            mExecutingAnimationData.startIfNeeded();
        }
    }

    @Override
    public int getExpandItemPosition() {
        return mExpandItemPosition;
    }

    @Override
    public int getExpandOrExpandingItem() {
        if (null == mExecutingAnimationData) {
            return mExpandItemPosition;
        }
        return mExecutingAnimationData.mAnimationProgressAction.getExpandItem();
    }

    @Override
    public void addAction(@NonNull final ExpandLayoutManager expandLayoutManager, @NonNull final AnimationAction animationAction) {
        if (null != mExecutingAnimationData) {
            if (GeneralUtils.equals(mExecutingAnimationData.mAnimationProgressAction, mPendingAnimationAction)) {
                // the same data executing
                return;
            }
            mExecutingAnimationData.addAnimationAction(animationAction);
        } else {
            mExpandItemPosition = ExpandLayoutManager.INVALID_POSITION;
            mPendingAnimationAction = animationAction;
            expandLayoutManager.requestLayout();
        }
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    @Override
    public void generateFirstRenderData(final int height, final int[] firstRenderPair) {
        int firstRender = mScrollOffset / mDecoratedChildHeight;
        int tmpScroll = firstRender * mDecoratedChildHeight - mScrollOffset;
        if (null != mExecutingAnimationData) {
            mTmpList.clear();
            mTmpList.addAll(mExecutingAnimationData.mAnimationProgressAction.getCollapseItems());
            if (ExpandLayoutManager.INVALID_POSITION != mExecutingAnimationData.mAnimationProgressAction.getExpandItem()) {
                mTmpList.add(mExecutingAnimationData.mAnimationProgressAction.getExpandItem());
            }
            Collections.sort(mTmpList);
            for (final int itemPosition : mTmpList) {
                if (itemPosition >= firstRender) {
                    break;
                }
                int movingDiff = getItemSize(itemPosition, height) - mDecoratedChildHeight;
                do {
                    if (movingDiff <= Math.abs(tmpScroll)) {
                        tmpScroll += movingDiff;
                        movingDiff = 0;
                    } else {
                        movingDiff -= Math.abs(tmpScroll);
                        firstRender -= 1;
                        tmpScroll = -getItemSize(firstRender, height);
                    }
                } while (0 < movingDiff);
            }
        }

        firstRenderPair[0] = firstRender;
        firstRenderPair[1] = tmpScroll;
    }

    @Override
    public int getItemSize(final int adapterPosition, final int height) {
        if (null == mExecutingAnimationData) {
            return mDecoratedChildHeight;
        }
        Float animationProgress = mExecutingAnimationData.mAnimationProgressAction.getExpandProgress(adapterPosition);
        if (null != animationProgress) {
            return Math.round(mDecoratedChildHeight + (height - mDecoratedChildHeight) * animationProgress);
        }
        animationProgress = mExecutingAnimationData.mAnimationProgressAction.getCollapseProgress(adapterPosition);
        if (null != animationProgress) {
            return Math.round(height - (height - mDecoratedChildHeight) * animationProgress);
        }
        return mDecoratedChildHeight;
    }

    @Override
    public Parcelable onSaveInstanceState(final Parcelable parcelable) {
        final ExpandSavedState expandSavedState = new ExpandSavedState(parcelable);
        expandSavedState.mExpandItemPosition = getExpandOrExpandingItem();
        expandSavedState.mScrollOffset = mScrollOffset;
        return expandSavedState;
    }

    @Override
    public Parcelable onRestoreInstanceState(final Parcelable state) {
        if (state instanceof ExpandSavedState) {
            final ExpandSavedState expandSavedState = (ExpandSavedState) state;
            mPendingSavedState = expandSavedState;
            return expandSavedState.mSuperState;
        }
        return state;
    }

    @Override
    public boolean updateChildStat(final AnimationView view, final int adapterPosition) {
        if (null != mExecutingAnimationData) {
            Float itemAnimationProgress = mExecutingAnimationData.mAnimationProgressAction.getExpandProgress(adapterPosition);
            if (null != itemAnimationProgress) {
                return view.doExpandAnimation(itemAnimationProgress);
            }
            itemAnimationProgress = mExecutingAnimationData.mAnimationProgressAction.getCollapseProgress(adapterPosition);
            if (null != itemAnimationProgress) {
                return view.doCollapseAnimation(itemAnimationProgress);
            }
        }
        if (mExpandItemPosition == adapterPosition) {
            return view.doExpandAnimation(1);
        }
        return view.resetState();
    }

    @Override
    public int getMaxSize(final int itemsCount, final int height) {
        if (0 == itemsCount) {
            return 0;
        }
        int fullSize = mDecoratedChildHeight * (itemsCount - 1);
        if (null != mExecutingAnimationData) {
            if (ExpandLayoutManager.INVALID_POSITION != mExecutingAnimationData.mAnimationProgressAction.getExpandItem()) {
                fullSize = fullSize - mDecoratedChildHeight + getItemSize(mExecutingAnimationData.mAnimationProgressAction.getExpandItem(), height);
            }
            for (final int itemPosition : mExecutingAnimationData.mAnimationProgressAction.getCollapseItems()) {
                fullSize = fullSize - mDecoratedChildHeight + getItemSize(itemPosition, height);
            }
        }
        if (fullSize < height) {
            return 0;
        }
        return fullSize - height;
    }

    @Override
    public void setPendingScrollPosition(final int position) {
        mPendingScrollPosition = position;
    }

    @Override
    public int scrollBy(final int diff, final int itemsCount, final int height) {
        final int maxOffset = getMaxSize(itemsCount, height);
        final int resultScroll;
        if (0 > mScrollOffset + diff) {
            resultScroll = -mScrollOffset; //to make it 0
        } else if (mScrollOffset + diff > maxOffset) {
            resultScroll = maxOffset - mScrollOffset; //to make it maxOffset
        } else {
            resultScroll = diff;
        }
        mScrollOffset += resultScroll;
        return resultScroll;
    }

    @Override
    public void onMeasure(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        mDecoratedChildHeight = null;
    }

    private static class ValueUpdateListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

        private final ExpandLayoutManager mExpandLayoutManager;
        private final RecyclerView.Recycler mRecycler;
        private final RecyclerView.State mState;

        private final SimpleExpandModel mSimpleExpandModel;

        private AnimationProgressAction mAnimationProgressAction;

        private Float mStartupProgress;
        private Integer mStartupScrollPosition;
        private Integer mToScrollOffset;
        private int mScrolledPosition;

        private float mPreviousAnimationProgress;
        private boolean mWasCanceled;

        @SuppressWarnings("ConstructorWithTooManyParameters")
        ValueUpdateListener(final SimpleExpandModel simpleExpandModel, final ExpandLayoutManager expandLayoutManager, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
            mSimpleExpandModel = simpleExpandModel;
            mExpandLayoutManager = expandLayoutManager;
            mRecycler = recycler;
            mState = state;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            if (mWasCanceled) {
                return;
            }

            final float animationProgress = (float) animation.getAnimatedValue();
            mAnimationProgressAction.addProgress(animationProgress - mPreviousAnimationProgress);
            mPreviousAnimationProgress = animationProgress;

            final int expandPosition = mAnimationProgressAction.getExpandItem();
            final int needToScroll;
            if (ExpandLayoutManager.INVALID_POSITION == expandPosition) {
                needToScroll = 0;
            } else {
                needToScroll = createNeedToScroll(animationProgress, expandPosition);
                mScrolledPosition += needToScroll;
            }
            if (0 == mExpandLayoutManager.scrollBy(needToScroll, mRecycler, mState)) {
                mExpandLayoutManager.fillData(mRecycler, mState, false);
            }

            if (mAnimationProgressAction.checkEnds()) {
                onAnimationEnds();
            }
        }

        private int createNeedToScroll(final float animationProgress, final int expandPosition) {
            final float expandAnimationProgress = 1 == mStartupProgress ? 1 : Math.min(1, animationProgress / (1 - mStartupProgress));
            if (null == mToScrollOffset || mAnimationProgressAction.hasCollapseItems()) {
                mToScrollOffset = mSimpleExpandModel.calculateScrollForSelectingPosition(expandPosition, mState, mExpandLayoutManager.getHeight());
            }
            return Math.round((mToScrollOffset - mStartupScrollPosition) * expandAnimationProgress - mScrolledPosition);
        }

        @Override
        public void onAnimationStart(final Animator animation) {
            mWasCanceled = false;
        }

        private void updateStartupProgress(final boolean force) {
            if (force || null == mStartupProgress) {
                final int expandPosition = mAnimationProgressAction.getExpandItem();
                if (ExpandLayoutManager.INVALID_POSITION == expandPosition) {
                    mStartupProgress = null;
                    mStartupScrollPosition = null;
                } else {
                    mStartupProgress = mAnimationProgressAction.getExpandProgress(expandPosition);
                    mStartupScrollPosition = mSimpleExpandModel.mScrollOffset;
                }
                mScrolledPosition = 0;
            }
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            onAnimationEnds();
        }

        private void onAnimationEnds() {
            if (mWasCanceled) {
                return;
            }
            mSimpleExpandModel.mExpandItemPosition = mAnimationProgressAction.getExpandItem();
            mSimpleExpandModel.mExecutingAnimationData = null;
            mExpandLayoutManager.requestLayout();

            mAnimationProgressAction.clearData();

            mWasCanceled = true;
        }

        @Override
        public void onAnimationCancel(final Animator animation) {

        }

        @Override
        public void onAnimationRepeat(final Animator animation) {

        }

        public void updateExpandItemData(final AnimationProgressAction animationProgressAction) {
            mAnimationProgressAction = animationProgressAction;
        }

        public void restartInner(final ValueAnimator progressAnimator) {
            progressAnimator.removeAllListeners();
            progressAnimator.cancel();
            startInner(progressAnimator);
        }

        public void startInner(final ValueAnimator progressAnimator) {
            progressAnimator.removeAllListeners();
            progressAnimator.addUpdateListener(this);
            progressAnimator.addListener(this);
            mWasCanceled = true;
            mToScrollOffset = null;
            mPreviousAnimationProgress = 0;
            updateStartupProgress(true);
            progressAnimator.start();
        }
    }

    private static class ExecutingAnimationData {

        @NonNull
        private final AnimationProgressAction mAnimationProgressAction;
        @NonNull
        private final ValueUpdateListener mValueListener;
        @NonNull
        private final ValueAnimator mProgressAnimator;

        ExecutingAnimationData(@NonNull final ValueUpdateListener valueUpdateListener, @NonNull final ValueAnimator animation) {
            mAnimationProgressAction = new AnimationProgressAction();

            mValueListener = valueUpdateListener;
            mProgressAnimator = animation;

            mProgressAnimator.addUpdateListener(mValueListener);
            mProgressAnimator.addListener(mValueListener);
        }

        void addAnimationAction(final AnimationAction animationAction) {
            if (mAnimationProgressAction.mergeActions(animationAction)) {
                mValueListener.updateExpandItemData(mAnimationProgressAction);

                if (mProgressAnimator.isRunning()) {
                    mValueListener.restartInner(mProgressAnimator);
                }
            }
        }

        public void startIfNeeded() {
            if (!mProgressAnimator.isRunning()) {
                mValueListener.startInner(mProgressAnimator);
            }
        }
    }

    public static class ExpandSavedState implements Parcelable {

        private final Parcelable mSuperState;
        private int mExpandItemPosition;
        private int mScrollOffset;

        public ExpandSavedState(@Nullable final Parcelable superState) {
            mSuperState = superState;
        }

        private ExpandSavedState(@NonNull final Parcel in) {
            mSuperState = in.readParcelable(Parcelable.class.getClassLoader());
            mExpandItemPosition = in.readInt();
            mScrollOffset = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(mSuperState, flags);
            dest.writeInt(mExpandItemPosition);
            dest.writeInt(mScrollOffset);
        }

        public static final Parcelable.Creator<ExpandSavedState> CREATOR = new Parcelable.Creator<ExpandSavedState>() {
            @Override
            public ExpandSavedState createFromParcel(final Parcel source) {
                return new ExpandSavedState(source);
            }

            @Override
            public ExpandSavedState[] newArray(final int size) {
                return new ExpandSavedState[size];
            }
        };
    }
}