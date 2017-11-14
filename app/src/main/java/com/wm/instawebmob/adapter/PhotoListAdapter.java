package com.wm.instawebmob.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.wm.instawebmob.MainActivity;
import com.wm.instawebmob.R;

import java.util.ArrayList;

public class PhotoListAdapter extends BaseAdapter {
    private Context mContext;

    private ImageLoader mImageLoader;
    private MainActivity.AnimateFirstDisplayListener mAnimator;

    private ArrayList<String> mPhotoList;

    private int mWidth;
    private int mHeight;

    public PhotoListAdapter(Context context) {
        mContext = context;

        DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.warning)
                .showImageOnFail(R.drawable.warning)
                .cacheInMemory(true)
                .cacheOnDisc(false)
                .considerExifParams(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .writeDebugLogs()
                .defaultDisplayImageOptions(displayOptions)
                .build();

        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);

        mAnimator = new MainActivity.AnimateFirstDisplayListener();
    }

    public void setData(ArrayList<String> data) {
        mPhotoList = data;
    }

    public void setLayoutParam(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getCount() {
        return (mPhotoList == null) ? 0 : mPhotoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPhotoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView = convertView;
        final ViewHolder holder;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.griditem, null);
            holder = new ViewHolder();
        } else {
            holder = (ViewHolder) gridView.getTag();
        }
        holder.image = (ImageView) gridView.findViewById(R.id.grid_item_image);
        gridView.setTag(holder);
        ImageAware imageAware = new ImageViewAware(holder.image, false);
        mImageLoader.displayImage(mPhotoList.get(position), imageAware, mAnimator);
        return gridView;
    }

    private static class ViewHolder {
        public ImageView image;
    }
}