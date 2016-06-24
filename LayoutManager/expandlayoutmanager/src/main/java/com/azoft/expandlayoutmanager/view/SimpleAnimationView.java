package com.azoft.expandlayoutmanager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.azoft.expandlayoutmanager.ExpandLayoutManager;
import com.azoft.expandlayoutmanager.utils.GeneralUtils;
import com.azoft.expandlayoutmanager.R;

public final class SimpleAnimationView extends FrameLayout implements AnimationView {

    private static final float EXPAND_ANGLE = -90f;

    private ExpandLayoutManager mExpandLayoutManager;

    public SimpleAnimationView(final Context context) {
        super(context);
    }

    public SimpleAnimationView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleAnimationView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimpleAnimationView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setExpandLayoutManager(final ExpandLayoutManager expandLayoutManager) {
        if (!GeneralUtils.equals(mExpandLayoutManager, expandLayoutManager)) {
            mExpandLayoutManager = expandLayoutManager;
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != mExpandLayoutManager) {
                        mExpandLayoutManager.actionItem(SimpleAnimationView.this);
                    }
                }
            });
        }
    }

    @Override
    public boolean resetState() {
        final View viewToAnimate = getViewToAnimate();

        final boolean hasChange = View.GONE != viewToAnimate.getVisibility();

        viewToAnimate.setVisibility(View.GONE);
        viewToAnimate.setPivotY(0);
        viewToAnimate.setRotationX(0);

        return hasChange;
    }

    public void actionItem() {
        mExpandLayoutManager.actionItem(this);
    }

    public void collapseItem() {
        mExpandLayoutManager.collapseItem(this);
    }

    public void expandItem() {
        mExpandLayoutManager.expandItem(this);
    }

    @Override
    public boolean doExpandAnimation(final float animationProgress) {
        final View viewToAnimate = getViewToAnimate();

        final boolean hasChange = View.VISIBLE != viewToAnimate.getVisibility();

        viewToAnimate.setVisibility(View.VISIBLE);
        viewToAnimate.setPivotX(getWidth() * 0.5f);
        viewToAnimate.setRotationX(EXPAND_ANGLE * (1 - animationProgress));

        return hasChange;
    }

    @Override
    public boolean doCollapseAnimation(final float animationProgress) {
        final View viewToAnimate = getViewToAnimate();

        final boolean hasChange = View.VISIBLE != viewToAnimate.getVisibility();

        viewToAnimate.setVisibility(View.VISIBLE);
        viewToAnimate.setPivotX(getWidth() * 0.5f);
        viewToAnimate.setRotationX(EXPAND_ANGLE * animationProgress);

        return hasChange;
    }

    private View getViewToAnimate() {
        final View viewToAnimate = findViewById(R.id.view_to_animate);
        if (null == viewToAnimate) {
            return this;
        }
        return viewToAnimate;
    }
}