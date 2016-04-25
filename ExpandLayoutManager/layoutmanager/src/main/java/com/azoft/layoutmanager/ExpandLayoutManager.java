package com.azoft.layoutmanager;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Date: 01.12.15
 * Time: 17:19
 *
 * @author Artem Zalevskiy
 */
public class ExpandLayoutManager extends RecyclerView.LayoutManager {

    private static final int TRANSITION_DURATION = 400;

    private int mAnchorPos;
    private boolean mIsOpen;
    private boolean mIgnoreOpenItem;
    private boolean mIgnoreClickItem;

    private final int mItemSize;
    private AnimationView mAnimationView;
    private final AnimatorSet mAnimatorOpenSet = new AnimatorSet();
    private final AnimatorSet mAnimatorCloseSet = new AnimatorSet();
    private final ValueAnimator mAnimatorOpen = ValueAnimator.ofFloat(0, 1);
    private final ValueAnimator mAnimatorClose = ValueAnimator.ofFloat(0, 1);
    private final ArrayList<ViewAnimationInfo> mAnimationInfoList = new ArrayList<>();

    public ExpandLayoutManager(final int itemSize) {
        mItemSize = itemSize;
        addOpenListener();
        addCloseListener();
        mAnimatorOpenSet.setDuration(TRANSITION_DURATION);
        mAnimatorCloseSet.setDuration(TRANSITION_DURATION);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (isOpen()) {
            return;
        }
        detachAndScrapAttachedViews(recycler);
        fill(recycler);
        mAnchorPos = 0;
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void actionItem(final int pos) {
        if (mIgnoreClickItem) {
            return;
        }
        if (isOpen()) {
            closeItem(pos);
        } else {
            openItem(pos);
        }
    }

    public void openItem(final int pos) {
        if (mIgnoreOpenItem) {
            return;
        }
        mIsOpen = true;
        mIgnoreOpenItem = true;
        View viewToOpen = null;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (position == pos) {
                viewToOpen = view;
            }
        }
        if (null != viewToOpen) {
            openView(viewToOpen);
        }
    }

    private void openView(final View viewToAnimate) {
        final int childCount = getChildCount();
        final int animatedPos = getPosition(viewToAnimate);
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            final int pos = getPosition(view);
            final int posDelta = pos - animatedPos;
            final ViewAnimationInfo viewAnimationInfo = new ViewAnimationInfo();
            viewAnimationInfo.startTop = getDecoratedTop(view);
            viewAnimationInfo.startBottom = getDecoratedBottom(view);
            viewAnimationInfo.finishTop = getHeight() * posDelta;
            viewAnimationInfo.finishBottom = getHeight() * posDelta + getHeight();
            viewAnimationInfo.view = view;
            mAnimationInfoList.add(viewAnimationInfo);
        }

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.EXACTLY);
        measureChildWithDecorationsAndMargin(viewToAnimate, widthSpec, heightSpec);

        mAnimationView = (AnimationView) viewToAnimate;
        mAnimatorOpenSet.start();
    }

    public void closeItem(final int pos) {
        View viewToClose = null;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (position == pos) {
                viewToClose = view;
            }
        }
        if (null != viewToClose) {
            closeView((AnimationView) viewToClose);
        }
    }

    private void closeView(final AnimationView viewToAnimate) {
        mAnimationView = viewToAnimate;
        mAnimatorCloseSet.start();
    }

    private void addOpenListener() {
        mAnimatorOpen.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float animationProgress = (float) animation.getAnimatedValue();
                for (final ViewAnimationInfo animationInfo : mAnimationInfoList) {
                    final int top = (int) (animationInfo.startTop + animationProgress * (animationInfo.finishTop - animationInfo.startTop));
                    final int bottom = (int) (animationInfo.startBottom + animationProgress * (animationInfo.finishBottom - animationInfo.startBottom));
                    layoutDecorated(animationInfo.view, 0, top, getWidth(), bottom);
                }
            }
        });
        mAnimatorOpen.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {
                if (null == mAnimationView) {
                    return;
                }
                mIgnoreClickItem = true;
                mAnimationView.openAnimation();
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                mIgnoreClickItem = false;
                mIgnoreOpenItem = false;
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                mIgnoreClickItem = false;
                mIgnoreOpenItem = false;
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }
        });
        mAnimatorOpenSet.play(mAnimatorOpen);
    }

    private void addCloseListener() {
        mAnimatorClose.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final float animationProgress = 1 - (float) animation.getAnimatedValue();
                for (final ViewAnimationInfo animationInfo : mAnimationInfoList) {
                    final int top = (int) (animationInfo.startTop + animationProgress * (animationInfo.finishTop - animationInfo.startTop));
                    final int bottom = (int) (animationInfo.startBottom + animationProgress * (animationInfo.finishBottom - animationInfo.startBottom));
                    layoutDecorated(animationInfo.view, 0, top, getWidth(), bottom);
                }
            }
        });
        mAnimatorClose.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {
                if (null == mAnimationView) {
                    return;
                }
                mIgnoreClickItem = true;
                mAnimationView.closeAnimation();

            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                mIgnoreClickItem = false;
                mIsOpen = false;
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                mIgnoreClickItem = false;
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }
        });
        mAnimatorCloseSet.play(mAnimatorClose);
    }

    private void fill(final RecyclerView.Recycler recycler) {
        final View anchorView = getView();
        detachAndScrapAttachedViews(recycler);
        fillUp(anchorView, recycler);
        fillDown(anchorView, recycler);
        if (null == anchorView) {
            return;
        }
        try {
            recycler.recycleView(anchorView);
        } catch (final Exception ignored) {
        }
    }

    private void fillUp(@Nullable final View anchorView, final RecyclerView.Recycler recycler) {
        final int anchorPos;
        int anchorTop = 0;
        if (null != anchorView) {
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }
        boolean fillUp = true;
        int pos = anchorPos - 1;
        int viewBottom = anchorTop;
        final int viewHeight = mItemSize;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY);
        while (fillUp && 0 <= pos) {
            final View view = recycler.getViewForPosition(pos);
            addView(view, 0);
            measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
            final int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
            layoutDecorated(view, 0, viewBottom - viewHeight, decoratedMeasuredWidth, viewBottom);
            viewBottom = getDecoratedTop(view);
            fillUp = (0 < viewBottom);
            pos--;
        }
    }

    private void fillDown(@Nullable final View anchorView, final RecyclerView.Recycler recycler) {
        final int anchorPos;
        int anchorTop = 0;
        if (null != anchorView) {
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }
        int pos = anchorPos;
        boolean fillDown = true;
        int viewTop = anchorTop;
        final int height = mItemSize * getItemCount();
        final int itemCount = getItemCount();
        final int viewHeight = mItemSize;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY);
        while (fillDown && pos < itemCount) {
            final View view = recycler.getViewForPosition(pos);
            addView(view, pos);
            measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
            final int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
            layoutDecorated(view, 0, viewTop, decoratedMeasuredWidth, viewTop + viewHeight);
            viewTop = getDecoratedBottom(view);
            fillDown = viewTop <= height;
            pos++;
        }
    }

    private View getView() {
        final int childCount = getChildCount();
        final Rect mainRect = new Rect(0, 0, getWidth(), mItemSize * getItemCount());
        final int maxSquare = 0;
        View view = null;
        for (int i = 0; i < childCount; i++) {
            final View item = getChildAt(i);
            final int top = getDecoratedTop(item);
            final int bottom = getDecoratedBottom(item);
            final int left = getDecoratedLeft(item);
            final int right = getDecoratedRight(item);
            final Rect viewRect = new Rect(left, top, right, bottom);
            final boolean intersect = viewRect.intersect(mainRect);
            if (intersect) {
                final int square = viewRect.width() * viewRect.height();
                if (maxSquare < square) {
                    view = item;
                }
            }
        }
        return view;
    }

    @Override
    public void smoothScrollToPosition(final RecyclerView recyclerView, final RecyclerView.State state, final int position) {
        if (position >= getItemCount()) {
            Log.e(ExpandLayoutManager.class.getName(), "Cannot scroll to " + position + ", item count is " + getItemCount());
            return;
        }

        final LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(final int targetPosition) {
                return ExpandLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    private PointF computeScrollVectorForPosition(final int targetPosition) {
        if (0 == getChildCount()) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;
        return new PointF(0, direction);
    }

    @Override
    public boolean canScrollVertically() {
        return !isOpen();
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public int scrollVerticallyBy(final int dy, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        return scrollBy(dy, recycler);
    }

    private int scrollBy(final int dy, final RecyclerView.Recycler recycler) {
        if (0 == getChildCount() || 0 == dy) {
            return 0;
        }
        final int childCount = getChildCount();
        final int itemCount = getItemCount();

        final View topView = getChildAt(0);
        final View bottomView = getChildAt(childCount - 1);

        final int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= mItemSize) {
            return 0;
        }

        int delta = 0;
        if (0 > dy) {
            final View firstView = findViewByPosition(0);
            if (null == firstView) {
                delta = dy;
            } else {
                final int firstViewAdapterPos = getPosition(firstView);
                if (0 < firstViewAdapterPos) {
                    delta = dy;
                } else {
                    final int topOffset = getDecoratedTop(firstView);
                    delta = Math.max(topOffset, dy);
                }
            }
        } else if (0 < dy) {
            final View lastView = getChildAt(childCount - 1);
            final int lastViewAdapterPos = getPosition(lastView);
            if (lastViewAdapterPos < itemCount - 1) {
                delta = dy;
            } else {
                final int viewBottom = getDecoratedBottom(lastView);
                final int parentBottom = getHeight();
                delta = Math.min(viewBottom - parentBottom, dy);
            }
        }
        offsetChildrenVertical(-delta);
        fill(recycler);
        return delta;
    }

    private void measureChildWithDecorationsAndMargin(final View child, final int widthSpec, final int heightSpec) {
        final Rect decorRect = new Rect();
        calculateItemDecorationsForChild(child, decorRect);
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int widthS = updateSpecWithExtra(widthSpec, lp.leftMargin + decorRect.left, lp.rightMargin + decorRect.right);
        final int heightS = updateSpecWithExtra(heightSpec, lp.topMargin + decorRect.top, lp.bottomMargin + decorRect.bottom);
        child.measure(widthS, heightS);
    }

    private int updateSpecWithExtra(final int spec, final int startInset, final int endInset) {
        if (0 == startInset && 0 == endInset) {
            return spec;
        }
        final int mode = View.MeasureSpec.getMode(spec);
        if (View.MeasureSpec.AT_MOST == mode || View.MeasureSpec.EXACTLY == mode) {
            return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(spec) - startInset - endInset, mode);
        }
        return spec;
    }

    private static class ViewAnimationInfo {
        int startTop;
        int startBottom;
        int finishTop;
        int finishBottom;
        View view;
    }
}
