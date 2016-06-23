package com.azoft.layoutmanager;

import java.io.Serializable;

public final class AnimationAction implements Serializable {

    private static final long serialVersionUID = -6133802071579174574L;

    public final int mExpandPosition;
    public final int mCollapsePosition;

    private AnimationAction(final int expandPosition, final int collapsePosition) {
        mExpandPosition = expandPosition;
        mCollapsePosition = collapsePosition;
    }

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

    public static AnimationAction createExpandAction(final int expandPosition) {
        return new AnimationAction(expandPosition, ExpandLayoutManager.INVALID_POSITION);
    }

    public static AnimationAction createCollapseAction(final int collapsePosition) {
        return new AnimationAction(ExpandLayoutManager.INVALID_POSITION, collapsePosition);
    }

    public static AnimationAction createdAction(final int expandPosition, final int collapsePosition) {
        return new AnimationAction(expandPosition, collapsePosition);
    }
}