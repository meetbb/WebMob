package com.wm.instawebmob.utils;

import android.content.Context;

import com.wm.instawebmob.database.SQLiteHelper;
import com.wm.instawebmob.object.DataObject;
import com.wm.instawebmob.object.LocationObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class ParseJSON {

    public static JSONObject getMainObject(String response) throws JSONException {
        return (JSONObject) new JSONTokener(response).nextValue();
    }

    public static JSONArray getDataArray(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONArray(Constants.DATA_KEY);
    }

    public static ArrayList<String> getPhotoList(JSONArray dataArray, String resolution) throws JSONException {
        ArrayList<String> photoList = new ArrayList<>();
        int length = dataArray.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                JSONObject jsonPhoto = dataArray.getJSONObject(i).getJSONObject(Constants.IMAGES_KEY).getJSONObject(resolution);
                photoList.add(jsonPhoto.getString(Constants.URL_KEY));
            }
        }
        return photoList;
    }

    public static final ArrayList<LocationObject> getLocationObject(JSONArray dataArray) throws JSONException {
        ArrayList<LocationObject> locationList = new ArrayList<>();
        int length = dataArray.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                JSONObject jsonLocation = dataArray.getJSONObject(i).getJSONObject(Constants.LOCATION_KEY);
                locationList.add(new LocationObject(jsonLocation.getString(Constants.LATITUDE), jsonLocation.getString(Constants.LONGITUDE),
                        jsonLocation.getString(Constants.NAME_KEY), jsonLocation.getString(Constants.ID_KEY)));
            }
        }
        return locationList;
    }

    public static boolean syncRecord(Context context, JSONArray dataArray) throws JSONException {
        try {
            SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
            int dataArrayLength = dataArray.length();
            for (int i = 0; i < dataArrayLength; i++) {
                JSONObject innerObject = dataArray.getJSONObject(i);
                String dataId = innerObject.getString(Constants.ID_KEY);
                if (!sqLiteHelper.isRecordExists(dataId)) {
                    JSONObject userObject = innerObject.getJSONObject(Constants.USER_KEY);
                    String username = userObject.getString(Constants.USER_NAME);
                    String fullName = userObject.getString(Constants.USER_FULL_NAME);
                    String profilePic = userObject.getString(Constants.PROFILE_PICTURE);
                    JSONObject imageObject = innerObject.getJSONObject(Constants.IMAGES_KEY);
                    JSONObject resolutionObject = imageObject.getJSONObject(Constants.LOW_RESOLUTION);
                    String locationId = "";
                    String latitude = "";
                    String longitude = "";
                    String locationName = "";
                    if (!innerObject.isNull(Constants.LOCATION_KEY)) {
                        JSONObject locationObject = innerObject.getJSONObject(Constants.LOCATION_KEY);
                        locationId = locationObject.getString(Constants.ID_KEY);
                        latitude = locationObject.getString(Constants.LATITUDE);
                        longitude = locationObject.getString(Constants.LONGITUDE);
                        locationName = locationObject.getString(Constants.NAME_KEY);
                    }
                    sqLiteHelper.insertRecord(new DataObject(dataId, username, fullName,
                            profilePic, resolutionObject.getString(Constants.IMAGE_URL), resolutionObject.getString(Constants.IMAGE_WIDTH),
                            resolutionObject.getString(Constants.IMAGE_HEIGHT), locationId, latitude,
                            longitude, locationName));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}