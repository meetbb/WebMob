package com.wm.instawebmob.object;

public class DataObject {

    String dataId;
    String userName;
    String fullName;
    String profilePicture;
    String imageUrl;
    String imageWidth;
    String imageHeight;
    String locationId;
    String latitude;
    String longitude;
    String locationName;

    public DataObject(String dataId, String userName, String fullName, String profilePicture, String imageUrl, String imageWidth, String imageHeight, String locationId, String latitude, String longitude, String locationName) {
        this.dataId = dataId;
        this.userName = userName;
        this.fullName = fullName;
        this.profilePicture = profilePicture;
        this.imageUrl = imageUrl;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getImageHeight() {
        return imageHeight;
    }

    public String getImageWidth() {
        return imageWidth;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserName() {
        return userName;
    }

    public String getDataId() {
        return dataId;
    }

}
