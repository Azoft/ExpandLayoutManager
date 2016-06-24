package com.azoft.expandlayoutmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.azoft.expandlayoutmanager.model.ExpandModel;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class LayoutHelper {

    private final List<LayoutOrder> mLayoutOrder;
    private final List<WeakReference<LayoutOrder>> mReusedItems;
    private final int[] mFirstRencerPair = new int[2];

    private final ExpandModel mExpandModel;

    LayoutHelper(@NonNull final ExpandModel expandModel) {
        mLayoutOrder = new ArrayList<>();
        mReusedItems = new ArrayList<>();

        mExpandModel = expandModel;
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

    public void generateLayoutOrder(final RecyclerView.State state, final int heightNoPadding) {
        clearItems();

        final int expandItemPosition = mExpandModel.getExpandItemPosition();
        if (ExpandLayoutManager.INVALID_POSITION == expandItemPosition) {
            mExpandModel.generateFirstRenderData(heightNoPadding, mFirstRencerPair);

            int top = mFirstRencerPair[1];

            for (int i = mFirstRencerPair[0]; i < state.getItemCount() - 1; ++i) {
                final int itemHeight = mExpandModel.getItemSize(i, heightNoPadding);
                final int bottom = top + itemHeight;

                createItem(i, top, bottom);

                top = bottom;
                if (top > heightNoPadding) {
                    break;
                }
            }
        } else {
            createItem(expandItemPosition, 0, heightNoPadding);
        }
    }

    public List<LayoutOrder> getLayoutOrder() {
        return Collections.unmodifiableList(mLayoutOrder);
    }

    public static final class LayoutOrder implements Serializable {

        private static final long serialVersionUID = 6373870678521262745L;

        private int mItemAdapterPosition;
        private int mTop;
        private int mBottom;

        public int getItemAdapterPosition() {
            return mItemAdapterPosition;
        }

        public int getTop() {
            return mTop;
        }

        public int getBottom() {
            return mBottom;
        }
    }
}