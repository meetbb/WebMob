package com.wm.instawebmob.object;

public class LocationObject {

    private String latitude;
    private String longitude;
    private String name;
    private String id;

    public LocationObject(String latitude, String longitude, String name, String id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.id = id;
    }

    public String getLatitude() {
        return latitude;

    }
    public String getLongitude() {
        return longitude;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
