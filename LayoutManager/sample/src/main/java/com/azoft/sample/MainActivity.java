package com.azoft.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azoft.sample.data.CitiesResponse;
import com.azoft.sample.data.City;
import com.azoft.expandlayoutmanager.ExpandLayoutManager;
import com.azoft.expandlayoutmanager.view.SimpleAnimationView;
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
//        mLayoutManager = new OldExpandLayoutManager(getResources().getDimensionPixelSize(R.dimen.height_item));
        final ExpandLayoutManager layoutManager = new ExpandLayoutManager();
        //noinspection ConstantConditions
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(dataAdapter);

        //noinspection ConstantConditions
        findViewById(R.id.b_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                layoutManager.actionItem(1000);
            }
        });
    }

    private static final class DataAdapter extends RecyclerView.Adapter<DataAdapter.InfoViewHolder> {

        private final int[] mColors;
        private final List<City> mCityList;
        private final Random mRandom = new Random();

        DataAdapter(final List<City> cityList) {
            mCityList = cityList;
            mColors = new int[getItemCount()];
            for (int i = 0; i < getItemCount(); ++i) {
                //noinspection MagicNumber
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
            }
        }

        @Override
        public InfoViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            return new InfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.info_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final InfoViewHolder holder, final int position) {
            final City city = mCityList.get(position);
            holder.mNameView.setText(city.getName());
            holder.mTextView.setText(city.getDescription());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ((SimpleAnimationView) v).actionItem();
                }
            });
            holder.mCoverImage.setBackgroundColor(mColors[position]);
        }

        @Override
        public int getItemCount() {
            return null == mCityList ? 0 : mCityList.size();
        }

        public static class InfoViewHolder extends RecyclerView.ViewHolder {

            private final ImageView mCoverImage;
            private final TextView mNameView;
            private final TextView mTextView;

            public InfoViewHolder(final View view) {
                super(view);
                mCoverImage = (ImageView) view.findViewById(R.id.iv_icon_id);
                mNameView = (TextView) view.findViewById(R.id.tv_name_id);
                mTextView = (TextView) view.findViewById(R.id.tv_text_id);
            }
        }
    }
}
