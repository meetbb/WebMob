package com.wm.instawebmob;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wm.instawebmob.activities.MapsActivity;
import com.wm.instawebmob.adapter.PhotoListAdapter;
import com.wm.instawebmob.database.SQLiteHelper;
import com.wm.instawebmob.instagram.Instagram;
import com.wm.instawebmob.instagram.InstagramRequest;
import com.wm.instawebmob.instagram.InstagramSession;
import com.wm.instawebmob.instagram.InstagramUser;
import com.wm.instawebmob.object.DataObject;
import com.wm.instawebmob.staticdata.ApplicationData;
import com.wm.instawebmob.utils.ConnectionManager;
import com.wm.instawebmob.utils.Constants;
import com.wm.instawebmob.utils.ParseJSON;
import com.wm.instawebmob.utils.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ConnectionManager connectionManager;
    private InstagramSession mInstagramSession;
    private Instagram mInstagram;
    private ProgressBar mLoadingPb;
    private GridView mGridView;
    private TextView fullNameText, userNameText;
    private ImageView profilePicture;
    AnimateFirstDisplayListener animate;
    JSONArray dataArray;
    ImageLoader imageLoader;
    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(InstagramUser user) {
            finish();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        }

        @Override
        public void onError(String error) {
            Utils.showToast(MainActivity.this, error);
        }

        @Override
        public void onCancel() {
            Utils.showToast(MainActivity.this, "OK. Maybe later?");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionManager = new ConnectionManager(this);
        mInstagram = new Instagram(this, ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        mInstagramSession = mInstagram.getSession();

        if (mInstagramSession.isActive()) {
            setContentView(R.layout.activity_user);
            InstagramUser instagramUser = mInstagramSession.getUser();
            mLoadingPb = (ProgressBar) findViewById(R.id.pb_loading);
            mGridView = (GridView) findViewById(R.id.gridView);
            Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
            ((TextView) toolbarTop.findViewById(R.id.toolbar_title)).setText(R.string.app_name);
            toolbarTop.findViewById(R.id.location_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                }
            });
            fullNameText = (TextView) findViewById(R.id.tv_name);
            fullNameText.setText(instagramUser.fullName);
            userNameText = (TextView) findViewById(R.id.tv_username);
            userNameText.setText(instagramUser.username);
            ((Button) findViewById(R.id.btn_logout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mInstagramSession.reset();
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                    finish();
                }
            });
            toolbarTop.findViewById(R.id.syncButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    syncRecords();
                }
            });
            ImageView userIv = (ImageView) findViewById(R.id.iv_user);
            DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading)
                    .showImageForEmptyUri(R.drawable.user)
                    .showImageOnFail(R.drawable.user)
                    .cacheInMemory(true)
                    .cacheOnDisc(false)
                    .considerExifParams(true)
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .writeDebugLogs()
                    .defaultDisplayImageOptions(displayOptions)
                    .build();

            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            animate = new AnimateFirstDisplayListener();
            imageLoader.displayImage(instagramUser.profilPicture, userIv, animate);

            if (connectionManager.isConnectingToInternet()) {
                new DownloadTask().execute();
            } else {
                fetchOfflineRecords();
                Utils.showToast(this,"Please enable the Internet connection!");
            }
        } else {
            setContentView(R.layout.activity_main);
            ((Button) findViewById(R.id.btn_connect)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mInstagram.authorize(mAuthListener);
                }
            });
        }
    }

    private void syncRecords() {
        if (connectionManager.isConnectingToInternet()) {
            mLoadingPb.setVisibility(View.GONE);
            new DownloadTask().execute();
        } else {
            Utils.showToast(this,"Please enable the Internet connection!");
        }
    }

    private void fetchOfflineRecords() {
        SQLiteHelper sqLiteHelper = new SQLiteHelper(this);
        ArrayList<DataObject> dataObjectArrayList = sqLiteHelper.getAllRecords();
        ArrayList<String> photoList = new ArrayList<>();
        DataObject userObject = dataObjectArrayList.get(0);
        fullNameText.setText(userObject.getFullName());
        userNameText.setText(userObject.getUserName());
        int dataListSize = dataObjectArrayList.size();
        for (int i = 0; i < dataListSize; i++) {
            DataObject dataObject = dataObjectArrayList.get(i);
            photoList.add(dataObject.getImageUrl());
        }
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) Math.ceil((double) dm.widthPixels / 2);
        width = width - 50;
        int height = width;
        PhotoListAdapter adapter = new PhotoListAdapter(MainActivity.this);
        adapter.setData(photoList);
        adapter.setLayoutParam(width, height);
        mGridView.setAdapter(adapter);
    }

    public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    public class DownloadTask extends AsyncTask<URL, Integer, Long> {
        ArrayList<String> photoList;

        protected void onCancelled() {

        }

        protected void onPreExecute() {

        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                List<NameValuePair> params = new ArrayList<>(1);
                params.add(new BasicNameValuePair("count", "100"));
                InstagramRequest request = new InstagramRequest(mInstagramSession.getAccessToken());
                String response = request.createRequest("GET", "/users/self/media/recent?", params);
                if (!response.equals("")) {
                    JSONObject mainJSONObject = ParseJSON.getMainObject(response);
                    dataArray = ParseJSON.getDataArray(mainJSONObject);
                    ParseJSON.syncRecord(MainActivity.this, dataArray);
                    photoList = ParseJSON.getPhotoList(dataArray, Constants.LOW_RESOLUTION);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            mLoadingPb.setVisibility(View.GONE);
            if (null == photoList) {
                Utils.showToast(getApplicationContext(), "No Photos Available");
            } else {
                try {
                    JSONObject userObject = dataArray.getJSONObject(0).getJSONObject(Constants.USER_KEY);
                    ImageAware imageAware = new ImageViewAware(profilePicture, false);
                    imageLoader.displayImage(userObject.getString(Constants.PROFILE_PICTURE), imageAware, animate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = (int) Math.ceil((double) dm.widthPixels / 2);
                width = width - 50;
                int height = width;
                PhotoListAdapter adapter = new PhotoListAdapter(MainActivity.this);
                adapter.setData(photoList);
                adapter.setLayoutParam(width, height);
                mGridView.setAdapter(adapter);
                Utils.showToast(getApplicationContext(), "Images Synchronized successfully!");
            }
        }
    }
}
