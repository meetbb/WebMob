package com.wm.instawebmob.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wm.instawebmob.object.DataObject;
import com.wm.instawebmob.utils.Constants;

import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "InstaWebMob.db";
    public static final String TABLE_NAME = "PEOPLE";
    public static final String COLUMN_ID = "ID";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;
    private Context mContext;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Constants.DATA_ID + " VARCHAR, " + Constants.USER_NAME + " VARCHAR, "
                + Constants.USER_FULL_NAME + " VARCHAR, "
                + Constants.PROFILE_PICTURE + " VARCHAR, "
                + Constants.IMAGE_URL + " VARCHAR, "
                + Constants.IMAGE_WIDTH + " VARCHAR, "
                + Constants.IMAGE_HEIGHT + " VARCHAR, "
                + Constants.LOCATION_ID + " VARCHAR, "
                + Constants.LATITUDE + " VARCHAR, "
                + Constants.LONGITUDE + " VARCHAR, "
                + Constants.LOCATION_NAME + " VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertRecord(DataObject dataObject) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.DATA_ID, dataObject.getDataId());
        contentValues.put(Constants.USER_NAME, dataObject.getUserName());
        contentValues.put(Constants.USER_FULL_NAME, dataObject.getFullName());
        contentValues.put(Constants.PROFILE_PICTURE, dataObject.getProfilePicture());
        contentValues.put(Constants.IMAGE_URL, dataObject.getImageUrl());
        contentValues.put(Constants.IMAGE_WIDTH, dataObject.getImageWidth());
        contentValues.put(Constants.IMAGE_HEIGHT, dataObject.getImageHeight());
        contentValues.put(Constants.LOCATION_ID, dataObject.getLocationId());
        contentValues.put(Constants.LATITUDE, dataObject.getLatitude());
        contentValues.put(Constants.LONGITUDE, dataObject.getLongitude());
        contentValues.put(Constants.LOCATION_NAME, dataObject.getLocationName());
        database.insert(TABLE_NAME, null, contentValues);
        database.close();
    }

    public boolean isRecordExists(String dataId) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                if (cursor.getString(1).equals(dataId)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        cursor.close();
        database.close();
        return false;
    }

    public ArrayList<DataObject> getAllRecords() {
        ArrayList<DataObject> dataList = new ArrayList<>();
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                    DataObject dataObject = new DataObject(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),
                            cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(11));
                    dataList.add(dataObject);
            }
        }
        cursor.close();
        database.close();
        return dataList;
    }

    public ArrayList<DataObject> getAllLocationData() {
        ArrayList<DataObject> dataList = new ArrayList<>();
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                if (!cursor.getString(8).equals("")) {
                    DataObject dataObject = new DataObject(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),
                            cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(11));
                    dataList.add(dataObject);
                }
            }
        }
        cursor.close();
        database.close();
        return dataList;
    }
}