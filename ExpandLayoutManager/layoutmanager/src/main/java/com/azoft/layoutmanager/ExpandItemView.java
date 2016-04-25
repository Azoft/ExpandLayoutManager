package com.azoft.layoutmanager;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Date: 01.12.15
 * Time: 17:39
 *
 * @author Artem Zalevskiy
 */
public class ExpandItemView extends RelativeLayout implements AnimationView {

    private static final float HALF = 0.5f;
    private static final float ANGLE = -90f;
    private static final int DURATION = 400;

    private View mItemView;
    private View mContainer;
    private boolean mIsOpen;
    private final AnimatorSet mAnimationOpenSet = new AnimatorSet();
    private final AnimatorSet mAnimationCloseSet = new AnimatorSet();

    public ExpandItemView(final Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.info_item, this, true);

        mItemView = findViewById(R.id.rl_item_id);
        mContainer = findViewById(R.id.tv_container_text_id);
        mAnimationOpenSet.addListener(getAnimationListener());
        mAnimationCloseSet.addListener(getAnimationListener());
        mAnimationOpenSet.setDuration(DURATION);
        mAnimationCloseSet.setDuration(DURATION);

        post(new Runnable() {
            @Override
            public void run() {
                mContainer.setPivotY(0);
                mContainer.setPivotX(mContainer.getWidth() * HALF);
                mContainer.setRotationX(ANGLE);
                mAnimationOpenSet.play(ObjectAnimator.ofFloat(mContainer, View.ROTATION_X, ANGLE, 0f));
                mAnimationCloseSet.play(ObjectAnimator.ofFloat(mContainer, View.ROTATION_X, 0f, ANGLE));
            }
        });
    }

    @Override
    public void openAnimation() {
        mIsOpen = true;
        mAnimationOpenSet.start();
    }

    @Override
    public void closeAnimation() {
        mIsOpen = false;
        mAnimationCloseSet.start();
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    private Animator.AnimatorListener getAnimationListener() {
        return new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(final Animator animation) {
                mItemView.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                mItemView.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                mItemView.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        };
    }
}
