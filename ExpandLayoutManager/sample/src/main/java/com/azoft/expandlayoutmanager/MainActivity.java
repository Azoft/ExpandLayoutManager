package com.azoft.expandlayoutmanager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azoft.expandlayoutmanager.data.CitiesResponse;
import com.azoft.expandlayoutmanager.data.City;
import com.azoft.layoutmanager.ExpandItemView;
import com.azoft.layoutmanager.ExpandLayoutManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Random;

/**
 * Date: 01.12.15
 * Time: 17:16
 *
 * @author Artem Zalevskiy
 */
public class MainActivity extends AppCompatActivity {

    private ExpandLayoutManager mLayoutManager;
    private int mCurrentPosition;
    private final Gson mGson = new GsonBuilder().create();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initList(getData());
    }

    private CitiesResponse getData() {
        try {
            final InputStream input = getAssets().open("cities.json");
            final Reader reader = new InputStreamReader(input, "UTF-8");
            return mGson.fromJson(reader, CitiesResponse.class);
        } catch (final Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return null;
    }

    private void initList(final CitiesResponse citiesResponse) {
        if (null == citiesResponse || null == citiesResponse.getCities()) {
            return;
        }
        final DataAdapter dataAdapter = new DataAdapter(citiesResponse.getCities());
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new ExpandLayoutManager(getResources().getDimensionPixelSize(R.dimen.height_item));
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(dataAdapter);
        dataAdapter.setItemClickListener(new DataAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(final int pos) {
                mCurrentPosition = pos;
                mLayoutManager.actionItem(mCurrentPosition);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (null == mLayoutManager || !mLayoutManager.isOpen()) {
            super.onBackPressed();
        } else {
            mLayoutManager.closeItem(mCurrentPosition);
        }
    }

    private static final class DataAdapter extends RecyclerView.Adapter<DataAdapter.InfoViewHolder> {

        private final int[] mColors;
        private final List<City> mCityList;
        private final Random mRandom = new Random();
        private OnItemClickListener mItemClickListener;

        DataAdapter(final List<City> cityList) {
            mCityList = cityList;
            mColors = new int[getItemCount()];
            for (int i = 0; i < getItemCount(); ++i) {
                //noinspection MagicNumber
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
            }
        }

        public void setItemClickListener(final OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @Override
        public InfoViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            return new InfoViewHolder(new ExpandItemView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(final InfoViewHolder viewHolder, final int position) {
            final City city = mCityList.get(position);
            viewHolder.mNameView.setText(city.getName());
            viewHolder.mTextView.setText(city.getDescription());
            viewHolder.mItemListView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (null != mItemClickListener && v.isEnabled()) {
                        mItemClickListener.onItemClicked(position);
                    }
                }
            });
            viewHolder.mCoverImage.setBackgroundColor(mColors[position]);
        }

        @Override
        public int getItemCount() {
            return null == mCityList ? 0 : mCityList.size();
        }

        public static class InfoViewHolder extends RecyclerView.ViewHolder {

            private final ImageView mCoverImage;
            private final TextView mNameView;
            private final TextView mTextView;
            private final View mItemListView;

            public InfoViewHolder(final View view) {
                super(view);
                mCoverImage = (ImageView) view.findViewById(R.id.iv_icon_id);
                mNameView = (TextView) view.findViewById(R.id.tv_name_id);
                mTextView = (TextView) view.findViewById(R.id.tv_text_id);
                mItemListView = view.findViewById(R.id.rl_item_id);
            }
        }

        public interface OnItemClickListener {
            void onItemClicked(int pos);
        }
    }

}
