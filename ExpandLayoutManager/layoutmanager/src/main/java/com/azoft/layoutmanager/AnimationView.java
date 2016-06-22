package com.azoft.layoutmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public final class AnimationView extends FrameLayout {

    private static final float EXPAND_ANGLE = -90f;

    private ExpandLayoutManager mExpandLayoutManager;

    public AnimationView(final Context context) {
        super(context);
    }

    public AnimationView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimationView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    void setLayoutManager(final ExpandLayoutManager expandLayoutManager) {
        mExpandLayoutManager = expandLayoutManager;
        final View viewToAnimate = getViewToAnimate();

        viewToAnimate.setVisibility(View.GONE);
        viewToAnimate.setPivotY(0);
        viewToAnimate.setRotationX(0);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mExpandLayoutManager.actionItem(AnimationView.this);
            }
        });
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

    void doExpandAnimation(final float animationProgress) {
        final View viewToAnimate = getViewToAnimate();

        viewToAnimate.setVisibility(View.VISIBLE);
        viewToAnimate.setPivotX(getWidth() * 0.5f);
        viewToAnimate.setRotationX(EXPAND_ANGLE * (1 - animationProgress));
    }

    void doCollapseAnimation(final float animationProgress) {
        final View viewToAnimate = getViewToAnimate();

        if (0.999999f < animationProgress) {
            viewToAnimate.setVisibility(View.GONE);
            viewToAnimate.setRotationX(EXPAND_ANGLE);
        } else {
            viewToAnimate.setVisibility(View.VISIBLE);
            viewToAnimate.setPivotX(getWidth() * 0.5f);
            viewToAnimate.setRotationX(EXPAND_ANGLE * animationProgress);
        }
    }

    View getViewToAnimate() {
        final View viewToAnimate = findViewById(R.id.view_to_animate);
        if (null == viewToAnimate) {
            return this;
        }
        return viewToAnimate;
    }
}