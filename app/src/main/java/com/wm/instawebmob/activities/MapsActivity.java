package com.wm.instawebmob.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koushikdutta.ion.Ion;
import com.wm.instawebmob.R;
import com.wm.instawebmob.database.SQLiteHelper;
import com.wm.instawebmob.maps.GPSTracker;
import com.wm.instawebmob.object.DataObject;
import com.wm.instawebmob.utils.ConnectionManager;
import com.wm.instawebmob.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 10000;// 60 * 1; // 1 minute
    protected LocationManager locationManager;
    ConnectionManager connectionManager;
    GoogleMap mGoogleMap;
    double mLatitude = 0;
    double mLongitude = 0;
    GPSTracker gpsTracker;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initViews();
    }

    private void initViews() {
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager
                .getBestProvider(criteria, false);
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        connectionManager = new ConnectionManager(MapsActivity.this);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            gpsTracker = new GPSTracker(this);
            gpsTracker.showSettingsAlert();
        } else {
            this.canGetLocation = true;
            // First get location from Network Provider
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, MapsActivity.this);
                Log.d("Network", "Network");
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(provider);
                    if (location != null) {
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                    }
                }
            }
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(provider);
                        if (location != null) {
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();
                        }
                    }
                }
            }
        }

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, MapsActivity.this, requestCode);
            dialog.show();

        } else {
            SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            fragment.getMapAsync(MapsActivity.this);
            PermissionUtils.checkLocationPermission(MapsActivity.this);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(provider, 0, 0, MapsActivity.this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        addCustomMarker();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (null != mGoogleMap) {
            mGoogleMap.clear();
            MarkerOptions mp = new MarkerOptions();
            mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
            mp.title("My position");
            mGoogleMap.addMarker(mp);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 16));
            addCustomMarker();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void addCustomMarker() {
        if (mGoogleMap == null) {
            return;
        }
        // adding a marker on map with image from  drawable
        SQLiteHelper sqLiteHelper = new SQLiteHelper(this);
        ArrayList<DataObject> dataList = sqLiteHelper.getAllLocationData();
        int dataLength = dataList.size();
        for (int i = 0; i < dataLength; i++) {
            DataObject dataObject = dataList.get(i);
            double longitude = Double.parseDouble(dataObject.getLongitude());
            double latitude = Double.parseDouble(dataObject.getLatitude());
            try {
                Bitmap bmImg = Ion.with(MapsActivity.this)
                        .load(dataObject.getImageUrl()).withBitmap().resize(100, 100).asBitmap().get();
                MarkerOptions mp = new MarkerOptions();
                mp.position(new LatLng(latitude, longitude));
                mp.title(dataObject.getLocationName());
                mp.icon(BitmapDescriptorFactory.fromBitmap(bmImg));
                mGoogleMap.addMarker(mp);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
//        mMap.addMarker(new MarkerOptions()
    }
}
